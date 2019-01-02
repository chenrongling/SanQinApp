package com.gotop.sanqinapp;

import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gotop.sanqinapp.msg.SanQinClient;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtmp.RtmpCamera2;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.Camera2Base}
 * {@link com.pedro.rtplibrary.rtmp.RtmpCamera2}
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VideoCallActivity2 extends AppCompatActivity
        implements ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback {
    private static final String TAG = "VideoCallActivity2";
    //远端的视图
    private SurfaceView surfaceViewRemote;
    //本地的视图
    private SurfaceView surfaceViewLocal;

    private RtmpCamera2 rtmpCamera2;
    private View hangupCallView;
    private View switchCameraView;
    private String pullUrl; //拉流地址
    private String pushUrl; //推流地址

    private boolean pushRead = false;
    private boolean pullRead = false;

    //默认本地视图的状态
    private boolean mSate = true;
    //默认本地视频宽度  90dp
    private int defaultLocalWidth = 120;
    private int defaultLocalHeight;
    //默认本地视频边距  20dp
    private int defaultLocalMargin = 20;

    //本地视图大小
    private RelativeLayout rlRemote;
    private RelativeLayout rlLocal;

    //拨打电话状态栏
    private LinearLayout llCallContainer;

    private String currentDateAndTime = "";

    public static final int START_PULL_STREAM = 1;
    public static final int STOP_PULL_STREAM = 2;
    public static final int START_PUSH_STREAM = 3;
    public static final int STOP_PUSH_STREAM = 4;
    public static final int REQUEST_URL = 5;

    public static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video_call_activity2);

        //如果判断有刘海屏不让填充到状态栏
        if (DisplayUtil.hasNotchScreen(this)) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case START_PULL_STREAM:
                        startPullStream(pullUrl);
                        break;
                    case STOP_PULL_STREAM:
                        stopPullStream();
                        break;
                    case START_PUSH_STREAM:
                        startPushStream(pullUrl);
                        break;
                    case STOP_PUSH_STREAM:
                        stopPushStream();
                        break;
                    case REQUEST_URL:
                        startRequestUrl();
                        break;
                }
                super.handleMessage(msg);
            }
        };

        hangupCallView = findViewById(R.id.tv_hangup_call);
        hangupCallView.setOnClickListener(this);

        switchCameraView = findViewById(R.id.tv_switch_camera);
        switchCameraView.setOnClickListener(this);

        llCallContainer = (LinearLayout) findViewById(R.id.ll_call_container);
//        rlRemote = (RelativeLayout) findViewById(R.id.rl_remote);
        surfaceViewRemote = (SurfaceView) findViewById(R.id.surfaceview_remote);
