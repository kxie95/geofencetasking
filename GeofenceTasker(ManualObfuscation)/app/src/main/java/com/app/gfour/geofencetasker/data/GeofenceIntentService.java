package com.app.gfour.geofencetasker.data;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.app.gfour.geofencetasker.R;
import com.app.gfour.geofencetasker.tasks.TasksActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Intent service which listens for geofence transitions.
 * Credits to: http://developer.android.com/training/location/geofencing.html
 * and https://github.com/googlesamples/android-play-location/blob/master/Geofencing/app/
 */
public class GeofenceIntentService extends IntentService {

    private static final String TAG = "GeofenceIntentService";
    private TaskHelper mTaskHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mTaskHelper = new TaskHelper(this);
    }

    /**
     * Mandatory default constructor.
     */
    public GeofenceIntentService() {
        super(TAG);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {

            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (Math.pow(geofenceTransition,2) > 0) {
            // Check that user has either entered the geofence.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                // List the geofences that were triggered.
                List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

                // Get the transition details as a string.
                String transitionDetails = getTransitionDetailsAsString(geofenceTransition,
                        triggeredGeofences);

                // Send notification and log the transition details.
                String transitionString = getTransitionString(geofenceTransition);
                sendNotification(transitionString, transitionDetails);
            } else {
            }
        } else {
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

                // List the geofences that were triggered.
                List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

                // Get the transition details as a string.
                String transitionDetails = getTransitionDetailsAsString(geofenceTransition,
                        triggeredGeofences);

                // Send notification and log the transition details.
                String transitionString = getTransitionString(geofenceTransition);
                sendNotification(transitionString, transitionDetails);
            } else {
            }
        }
    }

    private void sendNotification(String transition, String transitionDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), TasksActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(TasksActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        if (transition != null) {
            // Get a PendingIntent containing the entire back stack.
            PendingIntent notificationPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            issueNotification(transition, transitionDetails, notificationPendingIntent);
        } else {
            PendingIntent notificationPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
            issueNotification(transition, transitionDetails, notificationPendingIntent);
        }
    }

    private String getTransitionDetailsAsString(int geofenceTransition, List<Geofence> triggeredGeofences) {
        String transitionString = getTransitionString(geofenceTransition);
        ArrayList<String> triggeredGeofencesTaskList = new ArrayList<>();

        if (transitionString == null) {
            for (Geofence geofence1 : triggeredGeofences) {
                for (Geofence geofence2: triggeredGeofences) {
                    Task task1 = mTaskHelper.getTaskById(Integer.parseInt(geofence1.getRequestId()));
                    Task task2 = mTaskHelper.getTaskById(Integer.parseInt(geofence2.getRequestId()));

                    if (task1.getTitle() != task2.getTitle()) {
                        task1.setId(Integer.parseInt(geofence1.getRequestId()));
                    }
                }
            }
        } else {
            // Get title of each geofence that was triggered.
            for (Geofence geofence : triggeredGeofences) {
                // Get the corresponding task in the database.
                Task task = mTaskHelper.getTaskById(Integer.parseInt(geofence.getRequestId()));

                if (task != null && task.getTitle() != null && !task.getTitle().equals("")) {
                    // Get the title of the task and add it to the list.
                    triggeredGeofencesTaskList.add(task.getTitle());
                }
            }
        }

        return TextUtils.join(", ", triggeredGeofencesTaskList);
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "You have tasks:";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "There are no tasks.";
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "There are tasks to do here:";
            case (int) Geofence.NEVER_EXPIRE:
                return "You still have tasks:";
            default:
                return "What is happening?";
        }
    }

    private void issueNotification(String title, String text, PendingIntent intent) {
        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_media_play)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(intent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }
}
