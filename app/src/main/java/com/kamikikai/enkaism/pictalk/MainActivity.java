package com.kamikikai.enkaism.pictalk;

import android.app.Activity;
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
import java.util.HashMap;
import java.util.Map;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends Activity {
  private static final String PICTURE_PATH = "PICTURE_PATH";
  private static final String VOICE_PATH = "VOICE_PATH";

  @Bind(R.id.textureView) AutoFitTextureView textureView;
  @Bind(R.id.imageView) ImageView imageView;
  @Bind(R.id.shutterButton) Button shutterButton;
  private Camera2StateMachine camera2;
  private MediaRecorder recorder;
  private Map<String, String> filePaths = new HashMap<>();
  private byte[] pictureContent;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    camera2 = new Camera2StateMachine();
  }

  @Override protected void onResume() {
    super.onResume();
    camera2.open(this, textureView);
  }

  @Override protected void onPause() {
    camera2.close();
    super.onPause();
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && imageView.getVisibility() == View.VISIBLE) {
      textureView.setVisibility(View.VISIBLE);
      imageView.setVisibility(View.INVISIBLE);
      return false;
    }
    return super.onKeyDown(keyCode, event);
  }

  private void uploadFiles() {
    PicTalkService service = ServiceGenerator.createService(PicTalkService.class);

    File voiceFile = new File(filePaths.get(VOICE_PATH));

    Call<String> call =
        service.upload(RequestBody.create(MediaType.parse("multipart/form-data"), pictureContent),
            RequestBody.create(MediaType.parse("multipart/form-data"), voiceFile));

    call.enqueue(new Callback<String>() {
      @Override public void onResponse(Response<String> response, Retrofit retrofit) {
        Log.v("Upload", "success");
      }

      @Override public void onFailure(Throwable t) {
        Log.e("Upload", t.getMessage());
      }
    });
  }

  @OnClick(R.id.shutterButton) public void onClickShutter(View view) {
    if (shutterButton.getTag().equals(getString(R.string.picture))) {
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
          //try {
          //  ByteArrayOutputStream baos = new ByteArrayOutputStream();
          //  FileOutputStream fos = openFileOutput("test.png", Context.MODE_PRIVATE);
          //
          //  bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
          //  fos.flush();
          //  fos.close();
          //} catch (FileNotFoundException e) {
          //  e.printStackTrace();
          //} catch (IOException e) {
          //  e.printStackTrace();
          //}
          imageView.setVisibility(View.VISIBLE);
          textureView.setVisibility(View.INVISIBLE);
          shutterButton.setTag(getString(R.string.recording));
        }
      });
    } else if (shutterButton.getTag().equals(getString(R.string.recording))) {
      Log.d("log", "start recording");
      recorder = new MediaRecorder();
      recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
      recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

      //保存先
      filePaths.put(VOICE_PATH, Environment.getExternalStorageDirectory() + "/audio.mp4");
      recorder.setOutputFile(filePaths.get(VOICE_PATH));

      //録音準備＆録音開始
      try {
        recorder.prepare();
      } catch (Exception e) {
        e.printStackTrace();
      }
      recorder.start();   //録音開始
      shutterButton.setTag(getString(R.string.stop));
      shutterButton.setText(R.string.stop);
    } else if (shutterButton.getTag().equals(getString(R.string.stop))) {
      recorder.stop();
      recorder.reset();   //オブジェクトのリセット
      //release()前であればsetAudioSourceメソッドを呼び出すことで再利用可能
      recorder.release(); //Recorderオブジェクトの解放
      uploadFiles();
    }
  }
}
