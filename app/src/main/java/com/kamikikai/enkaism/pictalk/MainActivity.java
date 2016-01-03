package com.kamikikai.enkaism.pictalk;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.kamikikai.enkaism.pictalk.data.api.PicTalkService;
import com.kamikikai.enkaism.pictalk.data.api.ServiceGenerator;
import com.kamikikai.enkaism.pictalk.data.api.model.Message;
import com.kamikikai.enkaism.pictalk.data.api.model.PicTalk;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {

  @Bind(R.id.listView) ListView listView;
  private MessageAdapter adapter;
  private PicTalkService service;
  private MediaPlayer mediaPlayer;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    service = ServiceGenerator.createService(PicTalkService.class);
    mediaPlayer = new MediaPlayer();
    adapter = new MessageAdapter(getApplicationContext(), null);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PicTalk picTalk = (PicTalk) adapter.getItem(position);
        try {
          mediaPlayer.reset();
          mediaPlayer.setDataSource(getApplicationContext(),
              Uri.parse(Const.STORAGE_BASE_URL + picTalk.getVoiceUrl()));
          mediaPlayer.prepare();
          mediaPlayer.start();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
    getMessage();
  }

  private void getMessage() {
    adapter.clear();
    service.getMessage().enqueue(new Callback<Message>() {
      @Override public void onResponse(Response<Message> response, Retrofit retrofit) {
        if (response.isSuccess()) {
          if (response.body().getCount() == 0) {
            Toast.makeText(getApplicationContext(), "no contents", Toast.LENGTH_LONG).show();
          } else {
            List<PicTalk> picTalks = new ArrayList<>();
            for (List<String> paths : response.body().getPath()) {
              picTalks.add(new PicTalk(paths.get(0), paths.get(1)));
            }
            adapter.addAll(picTalks);
            Toast.makeText(getApplicationContext(), response.body().getPath().get(0).get(0),
                Toast.LENGTH_LONG).show();
          }
        } else {
          try {
            Toast.makeText(getApplicationContext(), response.errorBody().string(),
                Toast.LENGTH_LONG).show();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

      @Override public void onFailure(Throwable t) {
        Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
      }
    });
  }

  @OnClick(R.id.sendButton) public void clickSendButton() {
    startActivity(new Intent(this, PostActivity.class));
  }
}
