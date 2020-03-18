package com.example.sinbal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

import android.media.MediaPlayer;

public class MainActivity extends AppCompatActivity {

    private BluetoothSPP bt;
    int size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent(); //변수 받아오기 추가 부분
        size = intent.getIntExtra("size",0);
        Log.d("MainActivity", "Size = "+size); //log에 띄워줌
        //Toast.makeText(this,"Size = "+size, Toast.LENGTH_LONG).show(); //화면에 띄워줌
        bt = new BluetoothSPP(this); //Initializing


        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        // bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
        //   public void onDataReceived(byte[] data, String message) {
        //     Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        //}
        //});


        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            TextView distance22 = findViewById(R.id.distance2);
            TextView distance33 = findViewById(R.id.distance3);
            TextView distance44 = findViewById(R.id.distance4);


            double final_size = MainActivity.this.size; // 2값 바꾸기


            public void onDataReceived(byte[] data, String message) { //데이터 수신용 코드 추가


                Log.d("MainActivity", "Final size = "+final_size);
                String[] array = message.split(",");

                distance22.setText(array[0].concat("cm"));
                distance33.setText(array[1].concat("cm"));
                distance44.setText(array[2].concat("cm"));

                double distance2 = Double.parseDouble(array[0]);
                double distance3 = Double.parseDouble(array[1]);
                double distance4 = Double.parseDouble(array[2]);

                double distance24 = distance2/0.93969; //밑변길이를 cos20으로 나눈 값//대각선 길이

                //벽 & 오르막
                if(distance4<30){
                    if(distance4>distance24){
                        //오르막
                        final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.uphill);
                        mp.start();
                    }
                    else{
                        //벽
                        if(distance2<final_size){
                            //MyApplication myApp = (MyApplication) getApplication();

                            //double num2 = myApp.getGlobalValue2(); //num 0, turn 0 //num 1, turn 1
                            double num2 = 1.0;

                            if(num2 == 1.0) {
                                //num 1, turn 1
                               // myApp.setGlobalValue2(0.0); //num1, turn 0
                                final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.wall);
                                mp.start();

                            }
                            if(distance2<10) {
                                final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.close);
                                mp.start();
                            }


                        }
//                        else {
//                            MyApplication myApp = (MyApplication) getApplication();
//                            myApp.setGlobalValue2(1.0); //num 0, turn 1
//                        }
                    }

                }
                //장애물
                else{
                    MyApplication myApp = (MyApplication) getApplication();
                    if(distance2<100){

                        int num = myApp.getGlobalValue();

                        if(num == 1) {

                            final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.block);
                            mp.start();

                            Log.d("MainActivity", "Num1 = "+num);
                            myApp.setGlobalValue(0);
                            Log.d("MainActivity", "Num2 = "+num);
                        }
                        if(distance2<10) {
                            final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.close);
                            mp.start();
                        }

                    }
                    else {
                        int num = myApp.getGlobalValue();
                        Log.d("MainActivity", "Num3 = "+num);

                        myApp.setGlobalValue(1);
                    }
                }

                //내리막
                if(distance3>16.6){
                    final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.downhill);
                    mp.start();
                }


            }

        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때

            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = findViewById(R.id.btnConnect); //연결시도
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
                setup();
            }
        }
    }

    public void setup() {
        Button btnSend = findViewById(R.id.btnSend); //데이터 전송
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bt.send("Text", true);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    } }
