package com.kamikikai.enkaism.pictalk.data.api;

import com.squareup.okhttp.RequestBody;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;

/**
 * Created by enkaism on 1/3/16.
 */
public interface PicTalkService {
  @GET("/message") 

  @Multipart
  @POST("/upload") Call<String> upload(
      @Part("pic\"; filename=\"pic.jpg\" ") RequestBody file1,
      @Part("talk\"; filename=\"voice.mp4\" ") RequestBody file2);
}
