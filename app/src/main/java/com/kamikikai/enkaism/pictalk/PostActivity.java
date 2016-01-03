package com.kamikikai.enkaism.pictalk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.kamikikai.enkaism.pictalk.camera.AutoFitTextureView;
import com.kamikikai.enkaism.pictalk.camera.Camera2StateMachine;
import com.kamikikai.enkaism.pictalk.data.api.PicTalkService;
import com.kamikikai.enkaism.pictalk.data.api.ServiceGenerator;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import java.io.File;
import java.nio.ByteBuffer;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class PostActivity extends Activity {

  private static final int REQUEST_CAMERA_PERMISSION = 1;
  private static final int REQUEST_RECORDER_PERMISSION = 2;

  @Bind(R.id.textureView) AutoFitTextureView textureView;
  @Bind(R.id.imageView) ImageView imageView;
  @Bind(R.id.shutterButton) Button shutterButton;
  private Camera2StateMachine camera2;
  private MediaRecorder recorder;
  private String voiceFilePath;
  private byte[] pictureContent;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_post);
    ButterKnife.bind(this);
    shutterButton.setTag(R.string.picture);
    camera2 = new Camera2StateMachine();
  }

  @Override protected void onResume() {
    super.onResume();
    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[] { Manifest.permission.CAMERA }, REQUEST_CAMERA_PERMISSION);
    } else {
      camera2.open(this, textureView);
    }
  }

  @Override protected void onPause() {
    camera2.close();
    super.onPause();
  }

  @Override public void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {
    switch (requestCode) {
      case REQUEST_CAMERA_PERMISSION:
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
          Toast.makeText(this, "camera permission does not granted", Toast.LENGTH_SHORT).show();
        }
        break;
      case REQUEST_RECORDER_PERMISSION:
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
          Toast.makeText(this, "permission does not granted", Toast.LENGTH_SHORT).show();
        } else {
          startRecording();
        }
        break;
      default:
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        break;
    }
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && imageView.getVisibility() == View.VISIBLE) {
      textureView.setVisibility(View.VISIBLE);
      imageView.setVisibility(View.INVISIBLE);
      return false;
    }
    return super.onKeyDown(keyCode, event);
  }

  @OnClick(R.id.shutterButton) public void onClickShutter(View view) {
    switch (Integer.parseInt(shutterButton.getTag().toString())) {
      case R.string.picture:
        takePicture();
        break;
      case R.string.recording:
        startRecording();
        break;
      case R.string.stop:
        stopRecording();
        break;
      default:
        break;
    }
  }

  private void takePicture() {
    Log.d("log", "call takePicture()");
    camera2.takePicture(new ImageReader.OnImageAvailableListener() {
      @Override public void onImageAvailable(ImageReader reader) {
        // 撮れた画像をImageViewに貼り付けて表示。
        final Image image = reader.acquireLatestImage();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        pictureContent = new byte[buffer.remaining()];
        buffer.get(pictureContent);
        Bitmap bitmap = BitmapFactory.decodeByteArray(pictureContent, 0, pictureContent.length);
        image.close();

        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
        textureView.setVisibility(View.INVISIBLE);
        shutterButton.setTag(R.string.recording);
        shutterButton.setText(R.string.start_recording);
      }
    });
  }

  private void startRecording() {
    Log.d("log", "call startRecording()");

    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
        != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE },
          REQUEST_RECORDER_PERMISSION);
    } else {
      recorder = new MediaRecorder();
      recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
      recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

      //保存先
      voiceFilePath = Environment.getExternalStorageDirectory() + "/audio.mp4";
      recorder.setOutputFile(voiceFilePath);

      //録音準備＆録音開始
      try {
        recorder.prepare();
      } catch (Exception e) {
        e.printStackTrace();
      }
      recorder.start();   //録音開始
      shutterButton.setTag(R.string.stop);
      shutterButton.setText(R.string.stop_recording);
    }
  }

  private void stopRecording() {
    Log.d("log", "call stopRecording()");
    if (recorder == null) return;
    Log.d("log", "recorder is not null");
    // 録音を終了してファイルをアップロードする
    recorder.stop();
    recorder.reset();
    recorder.release();
    recorder = null;
    uploadFiles();
  }

  private void uploadFiles() {
    PicTalkService service = ServiceGenerator.createService(PicTalkService.class);
    File voiceFile = new File(voiceFilePath);

    Call<String> call =
        service.upload(RequestBody.create(MediaType.parse("multipart/form-data"), pictureContent),
            RequestBody.create(MediaType.parse("multipart/form-data"), voiceFile));

    call.enqueue(new Callback<String>() {
      @Override public void onResponse(Response<String> response, Retrofit retrofit) {
        Toast.makeText(getApplicationContext(), "success Upload", Toast.LENGTH_LONG).show();
        finish();
      }

      @Override public void onFailure(Throwable t) {
        Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
      }
    });
  }
}
