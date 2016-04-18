package com.app.gfour.geofencetasker.data;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Dictionary;
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

        MajorContinents.put("Australia", new LatLng(25.27, 133.77));
        MajorContinents.put("Asia", new LatLng(34.04, 100.62));
        MajorContinents.put("Africa", new LatLng(8.78, 37.50));
        MajorContinents.put("Europe", new LatLng(54.52, 15.25));
        MajorContinents.put("America", new LatLng(37.09, 95.71));
        MajorContinents.put("NewZealand", new LatLng(40.90, 174.88));
    }

    public AchievementService() {
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
        double closestDistance = 100000;

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
        e.putInt(closestDestination, continentTaskCount).commit();

        if(continentTaskCount % 10 == 0){
            //TODO: KAREN SPAM THEM WITH THIS NOTIFICATION
            String destinationAchievement = "WELL DONE! " + Integer.toString(continentTaskCount) + "Tasks done in " + closestDestination + "!";
            Log.i(TAG, "ACHIEVEMENT: " + destinationAchievement);
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
            e.putInt("firstTask", seconds).commit();
        } else if (secondTask == 0) {
            e.putInt("secondTask", seconds).commit();
        } else if (thirdTask == 0) {
            e.putInt("thirdTask", seconds).commit();
        } else {
            if (seconds - firstTask <= secondsInADay) {
                //TODO: KAREN SPAM THEM WITH THIS NOTIFICATION
                String threeInADayAchievement = "WELL DONE! THREE IN A DAY!";
                Log.i(TAG, "ACHIEVEMENT: " + threeInADayAchievement);
            }
            // Shuffles tasks around, so the three latest completed tasks are kept up to date
            e.putInt("firstTask", secondTask).commit();
            e.putInt("secondTask", thirdTask).commit();
            e.putInt("thirdTask", seconds).commit();
        }
    }
}
