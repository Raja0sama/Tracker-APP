package com.choicemartstore.computerzone.dtracker;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.service.carrier.CarrierMessagingService;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.ValueEventListener;

import java.io.Console;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class TrackerService extends Service {

    private static final String TAG = TrackerService.class.getSimpleName();
    String id;
    String altitude;
    private SensorManager mSensorManager;
    List<Sensor> sensors;
    Sensor sensor;
    Location myLocation;
    double longitude;
    double latitude;
    LocationListener locationListener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();

//Calling Service Notification
        buildNotification();
//Calling UserData
        readingid();


    }


    //Building Notification
    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);
        // Create the persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("You are now being tracked by Choicemart")
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_tracker);
        startForeground(1, builder.build());
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received stop broadcast");
            // Stop the service when the notification is tapped
            unregisterReceiver(stopReceiver);
        }
    };

    private void loginToFirebase() {
        // Functionality coming next step
    }


    //Requesting Location and submiting to Database every single miliseconds
    private void requestLocationUpdates(String trackid) {

        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(500);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        final String path = "locations/" + trackid.toString();
        int permission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.d(TAG, "location update " + location);
                        ref.setValue(location);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final String uid = user.getUid().toString();
                        String paths = "users/" + uid + "/Status";
                        FirebaseDatabase.getInstance().getReference(paths).setValue("Online");
                        onUserdisconnectToInternet();
//                        DatabaseReference refs = FirebaseDatabase.getInstance().getReference("users/" + uid + "/lastseen");
//                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
//                        refs.setValue(currentDateTimeString);
                    }
                }
            }, null);
        }

    }

    //Reading Data of the users .
    public void readingid() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String key = dataSnapshot.getKey();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    final String uid = user.getUid().toString();
                    if (key.equals(uid)) {
                        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
                        String tracke = value.get("trackerid").toString();
                        id = tracke;
                        requestLocationUpdates(id);
                        String status = value.get("Status").toString();
                        final String path = "users/" + uid + "/Status";
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                        ref.setValue("Online");



                    }

                }


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                String key = dataSnapshot.getKey();
                HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
                String tracke = value.get("trackerid").toString();
                //    Toast.makeText(TrackerService.this, "hellow" + tracke, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void onDestroy() {

    }

    public void onUserdisconnectToInternet() {
        Log.d("ClearFromRecentService", "Service Destroyed");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            final String uid = user.getUid().toString();
            final String path = "users/" + uid + "/Status";
            FirebaseDatabase.getInstance().getReference(path).onDisconnect().setValue("Offline");

        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals("true")) {
            Log.i("as", "Received Start Foreground Intent ");
            // your start service code
        }
        else if (intent.getAction().equals( "false")) {
            Log.i("as", "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }
    public void setuseronline() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
             String uid = user.getUid().toString();
             String path = "users/" + uid + "/Status";
            FirebaseDatabase.getInstance().getReference(path).setValue("Online");

        }

   }
}