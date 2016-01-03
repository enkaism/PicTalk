package com.kamikikai.enkaism.pictalk.data.api;

import com.squareup.okhttp.OkHttpClient;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by enkaism on 1/3/16.
 */
public class ServiceGenerator {

  public static final String API_BASE_URL = "https://evening-wildwood-2161.herokuapp.com";

  private static OkHttpClient httpClient = new OkHttpClient();
  private static Retrofit.Builder builder =
      new Retrofit.Builder()
          .baseUrl(API_BASE_URL)
          .addConverterFactory(GsonConverterFactory.create());

  public static <S> S createService(Class<S> serviceClass) {
    Retrofit retrofit = builder.client(httpClient).build();
    return retrofit.create(serviceClass);
  }
}