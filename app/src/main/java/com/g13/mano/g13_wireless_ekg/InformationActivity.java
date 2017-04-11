package com.g13.mano.g13_wireless_ekg;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.g13.mano.g13_wireless_ekg.Information.UserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class InformationActivity extends AppCompatActivity {

    private static EditText height,weight,DOB,name,gender;
    public static Button next;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private static final String TAG = "InformationActivity";
    public static String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        height = (EditText)findViewById(R.id.height_user);
        weight = (EditText)findViewById(R.id.weight_user);
        DOB = (EditText)findViewById(R.id.DOB_user);
        name = (EditText)findViewById(R.id.name_user);
        gender = (EditText)findViewById(R.id.gender_user);

        next = (Button) findViewById(R.id.next_button);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    uid = user.getUid();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            uid = user.getUid();
        }
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String H = height.getText().toString();
                String W = weight.getText().toString();
                String N = name.getText().toString();
                String D = DOB.getText().toString();
                String G = gender.getText().toString();
                if (checkData()){
                    UserInfo information = new UserInfo();
                    information.setName(N);
                    information.setHeight(H);
                    information.setWeight(W);
                    information.setDOB(D);
                    information.setGender(G);
                    //final String path = "users/"+curUser.getUid();
                    mDatabase.child("users").child(uid).child("Information").setValue(information);
//                    Intent intent = new Intent(getApplicationContext(),FileList.class);
//                    startActivity(intent);
                    finish();
                }

            }
        });


    }

    private boolean checkData() {
        Log.d(TAG, "Validating and adding user information");
        if (!validateForm()) {
            return false;
        } else {
            return true;
        }
    }


    private boolean isValidDate(String inDate){
        if (inDate == null)
            return false;

        //set the format to use as a constructor argument
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (inDate.trim().length() != dateFormat.toPattern().length())
            return false;

        dateFormat.setLenient(false);

        try {
            //parse the inDate parameter
            dateFormat.parse(inDate.trim());
        }
        catch (ParseException pe) {
            return false;
        }
        return true;
    }


    private boolean validateForm() {
        boolean valid = true;

        String H = height.getText().toString();
        String W = weight.getText().toString();
        String N = name.getText().toString();
        String D = DOB.getText().toString();
        String G = gender.getText().toString();

        if (TextUtils.isEmpty(H)|| Integer.parseInt(H)>250 || Integer.parseInt(H)<=20)
        {
            height.setError("Required.");
            Toast.makeText(this,"Please enter Height",Toast.LENGTH_SHORT).show();
            valid = false;
        }
        else
        {
            height.setError(null);
        }


        if (TextUtils.isEmpty(W) || Integer.parseInt(W)>250 || Integer.parseInt(W)<=0)
        {
            weight.setError("Required.");
            Toast.makeText(this,"Please enter Weight",Toast.LENGTH_SHORT).show();
            valid = false;
        }
        else
        {
            weight.setError(null);
        }


        if (TextUtils.isEmpty(N))
        {
            name.setError("Required.");
            Toast.makeText(this,"Please enter Name",Toast.LENGTH_SHORT).show();
            valid = false;
        }
        else
        {
            name.setError(null);
        }


        if (TextUtils.isEmpty(D) || !isValidDate(D))
        {
            DOB.setError("Required.");
            Toast.makeText(this,"Please enter Date of Birth",Toast.LENGTH_SHORT).show();
            valid = false;
        }
        else
        {
            DOB.setError(null);
        }


        if (TextUtils.isEmpty(G))
        {
            gender.setError("Required.");
            Toast.makeText(this,"Please enter  your Gender",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "gender is: "+ G);
            valid = false;
        }
        else
        {
            gender.setError(null);
        }

        return valid;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        signOut();
//        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
//        startActivity(intent);
        finish();
    }

    private void signOut() {

        mAuth.signOut();
    }

}


