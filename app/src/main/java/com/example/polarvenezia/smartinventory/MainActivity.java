package com.example.polarvenezia.smartinventory;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.example.polarvenezia.smartinventory.BarcodeFragment;
import com.example.polarvenezia.smartinventory.Inventory;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference itemRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        test_linear_regression("ShelfSmall1");
        test_linear_regression("ShelfSmall2");
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new BarcodeFragment(),"Barcode");
        adapter.addFragment(new Inventory(), "Inventory");
        viewPager.setAdapter(adapter);
    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
    public String test_linear_regression(final String shelfID) {
        FirebaseApp.initializeApp(getApplication());
        rootRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://iot2016-b5048.firebaseio.com/");
        userRef = rootRef.child("User");
        itemRef = rootRef.child("Item");

        final int NOTIFICATION_ID1 = 1234;
        final int NOTIFICATION_ID2 = 1342;
        final int NOTIFICATION_ID3 = 0011;

        final String estimate_refill_date = "";
        final ArrayList<String> output_weight_history = new ArrayList<>();

        final DatePrediction dtp = new DatePrediction();


        DatabaseReference weightRef = userRef.child(shelfID);
        Log.d("line 68 ",shelfID + " "+ weightRef.toString());
        // Attach a listener to read the data at our posts reference
        weightRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot key_time: dataSnapshot.getChildren()){
                    Log.d("line 62 ", key_time.toString());
                    output_weight_history.add(key_time.getKey()+","+key_time.getValue().toString());
                    Log.d("line 65 ", output_weight_history.toString());
                }


                for (String record : output_weight_history){
                    String[] split = record.split(",");
                    dtp.addDate(split[0]);
                    dtp.addWeight(split[1]);
                }
                dtp.linearRegression();
                String estimate_refill_date = dtp.getDate();
                Log.d("line 80", estimate_refill_date);

                if (!estimate_refill_date.equals("1970-01-01 07:30:00")){
                    //////////////////// SEND NOTIFICATION///////////////////////////////////
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    // use System.currentTimeMillis() to have a unique ID for the pending intent
                    PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, (int) System.currentTimeMillis(), intent, 0);
                    // build notification
                    // the addAction re-use the same intent to keep the example short
                    Intent muteIntent = new Intent(MainActivity.this, MainActivity.class);
                    muteIntent.putExtra("MuteStatus",true);
                    muteIntent.putExtra("ID",System.currentTimeMillis());
                    PendingIntent pMuteIntent = PendingIntent.getService(MainActivity.this, 0, muteIntent, 0);
                    Notification n = new Notification.Builder(getApplicationContext())
                            .setContentTitle("By "+ estimate_refill_date)
                            .setContentText("you should refill your "+shelfID)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentIntent(pIntent)
                            .setAutoCancel(true)
                            .addAction(0, "Mute", pMuteIntent).build();

                    Log.d("Notifyinggggg","hahaah");
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                    if (shelfID.equals("ShelfSmall1")){
                        notificationManager.notify(NOTIFICATION_ID1, n);
                    } else if (shelfID.equals("ShelfSmall2")){
                        notificationManager.notify(NOTIFICATION_ID2, n);
                    } else {
                        notificationManager.notify(NOTIFICATION_ID3, n);
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("line 64 ", "???");
            }
        });
        Log.d("line 74 ", output_weight_history.toString());
        return estimate_refill_date;
    }
}