//        rlLocal = (RelativeLayout) findViewById(R.id.rl_local);
        surfaceViewLocal = (SurfaceView) findViewById(R.id.surfaceview_local);
        surfaceViewLocal.setZOrderOnTop(true);
        surfaceViewLocal.setZOrderMediaOverlay(true);

        rtmpCamera2 = new RtmpCamera2(surfaceViewLocal, this);
        surfaceViewLocal.getHolder().addCallback(this);
        surfaceViewRemote.getHolder().addCallback(this);

        pushUrl = getIntent().getStringExtra("pushUrl");
        pullUrl = getIntent().getStringExtra("pullUrl");

    }

    private void read() {
        if (pullRead && pushRead) {
            handler.obtainMessage(REQUEST_URL).sendToTarget();
        }
    }

    private void startRequestUrl() {
//        Map<String, String> result = SanQinClient.getInstance().request(123321L);
        System.out.println("startPushStream:" + startPushStream(pushUrl));
        System.out.println("startPullStream：" + startPullStream(pullUrl));
    }

    @Override
    protected void onDestroy() {
        stopPullStream();
        stopPushStream();

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VideoCallActivity2.this, "Connection success", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    public void onConnectionFailedRtmp(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VideoCallActivity2.this, "Connection failed. " + reason,
                        Toast.LENGTH_SHORT).show();
                rtmpCamera2.stopStream();
            }
        });
    }

    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VideoCallActivity2.this, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthErrorRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VideoCallActivity2.this, "Auth error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VideoCallActivity2.this, "Auth success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 开始推流
    private boolean startPushStream(String url) {
        if (!rtmpCamera2.isStreaming()) { // start
            if (rtmpCamera2.prepareAudio() && rtmpCamera2.prepareVideo()) {
                rtmpCamera2.startStream(url);
                return true;
            } else {
                Toast.makeText(this, "Error preparing stream, This device cant do it",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return false;
    }

    // 结束推流
    private void stopPushStream() {
        if (rtmpCamera2 != null) {
            if (rtmpCamera2.isStreaming()) {
                rtmpCamera2.stopStream();
            }
        }
    }

    // 开始拉流
    private boolean startPullStream(String url) {
        surfaceViewLocal.setBackground(null);
        initMediaPlay(surfaceViewRemote.getHolder(), url);
        return true;
    }

    // 结束拉流
    private void stopPullStream() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_call_quiet:
                Toast.makeText(getApplicationContext(), "按钮：切换语音",Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_hangup_call:
                Toast.makeText(getApplicationContext(), "按钮：挂断",Toast.LENGTH_SHORT).show();
                break;
            case R.id.switch_camera:
                try {
                    rtmpCamera2.switchCamera();
                } catch (CameraOpenException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (surfaceViewLocal.getHolder().equals(holder)) {
            pushRead = true;
        } else if (surfaceViewRemote.getHolder().equals(holder)) {
            pullRead = true;
        }
        read();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (surfaceViewLocal.getHolder().equals(holder)) {
            System.out.println("surfaceViewLocal surfaceChanged");
            rtmpCamera2.startPreview();
        } else if (surfaceViewRemote.getHolder().equals(holder)) {

        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (surfaceViewLocal.getHolder().equals(holder)) {
            if (rtmpCamera2.isStreaming()) {
                rtmpCamera2.stopStream();
            }
            rtmpCamera2.stopPreview();
        }
    }

    /**
     * 大小视图切换 （小视图在前面、大视图在后面）
     *
     * @param sourcView  之前相对布局大小
     * @param beforeview 之前surfaceview
     * @param detView    之后相对布局大小
     * @param afterview  之后的surfaceview
     */
    private void zoomOpera(View sourcView, SurfaceView beforeview,
                           View detView, SurfaceView afterview) {
        RelativeLayout paretview = (RelativeLayout) sourcView.getParent();
        paretview.removeView(detView);
        paretview.removeView(sourcView);

        //设置远程大视图RelativeLayout 的属性
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        params1.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        beforeview.setZOrderMediaOverlay(true);
        beforeview.getHolder().setFormat(PixelFormat.TRANSPARENT);
        sourcView.setLayoutParams(params1);

        //设置本地小视图RelativeLayout 的属性
        params1 = new RelativeLayout.LayoutParams(defaultLocalWidth, defaultLocalHeight);
        params1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params1.setMargins(0, defaultLocalMargin, defaultLocalMargin, 0);
        //在调用setZOrderOnTop(true)之后调用了setZOrderMediaOverlay(true)  遮挡问题
        afterview.setZOrderOnTop(true);
        afterview.setZOrderMediaOverlay(true);
        afterview.getHolder().setFormat(PixelFormat.TRANSPARENT);
        detView.setLayoutParams(params1);

        paretview.addView(sourcView);
        paretview.addView(detView);
    }

    /**
     * 改变转态烂的显示隐藏
     */
    protected void changeStatus() {
        if (llCallContainer.getVisibility() == View.GONE) {
            llCallContainer.setVisibility(View.VISIBLE);
            showStatusBar();
            postDelayedCloseBtn();
        } else {
            llCallContainer.setVisibility(View.GONE);
            hideStatusBar();
        }
    }

    /**
     * 隐藏标题栏
     */
    private void hideStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    /**
     * 显示标题栏
     */
    private void showStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    /**
     * 开启取消延时动画
     */
    private void postDelayedCloseBtn() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                llCallContainer.setVisibility(View.GONE);
                hideStatusBar();
            }
        }, 5000);
    }


    private IjkMediaPlayer mMediaPlayer;
    private long mStartLocation = 0;
    private boolean isLive = true;
    // ---- play
    /**
     * 初始化播放器
     */
    private void initMediaPlay(SurfaceHolder sf, String url) {
        mMediaPlayer = new IjkMediaPlayer();
        mMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_INFO);
        //硬解码：1、打开，0、关闭
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        //软解码：1、打开，0、关闭
        //mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "videotoolbox", 0);

        //rtsp设置 https://ffmpeg.org/ffmpeg-protocols.html#rtsp
        //mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
        //mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");

        //udp
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 10000);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316);
        //播放前的探测时间
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 50000);
        //播放前的探测Size，默认是1M, 改小一点会出画面更快
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probsize", 4096);

        if (isLive) {
            // 最大缓存大小是3秒，可以依据自己的需求修改
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max_cached_duration", 3000);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 1);  // 无限读
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "packet-buffering", 0);//  关闭播放器缓冲
        } else {
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max_cached_duration", 0);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 0);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "packet-buffering", 1);
        }

        mMediaPlayer.setOnPreparedListener(new IjkMediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                if (mStartLocation > 0) {
                    iMediaPlayer.seekTo(mStartLocation);
                } else {
                    iMediaPlayer.start();
                }
            }
        });
        mMediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                return false;
            }
        });
        mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(IMediaPlayer iMediaPlayer) {
                iMediaPlayer.start();
            }
        });
        mMediaPlayer.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
            }
        });
        mMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                return false;
            }
        });
        try {
            mMediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setDisplay(sf);
        mMediaPlayer.prepareAsync();
    }
}