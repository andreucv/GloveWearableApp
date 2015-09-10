package com.example.andreucortes.tfg7;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by andreucortes on 18/6/15.
 */
public class PairingDeviceListAdapter extends RecyclerView.Adapter<PairingDeviceListAdapter.PairingViewHolder> {
    private LayoutInflater inflater;
    private Context context;

    List<BluetoothDevice> data = Collections.emptyList();

    public PairingDeviceListAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        super();
        data = devices;
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public PairingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(com.example.andreucortes.tfg7.R.layout.scan_result_layout, parent, false);
        PairingViewHolder holder = new PairingViewHolder(context, view);
        return holder;
    }

    @Override
    public void onBindViewHolder(PairingViewHolder holder, int position) {
        BluetoothDevice bluetoothDevice = data.get(position);
        holder.deviceName.setText(bluetoothDevice.getName());
        holder.deviceAddress.setText(bluetoothDevice.getAddress());
    }

    public BluetoothDevice getDevice(int position) {
        return data.get(position);
    }

    public void clear() {
        data.clear();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /*public View getView(int i, View view, ViewGroup viewGroup) {
        PairingViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = inflater.inflate(R.layout.scan_result_layout, null);
            viewHolder = new PairingViewHolder(context, view);
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (PairingViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(i);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }*/

    class PairingViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;

        public PairingViewHolder(Context context, View itemView) {
            super(itemView);
            deviceName = (TextView) itemView.findViewById(com.example.andreucortes.tfg7.R.id.device_name);
            deviceAddress = (TextView) itemView.findViewById(com.example.andreucortes.tfg7.R.id.device_address);
        }
    }
}
