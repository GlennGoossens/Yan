package com.glenngoossens.yan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * Created by Glenn on 30-5-2017.
 */

public class GattClient {

    private static final String TAG = GattClient.class.getSimpleName();

    public interface OnReadListener {
        void onConnected(boolean success);
    }

    private Context context;
    private OnReadListener listener;
    private String deviceAddress;

    private static final UUID SERVICE_UUID = UUID.fromString("795090c7-420d-4048-a24e-18e60180e23c");
    private static UUID CHARACTERISTIC_INTERACTOR_UUID = UUID.fromString("0b89d2d4-0ea6-4141-86bb-0c5fb91ab14a");

    private BluetoothManager manager;
    private BluetoothAdapter adapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                listener.onConnected(false);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                boolean connected = false;

                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    if ((service.getCharacteristic(CHARACTERISTIC_INTERACTOR_UUID) != null)) {
                        connected = true;
                    }
                }
                listener.onConnected(connected);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
    };
    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    startClient();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopClient();
                    break;
                default:
                    break;
            }
        }
    };

    public void onCreate(Context context, String deviceAddress, OnReadListener listener) throws RuntimeException {
        this.context = context;
        this.listener = listener;
        this.deviceAddress = deviceAddress;

        this.manager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        this.adapter = manager.getAdapter();
        if (!checkBluetoothSupport(adapter)) {
            throw new RuntimeException("GATT client requires Bluetooth support");
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.context.registerReceiver(mBluetoothReceiver, filter);
        if (!adapter.isEnabled()) {
            Log.w(TAG, "Bluetooth is currently disabled... enabling");
            adapter.enable();
        } else {
            Log.i(TAG, "Bluetooth enabled... starting client");
            startClient();
        }
    }

    public void onDestroy() {
        listener = null;

        BluetoothAdapter bluetoothAdapter = manager.getAdapter();
        if (bluetoothAdapter.isEnabled()) {
            stopClient();
        }

        context.unregisterReceiver(mBluetoothReceiver);
    }

    public void writeInteractor(String value) {
        BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
        BluetoothGattCharacteristic interactor = service
                .getCharacteristic(CHARACTERISTIC_INTERACTOR_UUID);
        interactor.setValue(value);
        bluetoothGatt.writeCharacteristic(interactor);
    }

    private boolean checkBluetoothSupport(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth is not supported");
            return false;
        }

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w(TAG, "Bluetooth LE is not supported");
            return false;
        }

        return true;
    }

    private void startClient() {
        BluetoothDevice bluetoothDevice = adapter.getRemoteDevice(deviceAddress);
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, gattCallback);
        if (bluetoothGatt == null) {
            Log.w(TAG, "Unable to create GATT client");
            return;
        }
    }

    private void stopClient() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }

        if (adapter != null) {
            adapter = null;
        }
    }

}
