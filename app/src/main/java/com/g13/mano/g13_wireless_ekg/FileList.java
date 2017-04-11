package com.g13.mano.g13_wireless_ekg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class FileList extends AppCompatActivity
{

    private ListView mListViewer;
    private DatabaseReference mDatabase;
    private static final String TAG = "FileListActivity";
    public static String uid;
    private FirebaseAuth mAuth;
    public static Button edit,confirm;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        edit = (Button) findViewById(R.id.edit_info);
        confirm = (Button) findViewById(R.id.confirmation);


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = mAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        mListViewer = (ListView) findViewById(R.id.list_view);
        FirebaseListAdapter<String> myAdapter = new FirebaseListAdapter<String>(
                this,String.class,android.R.layout.simple_list_item_1, mDatabase.child("users").child(uid).child("Information"))
        {
            @Override
            protected void populateView(View v,String model,int position)
            {
                TextView textView = (TextView) v.findViewById(android.R.id.text1);
                textView.setText(model);

            }
        };
        mListViewer.setAdapter(myAdapter);

        edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(),InformationActivity.class);
                startActivity(intent);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

    }
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mAuth.signOut();
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        signOut();
//        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
//        startActivity(intent);

        finish();
    }
//    private void signOut() {
//
//        mAuth.signOut();
//    }
}
