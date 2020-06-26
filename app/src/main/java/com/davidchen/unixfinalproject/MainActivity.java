package com.davidchen.unixfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    private final String tag = "David";
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference myRef;
    private DatabaseReference deviceAngleRef;
    private DatabaseReference deviceZoomRef;
    private DatabaseReference deviceScreenshotRef;
    private DatabaseReference deviceNameRef;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private Button btScreenshot;
    private Button btLeft;
    private Button btRight;
    private TextView tvName;
    private TextView tvAddress;
    private TextView tvAngle;
    private TextView tvZoom;
    private TextView tvX;
    private TextView tvY;
    private WebView wvWtream;
    private SeekBar sbZoom;
    private boolean pressFlag = false;
    private float prevX = 0;
    private float currX = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btScreenshot = findViewById(R.id.screenshot_button);
        btLeft = findViewById(R.id.left_button);
        btRight = findViewById(R.id.right_button);
        myRef = db.getReference("test");
        deviceAngleRef = db.getReference("device/home/angle");
        deviceZoomRef = db.getReference("device/home/zoom");
        deviceScreenshotRef = db.getReference("device/home/screenshot");
        deviceNameRef = db.getReference("device/home/address");

        tvName = findViewById(R.id.screenshots_name_textview);
        tvAngle = findViewById(R.id.angle_textview);
        sbZoom = findViewById(R.id.zoom_seekbar);
        tvZoom = findViewById(R.id.zoom_textview);

        tvX = findViewById(R.id.x_textview);
        tvY = findViewById(R.id.y_textview);

        wvWtream = findViewById(R.id.stream_webview);
        WebSettings webSettings = wvWtream.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvWtream.setWebViewClient(new WebViewClient());
        Bundle b = this.getIntent().getExtras();
        wvWtream.loadUrl("http://" + b.getString("address") + ":8000/index.html");

        wvWtream.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    tvX.setText("X: " + event.getX());
                    tvY.setText("Y: " + event.getY());
                    
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    pressFlag = false;
                    currX = event.getX();
                    if(currX > prevX) {
                        deviceAngleRef.setValue(Float.parseFloat((String) tvAngle.getText()) + 10);
                    }else {
                        if(Float.parseFloat(tvAngle.getText().toString()) > 0) {
                            deviceAngleRef.setValue(Float.parseFloat((String) tvAngle.getText()) - 10);
                        }
                    }
                    Log.w(tag, "ACTION_UP x:" + event.getRawX() + ", y:" + event.getRawY());
                    tvX.setText("X: ");
                    tvY.setText("Y: ");
                }else if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(!pressFlag) {
                        Log.w(tag, "ACTION_DOWN x:" + event.getRawX() + ", y:" + event.getRawY());
                        prevX = event.getX();
                        pressFlag = true;
                    }else {
                        return false;
                    }
                }
                return false;
            }
        });

        sbZoom.setProgress(99);
        sbZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.w(tag, "Seekbar:" + seekBar.getProgress());
                deviceZoomRef.setValue(seekBar.getProgress() + 1);
            }
        });

        deviceZoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tvZoom.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalDateTime now = LocalDateTime.now();
                System.out.println(dtf.format(now));
                myRef.setValue(dtf.format(now));
                deviceScreenshotRef.setValue(true);
            }
        });

        deviceScreenshotRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((boolean)dataSnapshot.getValue() == true) {
                    btScreenshot.setEnabled(false);
                    btScreenshot.setText(getResources().getString(R.string.processing));
                }else {
                    btScreenshot.setEnabled(true);
                    btScreenshot.setText(getResources().getString(R.string.take_picture));
                    Toast.makeText(MainActivity.this, "Picture has uploaded to firebase storage.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(tag, "Database device screenshot error");
            }
        });

        deviceAngleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tvAngle.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(tag, "Database device angle error");
            }
        });

        btLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceAngleRef.setValue(Float.parseFloat((String) tvAngle.getText()) + 10);
            }
        });

        btRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Float.parseFloat(tvAngle.getText().toString()) > 0) {
                    deviceAngleRef.setValue(Float.parseFloat((String) tvAngle.getText()) - 10);
                }
            }
        });
    }
}
