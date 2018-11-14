package com.example.bjtu.fitbit_steamr;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.os.Looper;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    public String userDevices = null;
    public String userActivities = null;
    public String token = null;
    public Dialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //绑定button按钮
        final Button btn_authority = this.findViewById(R.id.btn_authority);
        Button btn_getData = this.findViewById(R.id.btn_getData);
        Button btn_test = this.findViewById(R.id.btn_test);
        Button btn_streamr = this.findViewById(R.id.btn_streamr1);
        final EditText txt_appID = this.findViewById(R.id.txt_appID);
        final EditText txt_token = this.findViewById(R.id.txt_token);

//        final TextView textView_test = this.findViewById(R.id.text_test);


        //监听button事件
        btn_authority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.fitbit.com/oauth2/authorize?response_type=token&client_id="+txt_appID.getText()+"&redirect_uri=https%3A%2F%2Fwww.streamr.com%2F&scope=activity%20heartrate%20location%20nutrition%20profile%20settings%20sleep%20social%20weight&expires_in=604800";
                final Request request = new Request.Builder().url(url).get().build();

                //OkHttpUtil.enqueue(request, callback);
                CustomTabsHelper.openUrl(MainActivity.this,url);

            }
        });

        btn_streamr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String token_orign = txt_token.getText().toString().trim();

                if (!isCompleteUrl(token_orign)) {
                    Toast tot = Toast.makeText(
                            MainActivity.this,
                            "Please input the url that copied in the browser!",
                            Toast.LENGTH_LONG);
                    tot.show();
                    return;
                }
                token = token_orign.split("&")[0].split("=")[1];
                if (token != null) {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, StreamActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putString("token", token);

                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
        btn_getData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String token_orign = txt_token.getText().toString().trim();

                if (!isCompleteUrl(token_orign)) {
                    Toast tot = Toast.makeText(
                            MainActivity.this,
                            "Please input the url that copied in the browser!",
                            Toast.LENGTH_LONG);
                    tot.show();
                    return;
                }
                dialog = DialogUtils.createLoadingDialog(MainActivity.this, "loading");
                token = token_orign.split("&")[0].split("=")[1];
                if (token != null) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date(System.currentTimeMillis());
                    String url_devices = "https://api.fitbit.com/1/user/-/devices.json";
                    String url_activities = "https://api.fitbit.com/1/user/-/activities/date/"+simpleDateFormat.format(date)+".json";

                    Callback callback_devices = new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            DialogUtils.closeDialog(dialog);
                            Looper.prepare();
                            Toast tot = Toast.makeText(
                                    MainActivity.this,
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
                            if (userDevices!=null && userActivities!=null) {
                                System.out.println("data not null!");
                                Intent intent = new Intent();
                                intent.setClass(MainActivity.this, StreamActivity.class);

                                Bundle bundle = new Bundle();
                                bundle.putString("userDevices", userDevices);
                                bundle.putString("userActivities", userActivities);

                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        }
                    };
                    Callback callback_activities = new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            Looper.prepare();
                            Toast tot = Toast.makeText(
                                    MainActivity.this,
                                    "Failed to get user activities!",
                                    Toast.LENGTH_LONG);
                            tot.show();
                            Looper.loop();
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            userActivities = response.body().string();
                            System.out.println("data activities:::"+userActivities);
                            if (userDevices!=null && userActivities!=null) {
                                System.out.println("data not null!");
                                Intent intent = new Intent();
                                intent.setClass(MainActivity.this, StreamActivity.class);

                                Bundle bundle = new Bundle();
                                bundle.putString("userDevices", userDevices);
                                bundle.putString("userActivities", userActivities);

                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        }
                    };
                    sendRequest(url_devices, callback_devices);
                    sendRequest(url_activities, callback_activities);

                }
            }
        });
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date(System.currentTimeMillis());
                String token_orign = txt_token.getText().toString().trim();
                if (!isCompleteUrl(token_orign)) {
                    Toast tot = Toast.makeText(
                            MainActivity.this,
                            "Please input the url that copied in the browser!",
                            Toast.LENGTH_LONG);
                    tot.show();
                    return;
                }
                if (!token_orign.equals(""))
                    token = token_orign.split("&")[0].split("=")[1];
                /*Toast tot = Toast.makeText(
                        MainActivity.this,
                        token,
                        Toast.LENGTH_LONG);
                tot.show();*/
                System.out.println("date::"+simpleDateFormat.format(date));
                String url = "https://api.fitbit.com/1/user/-/activities/date/"+simpleDateFormat.format(date)+".json";
                Request.Builder requestBuilder = new Request.Builder().url(url).addHeader("Authorization","Bearer "+token);
                final Request request = requestBuilder.build();
                System.out.println("request::"+request.header("Authorization"));

                OkHttpUtil.enqueue(request, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Toast tot = Toast.makeText(
                                MainActivity.this,
                                "Test::Failed to get user activities!",
                                Toast.LENGTH_LONG);
                        tot.show();
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {

                        //textView_test.setText(response.body().string().toString());
                    }
                });
            }
        });

    }

    protected void getUserDevices(String token) {

    }

    protected void getUserActivities(String token) {

    }

    public static boolean isCompleteUrl(String text) {
        Pattern p =  Pattern.compile("((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher  = p.matcher(text);
        return matcher.find();
    }

    protected void sendRequest(String url, Callback callback) {
        Request.Builder requestBuilder = new Request.Builder().url(url).addHeader("Authorization","Bearer "+token);
        final Request request = requestBuilder.build();
        OkHttpUtil.enqueue(request, callback);
    }
}
