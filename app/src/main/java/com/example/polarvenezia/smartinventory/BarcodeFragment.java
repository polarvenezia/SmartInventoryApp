package com.example.polarvenezia.smartinventory;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


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

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class BarcodeFragment extends Fragment implements View.OnClickListener {

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";
    private TextView statusMessage;
    private TextView barcodeValue;
    private TextView filler;
    private Button inputManual;
    private Button scanBarcode;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference itemRef;
    private String xD;
    public String passForm;
    public BarcodeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barcode, container, false);
        Button scanBarcode = (Button) view.findViewById(R.id.scan_barcode);
        scanBarcode.setOnClickListener(BarcodeFragment.this);
        statusMessage = (TextView)view.findViewById(R.id.status_message);
        barcodeValue = (TextView)view.findViewById(R.id.barcode_value);
        filler = (TextView) view.findViewById(R.id.instruct);
        inputManual = (Button) view.findViewById(R.id.input_manually);
        inputManual.setOnClickListener(BarcodeFragment.this);
        getActivity().startService(new Intent(getContext(), MyLocationService.class));
        String serviceStatus = ""+ isMyServiceRunning(MyLocationService.class);
        //statusMessage.setText(serviceStatus);

        String currentLoc = MyLocationService.currentLat + ", " + MyLocationService.currentLon;
        barcodeValue.setText(currentLoc);


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scan_barcode) {
            // launch barcode activity.
            Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
            intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }
        else if (v.getId() == R.id.input_manually){
            //TODO create form for manual input
            Intent intent = new Intent(getContext(), formItem.class);
            intent.putExtra("barcode",passForm);
            startActivity(intent);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    barcodeValue.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                    //createItemListing(barcode.displayValue,"container");
                    filler.setText(R.string.proceed);
                    inputManual.setText(R.string.selshelf);
                    passForm=barcode.displayValue;
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //firebase
        FirebaseApp.initializeApp(getActivity().getApplication());
        rootRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://iot2016-b5048.firebaseio.com/");
        userRef = rootRef.child("User");
        itemRef = rootRef.child("Item");
    }

    private void createItemListing(String qrcode, String itemName) {
        itemRef.child(qrcode).child("itemName").setValue(itemName);
    }

    private void addUser(String shelfID) {
        userRef.child(shelfID);
    }

    private void loadItem(String shelfID, String qrcode) {
        userRef.child(shelfID).child(qrcode);
    }

    private void updateWeight(String shelfID, String qrcode, String weight ) {
        userRef.child(shelfID).child(qrcode).child("weight").setValue(weight);
    }

    private String getItemListing(String qrcode) {
        Query itemQuery = itemRef.orderByChild("qrcode").equalTo(qrcode);
        itemQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                xD = dataSnapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return xD;
    }
    public String getbarcode(){
        return passForm;
    }


}


