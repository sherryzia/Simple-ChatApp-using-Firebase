package com.androidadvance.simplechat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.androidadvance.simplechat.MainActivity;
import com.androidadvance.simplechat.R;
import com.androidadvance.simplechat.models.Message;
import com.androidadvance.simplechat.utils.SCUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MyViewHolder> {
  private ArrayList<Message> data;
  private Context mContext;
  private Map<String, Integer> usernameColorMap = new HashMap<>();

  public MainAdapter(MainActivity mContext, ArrayList<Message> data) {
    this.data = data;
  }

  @Override
  public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_main, viewGroup, false);

    MyViewHolder viewHolder = new MyViewHolder(view);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
    Message message = data.get(i);
    String formatted_date = SCUtils.formattedDate(message.getTimestamp());

    // Set username text color
    int usernameColor = getUsernameColor(message.getUsername());
    String usernameTag = "<font color=\"" + String.format("#%06X", (0xFFFFFF & usernameColor)) + "\">" + message.getUsername() + ":</font>";
    myViewHolder.textView_username.setText(Html.fromHtml(usernameTag));

    // Set sender name text and color
    myViewHolder.textView_sender_name.setText(message.getUsername());
    myViewHolder.textView_sender_name.setTextColor(usernameColor);

    // Set timestamp text
    myViewHolder.textView_timestamp.setText("  " + formatted_date);

    if (message.isNotification()) {
      myViewHolder.textView_message.setText(Html.fromHtml("<i><font color=\"#FFBB33\">" + " " + message.getMessage() + "</font></i>"));
    } else {
      String messageText = "<b>" + message.getMessage() + "</b>"; // Bold text
      myViewHolder.textView_message.setText(Html.fromHtml(messageText + " <font color=\"#999999\">" + "</font> <br>"));
      myViewHolder.textView_message.setTextSize(16); // Set text size here
    }
  }




  @Override
  public int getItemCount() {
    return (null != data ? data.size() : 0);
  }

  private int getUsernameColor(String username) {
    if (!usernameColorMap.containsKey(username)) {
      // Generate a random color for the username
      Random random = new Random();
      int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
      usernameColorMap.put(username, color);
    }
    return usernameColorMap.get(username);
  }


  public class MyViewHolder extends RecyclerView.ViewHolder {
    protected TextView textView_message;
    protected TextView textView_username;
    protected TextView textView_sender_name;
    protected TextView textView_timestamp;
    public MyViewHolder(View view) {
      super(view);
      this.textView_message = view.findViewById(R.id.textView_message);
      this.textView_username = view.findViewById(R.id.textView_username);
      this.textView_timestamp = view.findViewById(R.id.textView_timestamp);
      this.textView_sender_name = view.findViewById(R.id.textView_sender_name); // Initialize textView_sender_name
      this.textView_username.setVisibility(View.GONE); // Hide the username TextView

    }
  }


}
