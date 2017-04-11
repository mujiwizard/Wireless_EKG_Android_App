package com.g13.mano.g13_wireless_ekg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class LoadFiles extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private static final String TAG = "LoadFilesActivity";
    public static String uid;
    public static Button load,cancel;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_files);
    }
}
