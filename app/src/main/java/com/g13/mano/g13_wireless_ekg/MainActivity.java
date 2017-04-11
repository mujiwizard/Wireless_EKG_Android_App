package com.g13.mano.g13_wireless_ekg;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
//    public static final String TAG = "G13 Wireless EKG";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;

    public Button btnConnectDisconnect,btnSave,btnLoad;
    public TextView heart,spo2;


    //private String dataString ="";
    private LineGraphSeries<DataPoint> series;
    private PointsGraphSeries<DataPoint> series2;


    double sec1 = System.currentTimeMillis()/1000.0;
    //public int lastLoc,currentLoc = 0;
    //public String hVal,sVal;
    public int a =0;
    //public ArrayList<Double> dataGradient = new ArrayList<Double>();
    public int count =0;
    public  double [] dataGradient = new double[3];


    private static final StringBuilder dataString = new StringBuilder(); // holds all data received
    private static String unusedData = ""; // holds unused data from previous packet

    public StorageReference mStorageRef;
    public DatabaseReference mDatabase;
    private static final String TAG = "MainActivity";
    public static String uid;
    public FirebaseAuth mAuth;
    public final String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
    public String fileName;
    public String Credentials;



    public String filePath;
    public String userName;
    public String FirebaseStoragePath;
    public String time;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseUser user = mAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        Credentials  = user.getEmail().toString();
        int ind = Credentials.indexOf("@");
        userName = Credentials.substring(0,ind);

        Log.d("this username",""+userName);
        FirebaseStoragePath = "users" +"/"+ uid;
        mStorageRef = storage.getReference();


        setContentView(R.layout.activity_main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSave=(Button) findViewById(R.id.savebtn);
        btnLoad = (Button) findViewById(R.id.loadbtn);
        heart = (TextView) findViewById(R.id.heartValue);
        spo2 = (TextView) findViewById(R.id.spo2Value);


        GraphView graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<>();
        series2 = new PointsGraphSeries<>();

        //series.setTitle("EKG Signal");
        Viewport viewport = graph.getViewport();
        /viewport.setYAxisBoundsManual(true);
        viewport.setScalableY(true);
        //viewport.setMinY(6.5);
        //viewport.setMaxY(9);

        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setScalable(true);
        //viewport.calcCompleteRange();

        graph.setTitle("EKG Signal");
        graph.addSeries(series);
        graph.addSeries(series2);
        series2.setShape(PointsGraphSeries.Shape.RECTANGLE);
        series.setThickness(3);

        service_init();

        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    if (btnConnectDisconnect.getText().equals("Connect")){

                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        //Disconnect button pressed
                        if (mDevice!=null)
                        {
                            mService.disconnect();

                        }
                    }
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(dataString!= null)
                {
                    if (mDevice!=null)
                    {
                        mService.disconnect();

                    }
                    if(fileWrite(userName)){
                        Toast.makeText(MainActivity.this,"Data was written to file",Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this,"No data to be written",Toast.LENGTH_LONG).show();
                }

            }
        });

//        btnLoad.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(getApplicationContext(),InformationActivity.class);
//                startActivity(intent);
//            }
//        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override

        //Handler events that received from UART service
        public void handleMessage(Message msg) {

        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText("Disconnect");
                        //edtMessage.setEnabled(true);
                        //btnSend.setEnabled(true);
                        //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                        //listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setText("Connect");
                        //edtMessage.setEnabled(false);
                        //btnSend.setEnabled(false);
                        //((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        //listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();

                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);

                String packet;
                try {
                    packet = new String(txValue, "UTF-8"); // assumes Latin-1 encoding
                    // If you use different encoding when sending the data, change this one to match
                    // Aside: UTF-8 uses 1-4 bytes per character, while Latin-1 uses 1 byte
                } catch (UnsupportedEncodingException | UnsupportedCharsetException ex) {
                    // Not sure what to do with the error
                    packet = null;
                }
                if (packet != null && !packet.isEmpty())
                {
                    dataString.append(packet); // keep all packets so they can be written to a file later
                    String[] data = packet.split("\n"); // split the packet into an array using \n
                    for (int i = 0; i < data.length; i++)
                    {
                        if (i == 0 || packet.endsWith("\n")) // first bit of data in this packet could belong to the
                        {           // last bit of data from the previous packet
                            unusedData += data[i];
                            if (data.length > 1) // true if there is at least 1 newline character
                            {                    // i.e. This data is complete
                                parse(unusedData);
                                unusedData = "";
                            }
                        }
                        else if (i < data.length - 1) // doesn't include first or last position
                        {
                            parse(data[i]);
                        }
                        else // last position in the array
                        {
                            if (packet.endsWith("\n")) // data is complete
                            {
                                parse(data[i]);
                            }
                            else // data is not yet complete
                            {
                                unusedData = data[i];
                            }
                        }
                    }
                }

            }

            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){

                mService.disconnect();
            }

        }
    };

    public double temp = 0.0;
    private void parse(String data)
    {
        double value;

        if (!data.isEmpty()) // likely an unnecessary check
        {
            if (data.charAt(0) == 'H')
            {
                //System.out.println("Heart");
                //value = stringToDouble(data.substring(1));
                heart.setText(data.substring(1));
                // Do something with value if value != -1
            }
            else if (data.charAt(0) == 'S')
            {
                //System.out.println("SPI");
                //value = stringToDouble(data.substring(1));
                // Do something with value if value != -1
                spo2.setText(data.substring(1));
            }
            else
            {
                value = stringToDouble(data)/1000000.0;
                if (value>3.0){
                    //temp = temp+0.01;
                    temp = temp+0.0038;
                    series.appendData(new DataPoint(temp,value),true,1000);
                    // Do something with value if value != -1
                    //getGradient(value);
                }

            }
        }
        else
        {
            //System.out.println("Empty data");
            Log.w("Parse", "Data was empty");
        }
    }

    private double stringToDouble(String value)
    {
        double result = -1; // value that will be returned on an error
        try {
            result = Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            // Don't know what to do with this error
            //System.out.println(ex);
        }
        return result;
    }

