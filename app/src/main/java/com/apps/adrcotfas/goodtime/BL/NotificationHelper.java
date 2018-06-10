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
import android.media.AudioManager;
import android.net.Uri;
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

    static final long[] DEFAULT_VIBRATE_PATTERN = {0, 250, 250, 250};

    private static final String TAG = NotificationHelper.class.getSimpleName();
    public static int IN_PROGRESS_NOTIFICATION_ID = 42;
    public static int START_WORK_NOTIFICATION_ID = 43;
    public static int START_BREAK_NOTIFICATION_ID = 43;
    public static final String IN_PROGRESS_CHANNEL_ID = "goodtime.IN_PROGRESS_CHANNEL_ID";
    public static final String CHANNEL_ID_BREAK_FINISHED = "goodtime.CHANNEL_ID_BREAK_FINISHED";
    public static final String CHANNEL_ID_WORK_FINISHED = "goodtime.CHANNEL_ID_WORK_FINISHED";

    private NotificationManager mManager;
    private NotificationCompat.Builder mBuilderInProgress;
    private NotificationCompat.Builder mBuilderWorkFinished;
    private NotificationCompat.Builder mBuilderBreakFinished;

    public NotificationHelper(Context context) {
        super(context);
        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            initChannels();
        }
        initBuilders();
    }

    public void notifyFinished(Context context, SessionType sessionType) {
        Log.v(TAG, "notifyFinished");

        clearNotification();

        if (sessionType == SessionType.WORK) {
            mBuilderWorkFinished
                    .setVibrate(PreferenceHelper.isVibrationEnabled() ? DEFAULT_VIBRATE_PATTERN : null)
                    .setSound(PreferenceHelper.isRingtoneEnabled() ? Uri.parse(PreferenceHelper.getNotificationSound()) : null, AudioManager.STREAM_ALARM);
            Notification notification = mBuilderWorkFinished.build();

            notification.flags =  PreferenceHelper.isRingtoneInsistent() ?
                    notification.flags | Notification.FLAG_INSISTENT : notification.flags & ~Notification.FLAG_INSISTENT;

            mManager.notify(START_BREAK_NOTIFICATION_ID, notification);
        } else {
            mBuilderBreakFinished
                    .setVibrate(PreferenceHelper.isVibrationEnabled() ? DEFAULT_VIBRATE_PATTERN : null)
                    .setSound(PreferenceHelper.isRingtoneEnabled() ? Uri.parse(PreferenceHelper.getNotificationSoundBreak()) : null, AudioManager.STREAM_ALARM);
            Notification notification = mBuilderBreakFinished.build();

            notification.flags =  PreferenceHelper.isRingtoneInsistent() ?
                    notification.flags | Notification.FLAG_INSISTENT : notification.flags & ~Notification.FLAG_INSISTENT;
            mManager.notify(START_WORK_NOTIFICATION_ID, notification);
        }
    }

    public void updateNotificationProgress(Long duration) {
        if (mBuilderInProgress != null) {
            mBuilderInProgress.setOnlyAlertOnce(true)
                    .setContentText(buildProgressText(duration));
            mManager.notify(IN_PROGRESS_NOTIFICATION_ID, mBuilderInProgress.build());
        }
    }

    public void clearNotification() {
        mManager.cancelAll();
    }

    private CharSequence buildProgressText(Long duration) {
        CharSequence output;
        long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(duration + 500);
        if (minutesLeft > 1) {
            output = minutesLeft + " minutes left";
        } else {
            output = "1 minute left";
        }

        return output;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void initChannels() {
        NotificationChannel channelInProgress = new NotificationChannel(IN_PROGRESS_CHANNEL_ID, "In progress",
                NotificationManager.IMPORTANCE_LOW);
        channelInProgress.setBypassDnd(true);
        channelInProgress.setSound(null, null);
        mManager.createNotificationChannel(channelInProgress);

        NotificationChannel channelWorkFinished = new NotificationChannel(CHANNEL_ID_WORK_FINISHED, "Work finished",
                NotificationManager.IMPORTANCE_HIGH);
        channelWorkFinished.setDescription("Work finished notification");
        channelWorkFinished.setBypassDnd(true);
        channelWorkFinished.enableVibration(true);
        mManager.createNotificationChannel(channelWorkFinished);

        NotificationChannel channelBreakFinished = new NotificationChannel(CHANNEL_ID_BREAK_FINISHED, "Break finished",
                NotificationManager.IMPORTANCE_HIGH);
        channelWorkFinished.setDescription("Break finished notification");
        channelBreakFinished.setBypassDnd(true);
        channelBreakFinished.enableVibration(true);
        mManager.createNotificationChannel(channelBreakFinished);
    }

    private void initBuilders() {
        mBuilderInProgress = new NotificationCompat.Builder(this, IN_PROGRESS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createActivityIntent())
                .setOngoing(true)
                .setShowWhen(false);

        mBuilderWorkFinished = new NotificationCompat.Builder(this, CHANNEL_ID_WORK_FINISHED)
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setContentIntent(createActivityIntent())
                .setOngoing(false)
                .setShowWhen(false)
                .setContentTitle("Work session finished")
                .setContentText("Continue?")
                .addAction(buildStartBreakAction(this))
                .addAction(buildSkipBreakAction(this));

        mBuilderBreakFinished = new NotificationCompat.Builder(this, CHANNEL_ID_BREAK_FINISHED)
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setShowWhen(false)
                .setContentIntent(createActivityIntent())
                .setOngoing(false)
                .setContentTitle("Break finished")
                .setContentText("Continue?")
                .addAction(buildStartWorkAction(this));
        }

    /**
     * Get a "in progress" notification
     *
     * Provide the builder rather than the notification it's self as useful for making notification
     * changes.
     *
     * @param currentSession the current session
     * @return the builder
     */
    public NotificationCompat.Builder getInProgressBuilder(CurrentSession currentSession) {
        Log.v(TAG, "createNotification");

        mBuilderInProgress.mActions.clear();
        if (currentSession.getSessionType().getValue() == SessionType.WORK) {
            if (currentSession.getTimerState().getValue() == TimerState.PAUSED) {
                mBuilderInProgress
                        .addAction(buildStopAction(this))
                        .addAction(buildResumeAction(this))
                        .setContentTitle("Work session is paused")
                        .setContentText("Continue?");
            } else {
                mBuilderInProgress
                        .addAction(buildStopAction(this))
                        .addAction(buildPauseAction(this))
                        .setContentTitle("Work session in progress")
                        .setContentText(buildProgressText(currentSession.getDuration().getValue()));
            }
        } else if (currentSession.getSessionType().getValue() == SessionType.BREAK) {
            mBuilderInProgress
                    .addAction(buildStopAction(this))
                    .setContentTitle("Break in progress")
                    .setContentText(buildProgressText(currentSession.getDuration().getValue()));
        } else {
            Log.wtf(TAG, "Trying to create a notification in an invalid state.");
        }

        return mBuilderInProgress;
    }

    private PendingIntent createActivityIntent() {
        Intent openMainIntent = new Intent(this, TimerActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(TimerActivity.class);
        stackBuilder.addNextIntent(openMainIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);
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
