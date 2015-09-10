package com.example.andreucortes.glovebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

public class GloveBluetooth{

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> bluetoothDevices;

    private Handler mHandler;
    private int SCAN_PERIOD = 3000;

    private String deviceAddress;

    public GloveBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothDevices = new ArrayList<>();

        mHandler = new Handler();
    }

    /**
     * Method that scans for Gloves
     * <p/>
     * Check result with connectGlove. It stands for 5 sec.
     */
    public void scanForGloves(boolean enable, final Runnable interfaceCallback) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    bluetoothAdapter.cancelDiscovery();
                    interfaceCallback.run();
                }
            }, SCAN_PERIOD);

            bluetoothDevices.clear();
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (filterDevice(device)) {
                        bluetoothDevices.add(device);
                        Log.d("DEVICE", "Added " + device.toString());
                    }
                }
            };

    /**
     * This method filter if the device passed by parameter is a Glove for connecting to.
     *
     * @param bluetoothDevice
     * @return boolean if is a TFG-Glove
     */
    boolean filterDevice(BluetoothDevice bluetoothDevice) {
//        return (bluetoothDevice.getAddress().contains(SampleGattAttributes.GLOVE_MANUFACTURER));
        return true;
    }

    public ArrayList<BluetoothDevice> getBluetoothDevices() {
        return bluetoothDevices;
    }
}