package com.example.andreucortes.tfg7;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bluecreation.melodysmart.MelodySmartDevice;
import com.example.andreucortes.glovebluetooth.Gesture;
import com.example.andreucortes.glovebluetooth.GloveService;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private String TAG = this.getClass().getCanonicalName();

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ViewFlipper viewFlipper;
    private Button buttonPairing;
    private FloatingActionButton floatingActionButton;

    private MelodySmartDevice device;
    private GloveService mGloveService;
    private String mDeviceAddress;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager notificationManager;
    private Vibrator vibrator;
    private AudioManager audioManager;

    private Intent gloveService;
    private String command;
    private ArrayList<String> contactsNames = new ArrayList<>();
    private ArrayList<String> contactsNumbers = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, final IBinder service) {
            mGloveService = ((GloveService.LocalBinder) service).getService();
            mGloveService.setVibrator(vibrator);
            mGloveService.setAudioManager(audioManager);
            device = mGloveService.getDevice();
            if (!device.isBonded() && mDeviceAddress == null) {
                editor.remove("address").commit();
                viewFlipper.setDisplayedChild(1);
            } else {
                setRecyclerViewAdapter();
                viewFlipper.setDisplayedChild(0);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            device.disconnect();
            mGloveService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.card_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mGloveService.removeGesture(viewHolder.getAdapterPosition());
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper_main);
        buttonPairing = (Button) findViewById(R.id.go_pairing_glove_from_main_activity);
        buttonPairing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (device != null) device.disconnect();
                editor.remove("address").commit();
                Intent pairingActivity = new Intent(getBaseContext(), PairingActivity.class);
                startActivity(pairingActivity);
            }
        });
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fabAddGesture);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configureCustomDialog(true, null, -1);
            }
        });

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this).setContentTitle("Glove Service").setContentText("Glove Data").setSmallIcon(R.drawable.notification_template_icon_bg);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        gloveService = new Intent(this, GloveService.class);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.commit();

        mDeviceAddress = getIntent().getStringExtra("deviceAddress");

        if (mDeviceAddress != null) {
            editor.putString("address", mDeviceAddress).commit();
            gloveService.putExtra("mAddress", mDeviceAddress);
            bindService(gloveService, mServiceConnection, BIND_AUTO_CREATE);
        }
        if(device == null){
            viewFlipper.setDisplayedChild(1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getContactsInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGloveService.unbindService(mServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_go_pairing) {
            if (device != null) device.disconnect();
            editor.remove("address").commit();
            Intent pairingIntent = new Intent(getBaseContext(), PairingActivity.class);
            startActivity(pairingIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setRecyclerViewAdapter() {
        recyclerView.setAdapter(new GestureAdapter(MainActivity.this, mGloveService));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(MainActivity.this, recyclerView, new ClickListener() {
            @Override
            public void onCLick(View view, int position) {
                configureCustomDialog(false, mGloveService.getGestures().get(position), position);
            }

            @Override
            public void onLongClickListener(View view, int position) {
            }
        }));
    }

    private void createGesture(Gesture gesture) {
        mGloveService.createGesture(gesture);
        recyclerView.requestLayout();
    }

    private void updateGesture(Gesture gesture, int position) {
        mGloveService.updateGesture(gesture, position);
        recyclerView.requestLayout();
        recyclerView.getAdapter().notifyItemChanged(position);
    }

    private void configureCustomDialog(final boolean create, final Gesture gesture, final int position) {
        final Dialog dialog = new Dialog(MainActivity.this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.edit_gesture_fragment);

        TextView gestureTitleBefore = (TextView) dialog.findViewById(R.id.gesture_title_before);
        TextView actionsSpinnerBefore = (TextView) dialog.findViewById(R.id.category_actions_category_before);
        EditText gestureTitle = (EditText) dialog.findViewById(R.id.gesture_title);
        Spinner actionsSpinner = (Spinner) dialog.findViewById(R.id.actions_spinner);
        final ArrayList<String> enabledActions = mGloveService.getActionsNames();
        actionsSpinner.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, enabledActions));

        if (!create) {
            gestureTitleBefore.setText(gesture.getTitle());
            gestureTitle.setText(gesture.getTitle());
            if (gesture.hasAction()) {
                actionsSpinnerBefore.setText(mGloveService.getActionsNames().get(Integer.parseInt(gesture.getAction()[0])));
            }
            if(gesture.hasAction()){
                actionsSpinner.setSelection(Integer.parseInt(gesture.getAction()[0]));
                if(isDialCallSelected(actionsSpinner)){
                    Spinner contactsSpinner = (Spinner) dialog.findViewById(R.id.contacts_spinner);
                    contactsSpinner.setSelection(contactsNumbers.indexOf(Integer.parseInt(gesture.getTelf())));
                }
            }
        }

        mGloveService.setCommandListener(new GloveService.CommandListener() {
            @Override
            public void onReceivedCommand() {
                command = mGloveService.getLastReceivedCommand();
                mGloveService.setRecordingMode(false);
                Log.d(TAG, "command saved " + command);
            }
        });

        Button rec = (Button) dialog.findViewById(R.id.record_gesture);
        rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGloveService.setRecordingMode(true);
                Log.d(TAG, "recording state " + mGloveService.getRecordingMode());
            }
        });

        actionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner contactsSpinner = (Spinner) dialog.findViewById(R.id.contacts_spinner);
                if (enabledActions.get(position).contains("Dial") || enabledActions.get(position).contains("Call")) {
                    contactsSpinner.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, contactsNames));
                    contactsSpinner.setVisibility(View.VISIBLE);
                } else contactsSpinner.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        dialog.show();

        Button accept = (Button) dialog.findViewById(R.id.accept_button);
        Button cancel = (Button) dialog.findViewById(R.id.cancel_button);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner actionsSpinner = (Spinner) dialog.findViewById(R.id.actions_spinner);
                Spinner contactsSpinner = (Spinner) dialog.findViewById(R.id.contacts_spinner);
                Gesture acceptedGesture = (create ? new Gesture() : gesture);
                if(command != "") {
                    acceptedGesture.setCommandBLE(command);
                    Toast.makeText(getApplicationContext(), command, Toast.LENGTH_SHORT).show();
                    command = "";
                }

                String text = ((EditText) dialog.findViewById(R.id.gesture_title)).getText().toString();
                if (!text.equals("")) {
                    acceptedGesture.setTitle(text);

                    if (actionsSpinner.getSelectedItem() != null) {

                        if ((isDialCallSelected(actionsSpinner) && isContactSelected(contactsSpinner)) || !isDialCallSelected(actionsSpinner)) {

                            if (acceptedGesture.getCommandBLE() != null) {

                                if (isDialSelected(actionsSpinner)) {
                                    acceptedGesture.setAction(mGloveService.indexDialAction());
                                    acceptedGesture.setTelf(contactsNumbers.get(contactsSpinner.getSelectedItemPosition()));
                                } else if (isCallSelected(actionsSpinner)) {
                                    acceptedGesture.setAction(mGloveService.indexCallAction());
                                    acceptedGesture.setTelf(contactsNumbers.get(contactsSpinner.getSelectedItemPosition()));
                                } else {
                                    acceptedGesture.setAction(mGloveService.createAction((String) actionsSpinner.getSelectedItem()));
                                }

                                if (position != -1) updateGesture(acceptedGesture, position);
                                else createGesture(acceptedGesture);
                                dialog.dismiss();

                            } else {
                                Toast.makeText(getApplicationContext(), "You must save a Gesture", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            contactsSpinner.setBackgroundColor(getResources().getColor(R.color.accentColor));
                            Toast.makeText(getApplicationContext(), "You must choose a Contact", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        actionsSpinner.setBackgroundColor(getResources().getColor(R.color.accentColor));
                        Toast.makeText(getApplicationContext(), "You must choose an Action", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //gestureTitle.setHighlightColor(getResources().getColor(R.color.accentColor));
                    Toast.makeText(getApplicationContext(), "You must save a Title", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void getContactsInfo() {
        ContentResolver cr = this.getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor.moveToFirst()) {

            do {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        contactsNames.add(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                        contactsNumbers.add(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        break;
                    }
                    pCur.close();
                }

            } while (cursor.moveToNext());
        }
    }

    private boolean isDialCallSelected(Spinner actionsSpinner) {
        return isDialSelected(actionsSpinner) || isCallSelected(actionsSpinner);
    }

    private boolean isDialSelected(Spinner actionsSpinner) {
        return ((String) actionsSpinner.getSelectedItem()).contentEquals("Dial Contact");
    }

    private boolean isCallSelected(Spinner actionsSpinner) {
        return ((String) actionsSpinner.getSelectedItem()).contentEquals("Call Contact");
    }

    private boolean isContactSelected(Spinner contactSpinner) {
        return ((String)contactSpinner.getSelectedItem()) != null;
    }
}
