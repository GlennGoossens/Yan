package com.glenngoossens.yan;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceControlActivity extends AppCompatActivity {

    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    private TextView deviceName,deviceAddress;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final GattClient gattClient = new GattClient();
    private Button buttonClose, buttonOpen;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference = database.getReference("database");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        Intent intent = getIntent();
        buttonOpen = (Button) findViewById(R.id.buttonOpen);
        buttonOpen.setText("open");
        buttonOpen.setEnabled(false);
        buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openButton();
            }
        });

        buttonClose = (Button) findViewById(R.id.buttonClose);
        buttonClose.setEnabled(false);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeButton();
            }
        });
        gattClient.onCreate(this, intent.getStringExtra(EXTRAS_DEVICE_ADDRESS), new GattClient.OnReadListener() {

            @Override
            public void onConnected(final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                            buttonOpen.setEnabled(success);
                            if(!success){
                            Toast.makeText(DeviceControlActivity.this,"Connection error",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });




        deviceName = (TextView) findViewById(R.id.controlDeviceName);
        deviceAddress = (TextView) findViewById(R.id.controlDeviceAddress);

        deviceName.setText(intent.getStringExtra(EXTRAS_DEVICE_NAME));
        deviceAddress.setText(intent.getStringExtra(EXTRAS_DEVICE_ADDRESS));
    }

    private void openButton(){
        write("open");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        String log = String.format("opening "+ deviceAddress .toString() +" by user = test-user on " +currentDateandTime);
        reference.child("logs").push().setValue(log);
        buttonOpen.setEnabled(false);
        buttonClose.setEnabled(true);

    }

    private void closeButton() {
        write("close");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        String log = String.format("closing "+ deviceAddress.toString()+" by user = test-user on " +currentDateandTime);
        reference.child("logs").push().setValue(log);
        gattClient.onDestroy();
        Intent intent = new Intent(getApplicationContext(),DeviceScanActivity.class);
        startActivity(intent);

    }

    private void write(String valueString){
        gattClient.writeInteractor(valueString);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gattClient.onDestroy();
    }
}
