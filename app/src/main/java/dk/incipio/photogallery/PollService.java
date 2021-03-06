package dk.incipio.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

public class PollService extends IntentService {
    private static final String TAG = "dk.incipio.photogallery.PollService";
    private static final int POLL_INTERVAL = 1000*5; // check for news every 5 seconds
    public static final String PREF_IS_ALARM_ON ="isAlarmOn";
    public static final String ACTION_SHOW_NOTIFICATION ="dk.incipio.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE="dk.incipio.photogallery.PRIVATE";
    public PollService() {
        super(TAG);
    }



    protected void onHandleIntent(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        Log.i(TAG, "Received an intent: "+ intent);

        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo()!=null;
        if (!isNetworkAvailable) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String query = prefs.getString(FlickerFetchr.PREF_SEARCH_QUERY, null);
        String lastResultId = prefs.getString(FlickerFetchr.PREF_LAST_RESULT_ID, null);

        ArrayList<GalleryItem> items;
        if (query !=null) {
            items = new FlickerFetchr().search(query);
        } else {
            items = new FlickerFetchr().fetchItems();
        }

        if (items.size()==0)
            return;

        String resultId = items.get(0).getId();

        if (!resultId.equals(lastResultId)) {
            Log.i(TAG, "New result: "+ resultId);

            Resources r = getResources();
            PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, PhotoGalleryActivity.class), 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(r.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(r.getString(R.string.new_pictures_title))
                    .setContentText(r.getString(R.string.newpictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            // NotificationManager notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // notificationManager.notify(0,notification);
            // sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION),PERM_PRIVATE);

            showBackgroundNotification(0, notification);


        } else {
            Log.i(TAG,"Old result: "+ resultId);
        }

        prefs.edit().putString(FlickerFetchr.PREF_LAST_RESULT_ID, resultId).commit();

    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = new Intent(context, PollService.class);

        // p1: Context
        // p2: request
        // p3: Intent object
        // p4: Flags
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            // p1: Time basis constant
            // p2: When to start the alarm
            // p3: time interval
            // p4: Intent to fire on alarm
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        // The receiver needs to know whether the alarm should be on or off. So we use
        // a preference to store that...
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PollService.PREF_IS_ALARM_ON, isOn)
                .commit();

    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = new Intent(context, PollService.class);

        // The flag here means that if the described PendingIntent does not already exist,
        // then simply return null instead of creating it
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);

        return pi!= null;
    }

    public void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra("REQUEST_CODE", requestCode);
        i.putExtra("NOTIFICATION", notification);

        // p3: result receiver
        // p4: handler
        // p5: initial value result code
        // p6: initial value result data
        // p7: initial value result extras
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }

}
