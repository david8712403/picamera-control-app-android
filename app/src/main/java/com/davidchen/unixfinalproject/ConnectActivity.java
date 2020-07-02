package com.davidchen.unixfinalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class ConnectActivity extends AppCompatActivity {

    final String tag = "ConnectActivity";
    private EditText etAddress;
    private Button btConnect;
    private Button btImgList;
    private Button btMqtt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        etAddress = findViewById(R.id.address_edittext);
        btConnect = findViewById(R.id.connect_button);
        btImgList = findViewById(R.id.image_list_button);
        btMqtt    = findViewById(R.id.mqtt_button);

        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ConnectActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("address", String.valueOf(etAddress.getText()));
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        btImgList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ConnectActivity.this, ImageListActivity.class);
                startActivity(i);
            }
        });

        btMqtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ConnectActivity.this, MqttTest.class);
                Bundle bundle = new Bundle();
                bundle.putString("address", String.valueOf(etAddress.getText()));
                i.putExtras(bundle);
                startActivity(i);
            }
        });
    }
}
