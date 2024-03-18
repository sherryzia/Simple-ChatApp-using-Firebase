package com.androidadvance.simplechat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.androidadvance.simplechat.adapters.MainAdapter;
import com.androidadvance.simplechat.models.Message;
import com.androidadvance.simplechat.utils.SCUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

  // Constants
  public static final int ANTI_FLOOD_SECONDS = 3;

  // Variables
  private String username = "anonymous";
  private FirebaseDatabase database;
  private RecyclerView mainRecyclerView;
  private String userID;
  private MainActivity mContext;
  private MainAdapter adapter;
  private DatabaseReference databaseRef;
  private ImageButton sendButton;
  private EditText messageEditText;
  private ArrayList<Message> messageArrayList = new ArrayList<>();
  private ProgressBar progressBar;
  private long lastMessageTimestamp = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mContext = this;
    mainRecyclerView = findViewById(R.id.main_recycler_view);
    sendButton = findViewById(R.id.imageButton_send);
    messageEditText = findViewById(R.id.editText_message);
    progressBar = findViewById(R.id.progressBar);
    database = FirebaseDatabase.getInstance();
    databaseRef = database.getReference();

    progressBar.setVisibility(View.VISIBLE);

    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setStackFromEnd(true);
    mainRecyclerView.setLayoutManager(layoutManager);

    mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    adapter = new MainAdapter(mContext, messageArrayList);
    mainRecyclerView.setAdapter(adapter);

    databaseRef.child("the_messages").limitToLast(50).addChildEventListener(new ChildEventListener() {
      @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        progressBar.setVisibility(View.GONE);
        Message new_message = dataSnapshot.getValue(Message.class);
        messageArrayList.add(new_message);
        adapter.notifyDataSetChanged();
        mainRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
      }

      @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {

      }
      @Override
      public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d("REMOVED", dataSnapshot.getValue(Message.class).toString());
        Message removedMessage = dataSnapshot.getValue(Message.class);
        if (removedMessage != null) {
          messageArrayList.remove(removedMessage);
          adapter.notifyDataSetChanged();
        }
      }

      @Override
      public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

      @Override
      public void onCancelled(DatabaseError databaseError) {
        Toast.makeText(mContext, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });

    sendButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        processNewMessage(messageEditText.getText().toString().trim(), false);
      }
    });

    messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_SEND)) {
          sendButton.performClick();
          return true;
        }
        return false;
      }
    });

    logicForUsername();
  }

  private void processNewMessage(String newMessage, boolean isNotification) {
    if (newMessage.isEmpty()) {
      return;
    }

    if ((System.currentTimeMillis() / 1000L - lastMessageTimestamp) < ANTI_FLOOD_SECONDS) {
      Toast.makeText(mContext, "You cannot send messages so fast.", Toast.LENGTH_SHORT).show();
      return;
    }

    messageEditText.setText("");

    Message xMessage = new Message(userID, username, newMessage, System.currentTimeMillis() / 1000L, isNotification);

    String key = databaseRef.child("the_messages").push().getKey();
    if (key != null) {
      xMessage.setMessageId(key); // Set the generated key to the messageId field
      databaseRef.child("the_messages").child(key).setValue(xMessage);
      lastMessageTimestamp = System.currentTimeMillis() / 1000L;
    } else {
      Toast.makeText(mContext, "Failed to send message.", Toast.LENGTH_SHORT).show();
    }

  }

  private void logicForUsername() {
    userID = SCUtils.getUniqueID(getApplicationContext());
    databaseRef.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        progressBar.setVisibility(View.GONE);
        if (dataSnapshot.exists()) {
          username = dataSnapshot.getValue(String.class);
          Toast.makeText(mContext, "Logged in as " + username, Toast.LENGTH_SHORT).show();
        } else {
          showUsernameAlert();
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.w("Firebase", "username:onCancelled", databaseError.toException());
        Toast.makeText(mContext, "Failed to retrieve username.", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void showUsernameAlert() {
    AlertDialog.Builder alertDialogUsername = new AlertDialog.Builder(mContext);
    alertDialogUsername.setMessage("Your username");
    final EditText input = new EditText(mContext);
    input.setText(username);
    alertDialogUsername.setView(input);

    alertDialogUsername.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        String newUsername = input.getText().toString().trim();
        if ((!newUsername.equals(username)) && (!username.equals("anonymous"))) {
          processNewMessage(username + " changed its nickname to " + newUsername, true);
        }
        username = newUsername;
        databaseRef.child("users").child(userID).setValue(username);
      }
    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        dialog.dismiss();
      }
    });
    alertDialogUsername.show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      showUsernameAlert();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
