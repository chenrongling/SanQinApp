package com.gotop.sanqinapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gotop.sanqinapp.msg.SanQinClient;

import java.util.Map;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView tv_video;
    private Dialog dialog;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        tv_video = (TextView)this.findViewById(R.id.tv_video);
        tv_video.setOnClickListener(this);

/*      TextView testView = (TextView)findViewById(R.id.jsonInfo);
        String testInfo = getIntent().getStringExtra("testInfo");
        String errorcode = getIntent().getStringExtra("errorcode");
        String push_url = getIntent().getStringExtra("push_url");
        String poll_url = getIntent().getStringExtra("poll_url");
        String url_time = getIntent().getStringExtra("url_time");


        testView.setText("testInfo:"+testInfo+"\nerrorcode:"+errorcode+
                         "\npush_url:"+push_url+"\npoll_url:"+poll_url+
                         "\nurl_time"+url_time);*/
        //请求视频通话diagog设置
        dialog = new Dialog(VideoActivity.this, R.style.progress_dialog);
        dialog.setContentView(R.layout.process_dialog);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
        msg.setText("请稍后...");
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case 222:
                    dialog.dismiss();
                    startActivity(intent);
                    break;
                case 333:
                    break;
            }
        }
    };
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_video:
                dialog.show();
                new Thread(){
                    @Override
                    public void run(){
                        Message msg = new Message();
                        SanQinClient client = SanQinClient.getInstance();
                        Map<String, String> result = client.request(null);
                        if(result != null)
                        {

                            msg.what = 222;
                            intent = new Intent(VideoActivity.this, VideoCallActivity2.class);
                            intent.putExtra("pushUrl", result.get("push_url"));
                            intent.putExtra("pullUrl", result.get("pull_url"));
                            System.out.println("result--------------------------"+result.get("errorcode"));
                            System.out.println("push_url--------------------------"+result.get("push_url"));
                            System.out.println("pull_url--------------------------"+result.get("pull_url"));
                            System.out.println("url_time--------------------------"+result.get("url_time"));

                            handler.obtainMessage(222).sendToTarget();

                        }
                        {
                            msg.what = 333;
                            handler.obtainMessage(333).sendToTarget();

                        }
                    }
                }.start();
                break;
            default:
                break;
        }
    }
}
