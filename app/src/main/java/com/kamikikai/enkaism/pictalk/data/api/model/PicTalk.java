package com.kamikikai.enkaism.pictalk.data.api.model;

/**
 * Created by enkaism on 1/4/16.
 */
public class PicTalk {
  private String imageUrl;
  private String voiceUrl;

  public PicTalk(String imageUrl, String voiceUrl) {
    this.imageUrl = imageUrl;
    this.voiceUrl = voiceUrl;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getVoiceUrl() {
    return voiceUrl.replace(".m4a", ".mp3");
  }
}
