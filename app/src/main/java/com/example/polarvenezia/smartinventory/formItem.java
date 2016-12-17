package com.example.polarvenezia.smartinventory;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.Scanner;

import static android.R.attr.name;
import static android.R.attr.onClick;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.FirebaseApp;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class formItem extends AppCompatActivity implements View.OnClickListener {
    Typeface myfont;
    protected EditText nameField;
    protected Spinner typeSpin;
    protected Spinner shelfField;
    private DatabaseReference rootRef;
    private DatabaseReference userRef;
    private DatabaseReference itemRef;
    private String xD;
    private String barcode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Spinner typeSpin = (Spinner) findViewById(R.id.typeSpinner);
        final Spinner shelfSpin = (Spinner) findViewById(R.id.shelffield);
        EditText nameField = (EditText) findViewById(R.id.namefield);
        Button submiBtn = (Button) findViewById(R.id.submitbutton);
        this.nameField=nameField;
        this.shelfField=shelfSpin;
        this.typeSpin=typeSpin;
        submiBtn.setOnClickListener(formItem.this);
        ArrayAdapter<CharSequence> typeadapter = ArrayAdapter.createFromResource(this,R.array.type_array,android.R.layout.simple_spinner_item);
        typeadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpin.setAdapter(typeadapter);
        ArrayAdapter<CharSequence> shelfAd = ArrayAdapter.createFromResource(this,R.array.shelfArray,android.R.layout.simple_spinner_item);
        shelfAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shelfSpin.setAdapter(shelfAd);
        FirebaseApp.initializeApp(getApplication());
        rootRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://iot2016-b5048.firebaseio.com/");
        userRef = rootRef.child("User");
        itemRef = rootRef.child("Item");
        barcode=getIntent().getStringExtra("barcode");
    }
    @Override
    public void onClick(View v){
        String nameStr = nameField.getText().toString();
        String shelfStr = shelfField.getSelectedItem().toString();
        String typeStr = typeSpin.getSelectedItem().toString();
        addUser(shelfStr,nameStr);
        createItemListing(barcode,nameStr);
        Toast.makeText(getApplicationContext(),"Successfully Submitted!", Toast.LENGTH_SHORT).show();
    }
    private void createItemListing(String qrcode, String itemName) {
        itemRef.child(qrcode).child("itemName").setValue(itemName);
    }

    private void addUser(String shelfID,String itemName) {
        rootRef.child("ShelfItem").child(shelfID).setValue(itemName);
    }


}
