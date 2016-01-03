package com.kamikikai.enkaism.pictalk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.kamikikai.enkaism.pictalk.data.api.model.PicTalk;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enkaism on 1/4/16.
 */
public class MessageAdapter extends BaseAdapter {

  private LayoutInflater inflater;
  private Context context;
  private List<PicTalk> picTalks;

  public MessageAdapter(Context context, List<PicTalk> picTalks) {
    if (picTalks == null) {
      this.picTalks = new ArrayList<>();
    } else {
      this.picTalks = picTalks;
    }
    this.context = context;
    inflater = LayoutInflater.from(context);
  }

  @Override public int getCount() {
    return picTalks.size();
  }

  @Override public Object getItem(int position) {
    return picTalks.get(position);
  }

  @Override public long getItemId(int position) {
    return 0;
  }

  public void clear() {
    picTalks.clear();
    notifyDataSetChanged();
  }

  public void addAll(List<PicTalk> picTalks) {
    this.picTalks.addAll(picTalks);
    notifyDataSetChanged();
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView != null) {
      holder = (ViewHolder) convertView.getTag();
    } else {
      convertView = inflater.inflate(R.layout.list_message, null);
      holder = new ViewHolder(convertView);
      convertView.setTag(holder);
    }

    PicTalk item = (PicTalk) getItem(position);

    Picasso.with(context).load(Const.STORAGE_BASE_URL + item.getImageUrl()).into(holder.imageView);

    return convertView;
  }

  static class ViewHolder {
    @Bind(R.id.imageView) ImageView imageView;

    public ViewHolder(View view) {
      ButterKnife.bind(this, view);
    }
  }
}
