package com.gotop.sanqinapp;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtmp.RtmpCamera2;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.Camera2Base}
 * {@link com.pedro.rtplibrary.rtmp.RtmpCamera2}
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SurfaceModeRtmpActivity extends AppCompatActivity
    implements ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback {

  private RtmpCamera2 rtmpCamera2;
  private Button button;
  private Button bRecord;
  private EditText etUrl;

  private String currentDateAndTime = "";
  private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
      + "/rtmp-rtsp-stream-client-java");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_example);
    SurfaceView surfaceView = findViewById(R.id.surfaceView);
    button = findViewById(R.id.b_start_stop);
    button.setOnClickListener(this);
    bRecord = findViewById(R.id.b_record);
    bRecord.setOnClickListener(this);
    Button switchCamera = findViewById(R.id.switch_camera);
    switchCamera.setOnClickListener(this);
    etUrl = findViewById(R.id.et_rtp_url);
//    etUrl.setHint(R.string.hint_rtmp);
    rtmpCamera2 = new RtmpCamera2(surfaceView, this);
    surfaceView.getHolder().addCallback(this);
  }

  @Override
  public void onConnectionSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(SurfaceModeRtmpActivity.this, "Connection success", Toast.LENGTH_SHORT)
            .show();
      }
    });
  }

  @Override
  public void onConnectionFailedRtmp(final String reason) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(SurfaceModeRtmpActivity.this, "Connection failed. " + reason,
            Toast.LENGTH_SHORT).show();
        rtmpCamera2.stopStream();
        button.setText(R.string.start_button);
      }
    });
  }

  @Override
  public void onDisconnectRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(SurfaceModeRtmpActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthErrorRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(SurfaceModeRtmpActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(SurfaceModeRtmpActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.b_start_stop:
        if (!rtmpCamera2.isStreaming()) {
          if (rtmpCamera2.isRecording()
              || rtmpCamera2.prepareAudio() && rtmpCamera2.prepareVideo()) {
            button.setText(R.string.stop_button);
            rtmpCamera2.startStream(etUrl.getText().toString());
          } else {
            Toast.makeText(this, "Error preparing stream, This device cant do it",
                Toast.LENGTH_SHORT).show();
          }
        } else {
          button.setText(R.string.start_button);
          rtmpCamera2.stopStream();
        }
        break;
      case R.id.switch_camera:
        try {
          rtmpCamera2.switchCamera();
        } catch (CameraOpenException e) {
          Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        break;
      case R.id.b_record:
        if (!rtmpCamera2.isRecording()) {
          try {
            if (!folder.exists()) {
              folder.mkdir();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            currentDateAndTime = sdf.format(new Date());
            if (!rtmpCamera2.isStreaming()) {
              if (rtmpCamera2.prepareAudio() && rtmpCamera2.prepareVideo()) {
                rtmpCamera2.startRecord(
                    folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                bRecord.setText(R.string.stop_record);
                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
              } else {
                Toast.makeText(this, "Error preparing stream, This device cant do it",
                    Toast.LENGTH_SHORT).show();
              }
            } else {
              rtmpCamera2.startRecord(folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
              bRecord.setText(R.string.stop_record);
              Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
            }
          } catch (IOException e) {
            rtmpCamera2.stopRecord();
            bRecord.setText(R.string.start_record);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
          }
        } else {
          rtmpCamera2.stopRecord();
          bRecord.setText(R.string.start_record);
          Toast.makeText(this,
              "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
              Toast.LENGTH_SHORT).show();
          currentDateAndTime = "";
        }
        break;
      default:
        break;
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {

  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    rtmpCamera2.startPreview();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    if (rtmpCamera2.isRecording()) {
      rtmpCamera2.stopRecord();
      bRecord.setText(R.string.start_record);
      Toast.makeText(this,
          "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
          Toast.LENGTH_SHORT).show();
      currentDateAndTime = "";
    }
    if (rtmpCamera2.isStreaming()) {
      rtmpCamera2.stopStream();
      button.setText(getResources().getString(R.string.start_button));
    }
    rtmpCamera2.stopPreview();
  }
}