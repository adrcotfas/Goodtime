package com.apps.adrcotfas.goodtime.BL;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Main.TimerActivity;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.apps.adrcotfas.goodtime.Util.IntentWithAction;

import java.util.concurrent.TimeUnit;

/**
 * Class responsible with creating and updating notifications for the foreground {@link TimerService}
 * and triggering notifications for events like finishing a session or updating the remaining time.
 * The notifications are customized according to {@link PreferenceHelper}.
 */
public class NotificationHelper extends ContextWrapper {

    private static final String TAG = NotificationHelper.class.getSimpleName();
    public static final String IN_PROGRESS_CHANNEL_ID = "goodtime.IN_PROGRESS_CHANNEL_ID";
    private NotificationManager manager;
    private NotificationCompat.Builder mBuilder;

    public NotificationHelper(Context context) {
        super(context);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(IN_PROGRESS_CHANNEL_ID, "Goodtime notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setBypassDnd(true);

            notificationChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            getManager().createNotificationChannel(notificationChannel);
            mBuilder = new NotificationCompat.Builder(context, notificationChannel.getId());
        } else {
            mBuilder = new NotificationCompat.Builder(context);
        }
        mBuilder = new NotificationCompat.Builder(context);
    }

    public Notification createNotification(Context context, CurrentSession currentSession) {
        Log.v(TAG, "createNotification");
        mBuilder.mActions.clear();
        mBuilder.setChannelId(IN_PROGRESS_CHANNEL_ID)
                .addAction(buildStopAction(context))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(null);

        if (currentSession.getSessionType().getValue() == SessionType.WORK) {
            if (currentSession.getTimerState().getValue() == TimerState.PAUSED) {
                mBuilder.addAction(buildResumeAction(context))
                        .setContentTitle("Work session is paused")
                        .setContentText("Continue?");
            } else {
                mBuilder.addAction(buildPauseAction(context))
                        .setContentTitle("Work session in progress")
                        .setContentText(buildProgressText(currentSession.getDuration().getValue()));
            }
        } else if (currentSession.getSessionType().getValue() == SessionType.BREAK) {
            mBuilder.setContentTitle("Break in progress")
                    .setContentText(buildProgressText(currentSession.getDuration().getValue()));
        } else {
            Log.wtf(TAG, "Trying to create a notification in an invalid state.");
        }

        return mBuilder
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setContentIntent(createActivityIntent(context))
                .setOngoing(true)
                .build();
    }

    public void notifyFinished(Context context, SessionType sessionType) {
        Log.v(TAG, "notifyFinished");
        mBuilder.mActions.clear();
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setOngoing(false);
        if (sessionType == SessionType.WORK) {
            mBuilder.setContentTitle("Work session finished")
                    .setContentText("Continue?")
                    .setSound(Uri.parse(PreferenceHelper.getNotificationSound()))
                    .addAction(buildStartBreakAction(context))
                    .addAction(buildSkipBreakAction(context));
        } else {
            mBuilder.setContentTitle("Break finished")
                    .setContentText("Continue?")
                    .setSound(Uri.parse(PreferenceHelper.getNotificationSound()))
                    .addAction(buildStartWorkAction(context));
        }
        getManager().notify(Constants.NOTIFICATION_ID, mBuilder.build());
    }

    public void updateNotificationProgress(Long duration) {
        mBuilder.setOnlyAlertOnce(true)
                .setContentText(buildProgressText(duration));
        getManager().notify(Constants.NOTIFICATION_ID, mBuilder.build());
    }

    public void clearNotification() {
        getManager().cancelAll();
    }

    private CharSequence buildProgressText(Long duration) {
        CharSequence output;
        long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(duration + 500);
        if (minutesLeft > 1) {
            //TODO: extract to strings
            output = minutesLeft + " minutes left";
        } else {
            output = "1 minute left";
        }

        return output;
    }


    /**
     * Get the notification manager.
     *
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    private PendingIntent createActivityIntent(Context context) {
        return PendingIntent.getActivity(context,
                0, new Intent(context, TimerActivity.class), 0);
    }

    //TODO: add string resources
    private NotificationCompat.Action buildStopAction(Context context) {

        PendingIntent stopPendingIntent = PendingIntent.getService(context, 0,
                new IntentWithAction(context, TimerService.class, Constants.ACTION.STOP), 0);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_stop,
                "STOP",
                stopPendingIntent).build();
    }

    private NotificationCompat.Action buildResumeAction(Context context) {
        PendingIntent togglePendingIntent = PendingIntent.getService(context, 0,
                new IntentWithAction(context, TimerService.class, Constants.ACTION.TOGGLE), 0);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                "RESUME",
                togglePendingIntent).build();
    }

    private NotificationCompat.Action buildPauseAction(Context context) {

        PendingIntent togglePendingIntent = PendingIntent.getService(context, 0,
                new IntentWithAction(context, TimerService.class, Constants.ACTION.TOGGLE), 0);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_pause,
                "PAUSE",
                togglePendingIntent).build();
    }

    private NotificationCompat.Action buildStartWorkAction(Context context) {
        PendingIntent togglePendingIntent = PendingIntent.getService(context, 0,
                new IntentWithAction(context, TimerService.class, Constants.ACTION.START_WORK), 0);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                "START WORK",
                togglePendingIntent).build();
    }

    private NotificationCompat.Action buildStartBreakAction(Context context) {
        PendingIntent togglePendingIntent = PendingIntent.getService(context, 0,
                new IntentWithAction(context, TimerService.class, Constants.ACTION.START_BREAK), 0);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                "START BREAK",
                togglePendingIntent).build();
    }

    private NotificationCompat.Action buildSkipBreakAction(Context context) {
        PendingIntent togglePendingIntent = PendingIntent.getService(context, 0,
                new IntentWithAction(context, TimerService.class, Constants.ACTION.SKIP_BREAK), 0);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_skip,
                "SKIP BREAK",
                togglePendingIntent).build();
    }
}
