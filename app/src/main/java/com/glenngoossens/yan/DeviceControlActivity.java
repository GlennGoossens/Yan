package com.glenngoossens.yan;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceControlActivity extends AppCompatActivity {

    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    private TextView deviceName,deviceAddress;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final GattClient gattClient = new GattClient();
    private Button buttonClose, buttonOpen;


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
        buttonOpen.setEnabled(false);
        buttonClose.setEnabled(true);
    }

    private void closeButton() {
        write("close");
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
