package giorag.dailytimer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.nio.channels.CancelledKeyException;
import java.util.Calendar;
import java.util.Set;

import giorag.dailytimer.R;
import giorag.dailytimer.activities.MainActivity;
import giorag.dailytimer.preferences.NotificationsPreferenceHelper;

public class ReminderService extends Service {
    private NotificationManager mNM;

    NotificationsPreferenceHelper prefs;

    private void log(String msg) { Log.i("ReminderService", msg); }

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    public static final int REMINDER_NOTIFICATION_ID = 32768;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        ReminderService getService() {
            return ReminderService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i("ReminderServices", "STARTED");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        prefs = new NotificationsPreferenceHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ReminderServices", "Received start id " + startId + ": " + intent);
        showNotification();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(REMINDER_NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    private boolean shouldSendNotification() {
        prefs.retrievePreferences();

        if (!prefs.shouldNotify()) {
            log("Notification time reached , but notifications are swithced off");
            return false;
        }

        if (!shouldNotifyToday()) {
            log("Notification time reached , but today is not in the days of reminder!");
            return false;
        }

        return true;
    }

    private boolean shouldNotifyToday() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        Set<String> daysToNotify = prefs.daysToNotify();
        if (daysToNotify == null)
            return false;
        return daysToNotify.contains("" + dayOfWeek);
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        if (!shouldSendNotification())
            return;

        log("Showing notification!");

        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Have you performed your daily rituals today?";

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this).setSmallIcon(R.drawable.ic_bug_report)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Daily reminder")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(REMINDER_NOTIFICATION_ID, notification);
    }
}
