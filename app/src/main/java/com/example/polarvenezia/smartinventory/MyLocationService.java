package com.example.polarvenezia.smartinventory;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MyLocationService extends Service {
    public MyLocationService() {
    }

    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference itemRef;
    public static final String BROADCAST_ACTION = "Hello World";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    public static double currentLon = 0;
    public static double currentLat = 0;

    public double test_supermarket_loc_lat = 1.3421326;
    public double test_supermarket_loc_lon = 103.9642996;

    public boolean onSiteReminderOn = true;
    private static long disableNotificationAt = 0;
    private static final int disableDuration = 1000*100;

    public final int NOTIFICATION_ID = 1234;

    Intent intent;
    int counter = 0;

    @Override
    public void onCreate()
    {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        FirebaseApp.initializeApp(getApplication());
        rootRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://iot2016-b5048.firebaseio.com/");
        userRef = rootRef.child("User");
        itemRef = rootRef.child("Item");

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
            currentLat = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
            currentLon = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();
        }catch (SecurityException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Unable to get location, please check your location settings", Toast.LENGTH_SHORT).show();
        }
        getWeight("ShelfSmall1");
    }


    @Override
    public void onStart(Intent intent, int startId)
    {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra("MuteStatus",false)){
            tempDisableNotification();
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(intent.getIntExtra("ID",0));
            Log.d("Location Service: ", "Temp disabled");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }



    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }



    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        try {
            locationManager.removeUpdates(listener);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

    public static void tempDisableNotification(){
        disableNotificationAt = System.currentTimeMillis();
    }

    public boolean shldNotify(){
        return (System.currentTimeMillis() - disableNotificationAt) >= disableDuration;
    }


    public boolean supermarketNearby(Location location){
        double tolerance = 1;
        return (Math.pow(location.getLatitude()-test_supermarket_loc_lat,2)+Math.pow(location.getLongitude()-test_supermarket_loc_lon,2)) <= tolerance;
    }

    public class MyLocationListener implements LocationListener
    {

        // Define a listener that responds to location updates
            public void onLocationChanged(Location loc) {
                // Called when a new location is found by the network location provider.  Log.i("**************************************", "Location changed");
                if(isBetterLocation(loc, previousBestLocation)) {
                    currentLat = loc.getLatitude();
                    currentLon = loc.getLongitude();
                    intent.putExtra("Latitude", loc.getLatitude());
                    intent.putExtra("Longitude", loc.getLongitude());
                    intent.putExtra("Provider", loc.getProvider());
                    sendBroadcast(intent);
//                    Toast.makeText(getApplicationContext(),"Find LocatioN!" + " lon: "+currentLon+"  lat: "+currentLat, Toast.LENGTH_SHORT).show();
                    Log.d("Location Service: ", "lon: "+currentLon+"  lat: "+currentLat);
                    Log.d("Location Service: ", "status: "+shldNotify());
                    // prepare intent which is triggered if the
                    // notification is selected


                    if (onSiteReminderOn && shldNotify() && supermarketNearby(loc)) {
                        Intent intent = new Intent(MyLocationService.this, BarcodeCaptureActivity.class);
                        // use System.currentTimeMillis() to have a unique ID for the pending intent
                        PendingIntent pIntent = PendingIntent.getActivity(MyLocationService.this, (int) System.currentTimeMillis(), intent, 0);
                        // build notification
                        // the addAction re-use the same intent to keep the example short
                        Intent muteIntent = new Intent(MyLocationService.this, MyLocationService.class);
                        muteIntent.putExtra("MuteStatus",true);
                        muteIntent.putExtra("ID",NOTIFICATION_ID);
                        PendingIntent pMuteIntent = PendingIntent.getService(MyLocationService.this, 0, muteIntent, 0);
                        Notification n = new Notification.Builder(getApplicationContext())
                                .setContentTitle("Find supermarket nearby")
                                .setContentText("Would you like to refill your potato chips?")
                                .setSmallIcon(R.drawable.icon)
                                .setContentIntent(pIntent)
                                .setAutoCancel(true)
                                .addAction(0, "Mute", pMuteIntent).build();


                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                        notificationManager.notify(NOTIFICATION_ID, n);
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Toast.makeText(getApplicationContext() , "Location status changed", Toast.LENGTH_SHORT).show();
            }

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}

    }
    public void getWeight(String shelfID) {
        DatabaseReference weightRef = userRef.child(shelfID);
        Query newQuery = weightRef.limitToLast(1);
        newQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String string = "";
                for (DataSnapshot i: dataSnapshot.getChildren()) {
                    string = i.getValue(String.class);
                }
                double currentWeight = Double.parseDouble(string);
                if (currentWeight <= 160){
                    sendNotification();
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    public void sendNotification(){


        Intent intent = new Intent(MyLocationService.this, BarcodeCaptureActivity.class);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(MyLocationService.this, (int) System.currentTimeMillis(), intent, 0);
        // build notification
        // the addAction re-use the same intent to keep the example short
        Intent muteIntent = new Intent(MyLocationService.this, MyLocationService.class);
        muteIntent.putExtra("MuteStatus",true);
        muteIntent.putExtra("ID",System.currentTimeMillis());
        PendingIntent pMuteIntent = PendingIntent.getService(MyLocationService.this, 0, muteIntent, 0);
        Notification n = new Notification.Builder(getApplicationContext())
                .setContentTitle("Your food has run out")
                .setContentText("Would you like to refill")
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .addAction(0, "Mute", pMuteIntent).build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);
    }

}

