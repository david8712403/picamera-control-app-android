    package com.davidchen.unixfinalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Objects;

    public class MqttTest extends AppCompatActivity {
    MqttClient sampleClient = null;
    private final String MQTT_TAG = "mqtt";
    private String topicMsg    = "device/message";
    private String topicZoom    = "device/zoom";
    private String topicAngle   = "device/angle";
    private String content      = "Message from MqttPublishSample";
    private int qos             = 0;
    private String broker       = "tcp://172.20.10.3";
    private String clientId     = "JavaSample";
    private SeekBar sbAngle;
    private SeekBar sbZoom;
    private TextView tvAngle;
    private TextView tvZoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_test);
        broker = "tcp://" + Objects.requireNonNull(this.getIntent().getExtras()).getString("address");

        sbAngle = findViewById(R.id.angle_seekbar);
        sbZoom = findViewById(R.id.zoom_seekbar);
        tvAngle = findViewById(R.id.angle_textview);
        tvZoom = findViewById(R.id.zoom_textview);

        MemoryPersistence persistence = new MemoryPersistence();
        try {
            sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            Log.w(MQTT_TAG,"Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            Log.w(MQTT_TAG,"Connected");
            Log.w(MQTT_TAG,"Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topicMsg, message);
            Log.w(MQTT_TAG,"Message published");

        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }

        sbAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.w("seekbar", "angle:" + progress);
                try {
                    tvAngle.setText("Angle: " + String.valueOf(progress));
                    MqttMessage angle = new MqttMessage(String.valueOf(progress).getBytes());
                    sampleClient.publish(topicAngle, angle);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.w("seekbar", "zoom:" + progress);
                try {
                    tvZoom.setText("Zoom: " + String.valueOf(progress));
                    MqttMessage zoom = new MqttMessage(String.valueOf(progress).getBytes());
                    sampleClient.publish(topicZoom, zoom);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            sampleClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Log.w(MQTT_TAG,"Disconnected");
    }
}
