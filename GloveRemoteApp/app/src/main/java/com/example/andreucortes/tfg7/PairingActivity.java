package com.example.andreucortes.tfg7;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.andreucortes.glovebluetooth.GloveBluetooth;


public class PairingActivity extends ActionBarActivity implements OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ScrollView scrollView;

    private ViewFlipper viewFlipper;

    private RecyclerView recyclerView;
    private GloveBluetooth gloveBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        gloveBluetooth = new GloveBluetooth();

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.scanView);
        swipeRefreshLayout.setOnRefreshListener(this);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipperResult);

        recyclerView = (RecyclerView) findViewById(R.id.scanResultDevicesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new ClickListener() {
            @Override
            public void onCLick(View view, int position) {
                Intent gestures = new Intent(PairingActivity.this.getBaseContext(), MainActivity.class);
                gestures.putExtra("deviceName", gloveBluetooth.getBluetoothDevices().get(position).getName());
                gestures.putExtra("deviceAddress", gloveBluetooth.getBluetoothDevices().get(position).getAddress());
                gestures.putExtra("device", gloveBluetooth.getBluetoothDevices().get(position));
                startActivity(gestures);
            }

            @Override
            public void onLongClickListener(View view, int position) {
                Toast.makeText(PairingActivity.this, "onLongClick" + position, Toast.LENGTH_SHORT).show();
            }
        }));

        scrollView = (ScrollView) findViewById(R.id.scrollScanResult);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onRefresh() {
        gloveBluetooth.scanForGloves(true, new Runnable() {
            @Override
            public void run() {
                recyclerView.setAdapter(new PairingDeviceListAdapter(PairingActivity.this, gloveBluetooth.getBluetoothDevices()));
                swipeRefreshLayout.setRefreshing(false);
                scrollView.setFillViewport(true);
                if (gloveBluetooth.getBluetoothDevices().isEmpty())
                    viewFlipper.setDisplayedChild(0);
                else viewFlipper.setDisplayedChild(1);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
    }
}