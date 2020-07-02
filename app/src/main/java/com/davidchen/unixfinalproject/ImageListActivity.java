package com.davidchen.unixfinalproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;



public class ImageListActivity extends AppCompatActivity {

    final String tag = "ImageList.java";
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference imgRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private ListView lvImage;
    private ListAdapter adapter;
    private ArrayList<Screenshot> s = new ArrayList<>();
    private ConstraintLayout popupView;
    private TextView tvPopupName;
    private TextView tvPopupTime;
    private ImageView ivPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);
        lvImage = findViewById(R.id.image_listview);
        popupView = findViewById(R.id.popup_view);
        tvPopupName = findViewById(R.id.popup_name_textview);
        tvPopupTime = findViewById(R.id.popup_time_textview);
        ivPopup = findViewById(R.id.popup_imageview);

        Query query = db.getReference().child("device").child("images").orderByChild("createAt").limitToLast(5);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                s.clear();
                lvImage.setAdapter(adapter);
                Log.w(tag, "children count:" + String.valueOf(dataSnapshot.getChildrenCount()));
                for( DataSnapshot d: dataSnapshot.getChildren()) {
                    String name = d.child("name").getValue().toString();
                    String createAt = d.child("createAt").getValue().toString();
                    createAt = createAt.substring(0, createAt.indexOf("."));
                    String url = d.child("url").getValue().toString();
                    Log.w(tag, d.child("name").getValue().toString());
                    s.add(0, new Screenshot(name, createAt, url));
                }
                adapter = new ListAdapter(ImageListActivity.this, s);
                lvImage.setAdapter(adapter);
                lvImage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Screenshot temp = (Screenshot) parent.getItemAtPosition(position);
                        Log.w(tag,"name: " + temp.getName());
                        tvPopupName.setText(temp.getName());
                        tvPopupTime.setText(temp.getDateTime());
                        try {
                            StorageReference imgRef = storageRef.child("images").child(temp.getName());

                            final File file = File.createTempFile("image", "jpg");
                            imgRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    String s = file.getName();
                                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                    ivPopup.setImageBitmap(bitmap);
                                    popupView.setVisibility(View.VISIBLE);
                                    lvImage.setVisibility(View.INVISIBLE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ImageListActivity.this, "Image failed to load", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupView.setVisibility(View.GONE);
                lvImage.setVisibility(View.VISIBLE);
            }
        });

    }
    private class ListAdapter extends BaseAdapter {
        private LayoutInflater myInflater;
        private ArrayList<Screenshot> screenshots;

        public ListAdapter(Context context, ArrayList<Screenshot> screenshots) {
            this.myInflater = LayoutInflater.from(context);
            this.screenshots = screenshots;
        }

        @Override
        public int getCount() {
            return screenshots.size();
        }

        @Override
        public Object getItem(int position) {
            return screenshots.get(position);
        }

        @Override
        public long getItemId(int position) {
            return screenshots.indexOf(getItem(position));
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = myInflater.inflate(R.layout.screenshots_item, null);
                holder = new ViewHolder(
                        (TextView) convertView.findViewById(R.id.screenshots_name_textview),
                        (TextView) convertView.findViewById(R.id.screenshots_datetime_textview),
                        (ImageView) convertView.findViewById(R.id.screenshot_imageview)
                );
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            Screenshot screenshot = (Screenshot)getItem(position);
            holder.tvName.setText(screenshot.getName());
            holder.tvDateTime.setText(screenshot.getDateTime());

            try {
                StorageReference imgRef = storageRef.child("images").child(screenshot.getName());

                final File file = File.createTempFile("image", "jpg");
                final ViewHolder finalHolder = holder;
                imgRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        String s = file.getName();
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        finalHolder.iv.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ImageListActivity.this, "Image failed to load", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tvName;
            TextView tvDateTime;
            ImageView iv;

            public ViewHolder(TextView tvName, TextView tvDateTime, ImageView iv) {
                this.tvName = tvName;
                this.tvDateTime = tvDateTime;
                this.iv = iv;
            }
        }
    }
}