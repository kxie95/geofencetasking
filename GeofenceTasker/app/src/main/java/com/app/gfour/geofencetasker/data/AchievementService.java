package com.app.gfour.geofencetasker.data;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.app.gfour.geofencetasker.R;
import com.app.gfour.geofencetasker.tasks.TasksActivity;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AchievementService extends Service {

    // Stores all the continents and their respective lat/longs
    Map<String, LatLng> MajorContinents = new HashMap<String, LatLng>();

    // Shared Preferences
    SharedPreferences prefs;

    // Temp values for current tasks lat/long
    private double lat = 12.32;
    private double lng = 44.56;

    private String TAG = "NewTaskActivity";

    // Seconds in a day
    private int secondsInADay = 86400;

    @Override
    public void onCreate() {
        prefs = getSharedPreferences("AchievementPreferences", MODE_PRIVATE);

        MajorContinents.put("Australasia", new LatLng(25.27, 133.77));
        MajorContinents.put("Asia", new LatLng(34.04, 100.62));
        MajorContinents.put("Africa", new LatLng(8.78, 37.50));
        MajorContinents.put("Europe", new LatLng(54.52, 15.25));
        MajorContinents.put("America", new LatLng(37.09, 95.71));
        MajorContinents.put("Antarctica", new LatLng(-83.49, 21.94));
    }

    public AchievementService() {
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Convert the users task location into lat/long
        String locationString = intent.getStringExtra("Address");

        Geocoder gc = new Geocoder(this);
        try {
            List<Address> list = gc.getFromLocationName(locationString, 1);
            Address address = list.get(0);
            lat = address.getLatitude();
            lng = address.getLongitude();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        // Execute checks which will award achievements
        checkForThreeADay();
        checkForLocationCount();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void checkForLocationCount() {

        String closestDestination = "The Matrix";
        double closestDistance = Double.MAX_VALUE;

        Set<String> dictionaryKeys = MajorContinents.keySet();

        for (Iterator<String> x = dictionaryKeys.iterator(); x.hasNext();) {
            String dictKey = (String) x.next();
            LatLng dictValue = (LatLng) MajorContinents.get(dictKey);
            double d = distance(lat, dictValue.latitude, lng, dictValue.longitude);
            if(d < closestDistance) {
                closestDistance = d;
                closestDestination = dictKey;
            }
        }

        int continentTaskCount = prefs.getInt(closestDestination, 0);
        continentTaskCount++;

        SharedPreferences.Editor e = prefs.edit();
        e.putInt(closestDestination, continentTaskCount).apply();

        if(continentTaskCount % 2 == 0){
            sendNotification("ACHIEVEMENT", "Well done! " + Integer.toString(continentTaskCount)
                    + " tasks done in " + closestDestination + "!");
        }
    }

    /*
 * Calculate distance between two points in latitude and longitude.
 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
 * @returns Distance in Meters
 * Does not need to be accurate as only continents are being calculated
 */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2) + Math.pow(0, 2);

        return Math.sqrt(distance);
    }

    public void checkForThreeADay() {
        // Get the current time
        Calendar c = Calendar.getInstance();
        int seconds = c.get(Calendar.SECOND);

        // Accessing the shared preferences
        int firstTask = prefs.getInt("firstTask", 0);
        int secondTask = prefs.getInt("secondTask", 0);
        int thirdTask = prefs.getInt("thirdTask", 0);

        SharedPreferences.Editor e = prefs.edit();

        // Iterate through first, second and third day
        if (firstTask == 0) {
            e.putInt("firstTask", seconds).apply();
        } else if (secondTask == 0) {
            e.putInt("secondTask", seconds).apply();
        } else if (thirdTask == 0) {
            e.putInt("thirdTask", seconds).apply();
        } else {
            if (seconds - firstTask <= secondsInADay) {
                sendNotification("ACHIEVEMENT", "Well done! Three in a day!");
            }
            // Shuffles tasks around, so the three latest completed tasks are kept up to date
            e.putInt("firstTask", secondTask).apply();
            e.putInt("secondTask", thirdTask).apply();
            e.putInt("thirdTask", seconds).apply();
        }
    }

    private void sendNotification(String title, String details) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), TasksActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(TasksActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_media_play)
                .setContentTitle(title)
                .setContentText(details)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }
}
