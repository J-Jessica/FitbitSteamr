package com.example.bjtu.fitbit_steamr;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;


import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class StreamActivity extends AppCompatActivity {

    static Toast toast = null;
    public Dialog dialog = null;
    public String userDevices;
    public String userActivities = null;
    String token = null;
    String streamID = null;
    String key = null;

    ImageView img1 = null;
    ImageView img2 = null;
    ImageView img3 = null;

    Animation animation = null;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        Bundle bundle = this.getIntent().getExtras();

//        final String userDevices = bunde.getString("userDevices");
//        final String userActivities = bunde.getString("userActivities");
        token = bundle.getString("token");

        /*TextView data = this.findViewById(R.id.data);
        data.setText(userDevices+"------------------"+userActivities);*/

        Button btn_stream = this.findViewById(R.id.btn_stream);
        Button btn_stop = this.findViewById(R.id.btn_stop);

        final TextView txt_streamID = this.findViewById(R.id.txt_streamID);
        final TextView txt_key = this.findViewById(R.id.txt_key);

        img1 = this.findViewById(R.id.img1);
        img2 = this.findViewById(R.id.img2);
        img3 = this.findViewById(R.id.img3);

        animation = AnimationUtils.loadAnimation(StreamActivity.this, R.anim.img_animation);
        LinearInterpolator lin = new LinearInterpolator();//设置动画匀速运动
        animation.setInterpolator(lin);

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img1.clearAnimation();
                img2.clearAnimation();
                img3.clearAnimation();
                timer.cancel();
            }
        });


        btn_stream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                streamID = txt_streamID.getText().toString();
                key = txt_key.getText().toString();
                if (streamID.equals("")) {
                    if(toast!=null){
                        toast.setText("Error:Stream ID is null!");
                    }else{
                        toast= Toast.makeText(StreamActivity.this, "Error:Stream ID is null!", Toast.LENGTH_SHORT);
                    }
                    toast.show();
                } else if (key.equals("")) {
                    if(toast!=null){
                        toast.setText("Error:Key is null!");
                    }else{
                        toast= Toast.makeText(StreamActivity.this, "Error:Key is null!", Toast.LENGTH_SHORT);
                    }
                    toast.show();
                } else {
                    dialog = DialogUtils.createLoadingDialog(StreamActivity.this, "loading");
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            getData();
                        }
                    },0,2000);//每隔一秒使用handler发送一下消息,也就是每隔一秒执行一次,一直重复执行

                }
            }
        });
    }

    protected void start() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                getData();
            }
        };
    }

    protected void stream() {

        String header = "token " + key;
        String url = "https://www.streamr.com/api/v1/streams/"+streamID+"/data";

        //String userDevices = getUserDevices();
       // String userActivities = getUserActivities();
        if (userDevices == null)
            userDevices = "The Data not update!";
        if (userActivities == null)
            userActivities = "The Data not update!";
        Callback callback = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                DialogUtils.closeDialog(dialog);
                Looper.prepare();
                Toast tot = Toast.makeText(
                        StreamActivity.this,
                        "Failed to upload data to Streamr!",
                        Toast.LENGTH_LONG);
                tot.show();
                Looper.loop();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                DialogUtils.closeDialog(dialog);
                img1.startAnimation(animation);
                img2.startAnimation(animation);
                img3.startAnimation(animation);
                Looper.prepare();
                Toast tot = Toast.makeText(
                        StreamActivity.this,
                        "Succeed to upload data to Streamr!",
                        Toast.LENGTH_LONG);
                tot.show();
                Looper.loop();
            }
        };
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("User Devices", userDevices);
            jsonObject.put("User Activities", userActivities);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpUtil.httpPost(url, jsonObject.toString(), callback, header);
    }

    protected String getUserDevices() {
        String url_devices = "https://api.fitbit.com/1/user/-/devices.json";
        userDevices = null;
        Callback callback_devices = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                DialogUtils.closeDialog(dialog);
                Looper.prepare();
                Toast tot = Toast.makeText(
                        StreamActivity.this,
                        "Failed to get user devices!",
                        Toast.LENGTH_LONG);
                tot.show();
                Looper.loop();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                DialogUtils.closeDialog(dialog);
                userDevices = response.body().string();
                System.out.println("data devices:::"+userDevices);
                if (userDevices != null && userActivities != null)
                    stream();
            }
        };
        sendRequest(url_devices, callback_devices);
        //System.out.println("data devices:::"+userDevices);
        return userDevices;
    }

    protected String getUserActivities() {
        userActivities = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        String url_activities = "https://api.fitbit.com/1/user/-/activities/date/"+simpleDateFormat.format(date)+".json";
        Callback callback_activities = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Looper.prepare();
                Toast tot = Toast.makeText(
                        StreamActivity.this,
                        "Failed to get user activities!",
                        Toast.LENGTH_LONG);
                tot.show();
                Looper.loop();
            }


            @Override
            public void onResponse(Response response) throws IOException {
                userActivities = response.body().string();
                System.out.println("data activities:::"+userActivities);
                if (userDevices != null && userActivities != null)
                    stream();
            }
        };
        sendRequest(url_activities, callback_activities);
        //System.out.println("data activities:::"+userActivities);
        return userActivities;
    }

    protected void getData() {
        userActivities = null;
        userDevices = null;

        getUserActivities();
        getUserDevices();

    }

    protected void sendRequest(String url, Callback callback) {
        Request.Builder requestBuilder = new Request.Builder().url(url).addHeader("Authorization","Bearer "+token);
        final Request request = requestBuilder.build();
        OkHttpUtil.enqueue(request, callback);
    }

}
