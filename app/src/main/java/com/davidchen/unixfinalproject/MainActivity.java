package com.davidchen.unixfinalproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    private final String tag = "David";

    MqttClient sampleClient = null;
    private final String MQTT_TAG   = "mqtt";
    private String topicMsg         = "device/message";
    private String topicZoom        = "device/zoom";
    private String topicAngle       = "device/angle";
    private String topicScreenshot  = "device/screenshot";
    private String content          = "Message from MqttPublishSample";
    private int qos                 = 0;
    private String broker           = "tcp://140.124.73.217";//replace to your broker ip.
    private String clientId         = "JavaSample";

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference myRef;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private Button btScreenshot;
    private TextView tvAngle;
    private TextView tvZoom;
    private TextView tvX;
    private TextView tvY;
    private WebView wvStream;
    private SeekBar sbZoom;
    private int angle = 0;
    private float wvHeight;
    private float wvWidth;
    private final int wvWidthSize = 40;
    private float startX = 0;
    private int deltaX = 0;
    private int prevX = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        broker = "tcp://" + Objects.requireNonNull(this.getIntent().getExtras()).getString("address");
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

        btScreenshot = findViewById(R.id.screenshot_button);
        myRef = db.getReference("test");

        tvAngle = findViewById(R.id.angle_textview);
        tvAngle.setText(String.valueOf(angle));
        sbZoom = findViewById(R.id.zoom_seekbar);
        tvZoom = findViewById(R.id.zoom_textview);

        tvX = findViewById(R.id.x_textview);
        tvY = findViewById(R.id.y_textview);

        wvStream = findViewById(R.id.stream_webview);
        WebSettings webSettings = wvStream.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvStream.setWebViewClient(new WebViewClient());
        Bundle b = this.getIntent().getExtras();
        wvStream.loadUrl(b.getString("address"));

        wvStream.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                wvWidth = wvStream.getWidth();
                wvHeight = wvStream.getHeight();
                Log.w(tag, "wvStream W:" + wvWidth + ", H:" + wvHeight);
            }
        });

        wvStream.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int currX;
                if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    currX = (int) (wvWidthSize * (event.getX()/wvWidth));
                    if(prevX != currX) {
                        prevX = currX;
                    }else {
                        return false;
                    }
                    deltaX = (int) (currX - startX);
                    tvX.setText("X: " + event.getX() + "\n" + deltaX);
                    tvY.setText("Y: " + event.getY());

                    try {
                        if(angle + deltaX < 0) {
                            tvAngle.setText("0");
                        }else if(angle + deltaX > 170){
                            tvAngle.setText("170");
                        }else {
                            sampleClient.publish(topicAngle, new MqttMessage(String.valueOf(angle + deltaX).getBytes()));
                            tvAngle.setText(String.valueOf(angle + deltaX));
                        }

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    tvX.setText("X: ");
                    tvY.setText("Y: ");
                    if(angle + deltaX < 0) {
                        angle = 0;
                    }else if(angle + deltaX > 170){
                        angle = 170;
                    }else {
                        angle += deltaX;
                    }
                }else if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    currX = (int) (wvWidthSize * (event.getX()/wvWidth));
                    startX = currX;
                }
                return false;
            }
        });

        sbZoom.setProgress(99);
        sbZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.w("seekbar", "zoom:" + progress);
                try {
                    tvZoom.setText(String.valueOf(progress));
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

        btScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalDateTime now = LocalDateTime.now();
                System.out.println(dtf.format(now));
                myRef.setValue(dtf.format(now));
                Log.w("seekbar", "screenshot: true");
                try {

                    MqttMessage screenshot = new MqttMessage(String.valueOf(true).getBytes());
                    sampleClient.publish(topicScreenshot, screenshot);
                } catch (MqttException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Error");
                    builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    e.printStackTrace();
                }
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
    }
}