/*
    private void getGradient(double value){
        double gradient;
        if (count <3)
        {
            dataGradient[count] = value;
            count++;
        }
        else
        {
            gradient = (-dataGradient[2] + (4 * dataGradient[1]) - (3 * dataGradient[0])) / 0.02;
            Log.d("Gradient part", "Gradient is:" + gradient);
            dataGradient[0] = dataGradient[1];
            dataGradient[1] = dataGradient[2];
            dataGradient[2] = value;
            if (gradient >1.8){
                series2.appendData(new DataPoint(temp,value),true,1000);
            }

        }
    }
    */
    //public double[] convertedData;


//    public void updateUIthread(){
//
//        if (dataString.length()>2){
//            currentLoc = dataString.lastIndexOf("\n");
//            tempString = dataString.substring(lastLoc,currentLoc);
//            lastLoc = currentLoc;
//            Thread newThred = new Thread(new Runnable() {
//            //runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    try{
//                        String[] dataBuffer = tempString.split("\n");
//
//                        if (dataBuffer.length >2){
//
//                            for(int i =0; i<dataBuffer.length;i++){
//
//                                double sec2 = System.currentTimeMillis()/1000.0;
//
//                                if (dataBuffer[i].length()>0){
//                                    if(dataBuffer[i].charAt(0)=='H' && dataBuffer[i].length() > 0)
//                                    {
//                                        hVal = dataBuffer[i].substring(1,dataBuffer[i].length());
//                                    }
//                                    else if(dataBuffer[i].charAt(0)=='S' && dataBuffer[i].length() > 0)
//                                    {
//                                        sVal = dataBuffer[i].substring(1,dataBuffer[i].length());
//
//                                    }
//                                    else if(dataBuffer[i].length() > 4)
//                                    {
//
//                                        double value = Double.parseDouble(dataBuffer[i]) /1000000.0;
//                                        Log.d("plotArea", "Got values: "+ value);
//                                        temp = temp+0.001;
//                                        series.appendData(new DataPoint(temp,value),true,2000);
//                                        //dataArray.add(value);
//                                        //Log.d("array value",""+dataArray.get(a));
//                                        //a++;
//
//                                    }
//                                }
//
//                            }
//                            //Thread.sleep(25);
//                        }
//
//                    }catch (Exception e){
//                        Log.d("Plotting","got error: "+e);
//                    }
//                }
//
//            });
//            newThred.start();
//
//        }
//
//
//    }


    public boolean fileWrite( String userName){

        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        time = formatter.format(today);
        Log.d("this username",""+userName);
        //userName = "bobby";
        fileName = userName + time +".txt";
        filePath = filePath + "/" +fileName;
        boolean success = false;
        //FileOutputStream outputStream;

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dir + File.separator + fileName), "utf-8"))) {
            writer.write(dataString.toString());
            uploadFile();
            success = true;

            Log.d("File", "Success");
        }catch (Exception e){
            Log.d("FileWriter","file write  exception: " + e);
            success = false;
        }
        return success;

    }

    public void uploadFile(){
        try{
            Uri file = Uri.fromFile(new File(dir + File.separator + fileName));
            Log.d("uploadFile method","Firebase upload read file location: "+file.getPath());
            StorageReference childRef = mStorageRef.child(FirebaseStoragePath+ File.separator + fileName);
            UploadTask uploadTask = childRef.putFile(file);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle unsuccessful uploads
                    Log.d("uploadFail", "" + e);

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Toast.makeText(MainActivity.this,"File Uploaded",Toast.LENGTH_LONG).show();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d("downloadUrl", "" + downloadUrl);
                    mDatabase.child("users").child(uid).child("files").child(time).setValue(fileName);
                }
            });
        }catch (Exception ex){
            Log.d("firebasefile upload", ""+ex);
        }


    }


    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }



    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == 1 || requestCode ==2){


            switch (requestCode) {

                case REQUEST_SELECT_DEVICE:
                    //When the DeviceListActivity return, with the selected device address
                    //Bundle bundle = data.getExtras();
                    Log.w(TAG,("Entered request select device: "));
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        Log.w(TAG,("trying to select device: "));
                        String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                        mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                        Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                        //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                        mService.connect(deviceAddress);


                    }
                    break;
                case REQUEST_ENABLE_BT:

                    // When the request to enable Bluetooth returns
                    if (resultCode == Activity.RESULT_OK) {
                        Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                    } else {
                        // User did not enable Bluetooth or an error occurred
                        Log.d(TAG, "BT not enabled");
                        Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                default:
                    Log.e(TAG, "wrong request code");
                    break;
            }
        }else{Log.w(TAG,("request code was: " + String.valueOf(requestCode)));}

    }



    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {

//            Intent startMain = new Intent(Intent.ACTION_MAIN);
//            startMain.addCategory(Intent.CATEGORY_HOME);
//            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(startMain);
//            showMessage("G13 Wireless EKG is running in background.\n             Disconnect to exit");
        }
            mService.disconnect();

            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Quit Plotting Activity")
                    .setMessage("Leaving plotting activity will disconnect the EKG Monitor, Do you want to continue?")
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
            //finish();
//        }
//        else {
//            new AlertDialog.Builder(this)
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .setTitle(R.string.popup_title)
//                    .setMessage(R.string.popup_message)
//                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            finish();
//                        }
//                    })
//                    .setNegativeButton(R.string.popup_no, null)
//                    .show();
//        }
    }
}
