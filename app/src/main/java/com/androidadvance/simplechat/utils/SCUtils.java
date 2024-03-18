package com.androidadvance.simplechat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.androidadvance.simplechat.R;
import com.google.android.material.snackbar.Snackbar;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SCUtils {

  // Returns a unique id for each device
  public static String getUniqueID(Context ctx) {
    return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
  }

  // Use either formattedDate or timeAgo (below)
  public static String formattedDate(long timestamp) {
    Calendar cal = Calendar.getInstance();
    TimeZone tz = cal.getTimeZone(); // Get your local time zone.
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mma"); // dd MMM yyyy KK:mma
    sdf.setTimeZone(tz); // Set time zone.
    String localTime = sdf.format(new Date(timestamp * 1000));
    return localTime.toLowerCase();
  }

  public static String timeAgo(long messageTimestamp) {
    return android.text.format.DateUtils.getRelativeTimeSpanString(messageTimestamp * 1000L, System.currentTimeMillis(), android.text.format.DateUtils.SECOND_IN_MILLIS).toString();
  }

  // Show Facebook key hash
  public static void showFacebookKeyHash(Context ctx) {
    try {
      PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES);
      for (Signature signature : info.signatures) {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(signature.toByteArray());
        android.util.Log.d("FB KeyHash:", android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT));
      }
    } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  public static Snackbar showErrorSnackBar(Activity mContext, View rootView, String message) {
    if (message == null || message.isEmpty()) {
      message = "There was an error.";
    }
    Snackbar snackError = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
    View view = snackError.getView();
    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
    view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.material_red));
    tv.setTextColor(Color.parseColor("#FFFFFF"));
    return snackError;
  }
}
