package com.kamikikai.enkaism.pictalk.data.api.model;

import java.util.List;

/**
 * Created by enkaism on 1/3/16.
 */
public class Message {
  private int count;
  private List<List<String>> path;

  public int getCount() {
    return count;
  }

  public List<List<String>> getPath() {
    return path;
  }
}