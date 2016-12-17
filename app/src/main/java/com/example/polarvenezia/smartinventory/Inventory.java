package com.example.polarvenezia.smartinventory;


import android.annotation.TargetApi;
import android.icu.text.SimpleDateFormat;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.polarvenezia.smartinventory.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

//import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class Inventory extends Fragment {
    private String TAG = "lol";
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference itemRef;
    private String xD;
    public static String temp1;
    public static String temp2;
    public static String temp3;
    protected TextView largeItem;
    protected TextView smallItem1;
    protected TextView smallItem2;
    protected String[] s0;
    protected TextView weightss1;
    protected String weight;

    public Inventory() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(getActivity().getApplication());
        rootRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://iot2016-b5048.firebaseio.com/");
        userRef = rootRef.child("User");
        itemRef = rootRef.child("Item");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        checkItemOnShelf1("ShelfBig");
        checkItemOnShelf2("ShelfSmall1");
        checkItemOnShelf3("ShelfSmall2");

        View v = (View) inflater.inflate(R.layout.fragment_inventory, container, false);
        largeItem = (TextView) v.findViewById(R.id.bsName);
        smallItem1 = (TextView) v.findViewById(R.id.ss1Name);
        smallItem2 = (TextView) v.findViewById(R.id.ss2Name);
        return v;
    }


    public void addUser(String shelfID) {
        userRef.child(shelfID);
    }

    public void loadItem(String shelfID, String qrcode) {
        userRef.child(shelfID).child("qrcode").setValue(qrcode);
    }


//    public String getWeight(String shelfID) {
//        String timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//        DatabaseReference shelfweightref = userRef.child(shelfID).child(timeStamp);
//    }


//    public String getItemListing(String qrcode) {
//        Query itemQuery = itemRef.orderByChild("qrcode").equalTo(qrcode);
//        itemQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                temp = (String) dataSnapshot.getValue(String.class);
//                return;
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                return;
//            }
//        });
//        return temp;
//    }

    public String checkItemOnShelf1(final String shelfID) {
        DatabaseReference shelfItemRef = rootRef.child("ShelfItem").child(shelfID);
        shelfItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                temp1 = (String) dataSnapshot.getValue(String.class);
                largeItem.setText(temp1);

                return;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                return;
            }
        });
        return temp1;
    }

    public String checkItemOnShelf2(final String shelfID) {
        DatabaseReference shelfItemRef = rootRef.child("ShelfItem").child(shelfID);
        shelfItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                temp2 = (String) dataSnapshot.getValue(String.class);
                smallItem1.setText(temp2);

                return;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                return;
            }
        });
        return temp2;
    }

    public String checkItemOnShelf3(final String shelfID) {
        DatabaseReference shelfItemRef = rootRef.child("ShelfItem").child(shelfID);
        shelfItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                temp3 = (String) dataSnapshot.getValue(String.class);
                smallItem2.setText(temp3);

                return;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                return;
            }
        });
        return temp3;
    }

}