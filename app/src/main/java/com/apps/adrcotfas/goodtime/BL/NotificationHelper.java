package com.apps.adrcotfas.goodtime.BL;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Main.TimerActivity;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.apps.adrcotfas.goodtime.Util.IntentWithAction;

import java.util.concurrent.TimeUnit;

//TODO: extract to strings

/**
 * Class responsible with creating and updating notifications for the foreground {@link TimerService}
 * and triggering notifications for events like finishing a session or updating the remaining time.
 * The notifications are customized according to {@link PreferenceHelper}.
 */
public class NotificationHelper extends ContextWrapper {

    private static final String TAG = NotificationHelper.class.getSimpleName();
    public static final String GOODTIME_NOTIFICATION = "goodtime.notification";
    public static int GOODTIME_NOTIFICATION_ID = 42;

    private NotificationManager mManager;

    public NotificationHelper(Context context) {
        super(context);
        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            initChannels();
        }
    }

    public void notifyFinished(SessionType sessionType) {
        Log.v(TAG, "notifyFinished");
        clearNotification();
        Notification notification = getFinishedSessionBuilder(sessionType).build();
        mManager.notify(GOODTIME_NOTIFICATION_ID, notification);
    }

    public void updateNotificationProgress(CurrentSession currentSession) {
        Log.v(TAG, "updateNotificationProgress");
        NotificationCompat.Builder builder = getInProgressBuilder(currentSession);
            builder.setOnlyAlertOnce(true)
                    .setContentText(buildProgressText(currentSession.getDuration().getValue()));
            mManager.notify(GOODTIME_NOTIFICATION_ID, builder.build());
    }

    public void clearNotification() {
        mManager.cancelAll();
    }

    private CharSequence buildProgressText(Long duration) {
        long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(duration);
        long minutesLeft = secondsLeft / 60;
        secondsLeft %= 60;

        return (minutesLeft > 9 ? minutesLeft : "0" + minutesLeft) + ":" +
                (secondsLeft > 9 ? secondsLeft : "0" + secondsLeft);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void initChannels() {
        NotificationChannel channelInProgress = new NotificationChannel(GOODTIME_NOTIFICATION, "Goodtime Notifications",
                NotificationManager.IMPORTANCE_LOW);
        channelInProgress.setBypassDnd(true);
        channelInProgress.setShowBadge(false);
        channelInProgress.setSound(null, null);
        mManager.createNotificationChannel(channelInProgress);
    }

    /**
     * Get a "in progress" notification
     *
     * @param currentSession the current session
     * @return the builder
     */
    public NotificationCompat.Builder getInProgressBuilder(CurrentSession currentSession) {
        Log.v(TAG, "createNotification");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, GOODTIME_NOTIFICATION)
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createActivityIntent())
                .setOngoing(true)
                .setShowWhen(false);

        if (currentSession.getSessionType().getValue() == SessionType.WORK) {
            if (currentSession.getTimerState().getValue() == TimerState.PAUSED) {
                builder.addAction(buildStopAction(this))
                        .addAction(buildResumeAction(this))
                        .setContentTitle("Work session is paused")
                        .setContentText("Continue?");
            } else {
                builder.addAction(buildStopAction(this))
                        .addAction(buildPauseAction(this))
                        .setContentTitle("Work session in progress")
                        .setContentText(buildProgressText(currentSession.getDuration().getValue()));
            }
        } else if (currentSession.getSessionType().getValue() == SessionType.BREAK) {
            builder
                    .addAction(buildStopAction(this))
                    .setContentTitle("Break in progress")
                    .setContentText(buildProgressText(currentSession.getDuration().getValue()));
        } else {
            Log.wtf(TAG, "Trying to create a notification in an invalid state.");
        }

        return builder;
    }

    public NotificationCompat.Builder getFinishedSessionBuilder(SessionType sessionType) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, GOODTIME_NOTIFICATION)
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setContentIntent(createActivityIntent())
                .setOngoing(false)
                .setShowWhen(false);
        if (sessionType == SessionType.WORK) {
            builder.setContentTitle("Work session finished")
                    .setContentText("Continue?")
                    .addAction(buildStartBreakAction(this))
                    .addAction(buildSkipBreakAction(this));
        } else if (sessionType == SessionType.BREAK) {
            builder.setContentTitle("Break finished")
                    .setContentText("Continue?")
                    .addAction(buildStartWorkAction(this));
        }
        return builder;
    }

    private PendingIntent createActivityIntent() {
        Intent openMainIntent = new Intent(this, TimerActivity.class);
        return PendingIntent.getActivity(this, 0, openMainIntent, 0);
    }

    //TODO: add string resources
    private static NotificationCompat.Action buildStopAction(Context context) {

        PendingIntent stopPendingIntent = PendingIntent.getService(context, 0,
                new IntentWithAction(context, TimerService.class, Constants.ACTION.STOP), 0);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_stop,
                "STOP",
                stopPendingIntent).build();
    }

    private static NotificationCompat.Action buildResumeAction(Context context) {
        PendingIntent togglePendingIntent = PendingIntent.getService(context, 0,
                new IntentWithAction(context, TimerService.class, Constants.ACTION.TOGGLE), 0);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                "RESUME",
                togglePendingIntent).build();
    }

    private static NotificationCompat.Action buildPauseAction(Context context) {

        PendingIntent togglePendingIntent = PendingIntent.getService(context, 0,
                new IntentWithAction(context, TimerService.class, Constants.ACTION.TOGGLE), 0);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_pause,
                "PAUSE",
                togglePendingIntent).build();
    }

    private static NotificationCompat.Action buildStartWorkAction(Context context) {
        PendingIntent togglePendingIntent = PendingIntent.getService(context, 0,
                new IntentWithAction(context, TimerService.class, Constants.ACTION.START_WORK), 0);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                "START WORK",
                togglePendingIntent).build();
    }

    private static NotificationCompat.Action buildStartBreakAction(Context context) {
        PendingIntent togglePendingIntent = PendingIntent.getService(context, 0,
                new IntentWithAction(context, TimerService.class, Constants.ACTION.START_BREAK), 0);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                "START BREAK",
                togglePendingIntent).build();
    }

    private static NotificationCompat.Action buildSkipBreakAction(Context context) {
        PendingIntent togglePendingIntent = PendingIntent.getService(context, 0,
                new IntentWithAction(context, TimerService.class, Constants.ACTION.SKIP_BREAK), 0);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_skip,
                "SKIP BREAK",
                togglePendingIntent).build();
    }
}
