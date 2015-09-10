package com.example.andreucortes.glovebluetooth;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;

import com.bluecreation.melodysmart.BLEError;
import com.bluecreation.melodysmart.DataService;
import com.bluecreation.melodysmart.DeviceDatabase;
import com.bluecreation.melodysmart.MelodySmartDevice;
import com.bluecreation.melodysmart.MelodySmartListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GloveService extends Service implements MelodySmartListener {

    private String TAG = this.getClass().getCanonicalName();
    private String gesturesFile = "GesturesFile";

    private MelodySmartDevice device;

    private Vibrator vibrator;
    private AudioManager audioManager;

    private ArrayList<Gesture> gestures = new ArrayList<>();
    private ArrayList<Action> actions = new ArrayList<>();
    private ArrayList<String> actionsNames = new ArrayList<>();
        private HashMap<String, String[]> mapping = new HashMap<>();

    private boolean recording = false;
    private String command = "";
    private CommandListener commandListener;

    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";
    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDSTOP = "stop";

    public MelodySmartDevice getDevice(){
        return device;
    }

    public void setVibrator(Vibrator vibrator) {
        this.vibrator = vibrator;
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    private DataService.Listener dataServiceListener = new DataService.Listener() {
        @Override
        public void onReceived(final byte[] bytes) {
            Log.d(TAG, bytes.toString());

            if(vibrator != null) vibrator.vibrate(100);
            String partial = parseReceivedData(bytes);
            if (!partial.contains("e")) command += partial;
            else {
                command += partial.replace("e", "");
                command = command.replace(" 0", "");
                manageGestures(command);
                command = "";
            }
        }

        @Override
        public void onConnected(boolean b) {
            if (b) {
                device.getDataService().enableNotifications(true);
            }
        }
    };

    private String parseReceivedData(byte data[]) {
        String commandBLE = "";
        for (int i = 0; i < data.length; i++) {
            commandBLE += (char) data[i];
        }
        commandBLE = commandBLE.replaceAll(" 0", "");
        Log.d("COMMAND RECEIVED", commandBLE);
        return commandBLE;
    }

    private void manageGestures(String gesture) {
        // Doing here all related about the gesture.
        // Think about the process.
        // FIRST CHECK IF VOLUME CONTROL
        //Toast.makeText(getApplicationContext(), gesture, Toast.LENGTH_SHORT).show();

        if (recording) {
            //RECORD GESTURE
            commandListener.onReceivedCommand();
            recording = false;
        } else {
            String action[];
            Log.d(TAG, "'" + gesture.trim() + "'");
            try {
                action = mapping.get(gesture.trim());
                if(isDialCallAction(Integer.parseInt(action[0]))){
                    if(Integer.parseInt(action[0]) == indexDialAction()) dial(action[1]);
                    else if(Integer.parseInt(action[0]) == indexCallAction()) call(action[1]);
                }
                else {
                    Action act = actions.get(Integer.parseInt(action[0]));
                    act.getR().run();
                }
            }catch (NullPointerException npe){
                command = "";
            }
            command = "";
        }
    }

    @Override
    public void onDeviceConnected() {

    }

    @Override
    public void onDeviceDisconnected(BLEError bleError) {

    }

    @Override
    public void onOtauAvailable() {

    }

    @Override
    public void onOtauRecovery(DeviceDatabase.DeviceData deviceData) {

    }

    public class LocalBinder extends Binder {
        public GloveService getService() {
            return GloveService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        createFileFirstTime();
        readActionsFile();
        device = MelodySmartDevice.getInstance();
        device.init(getApplicationContext());
        device.connect(intent.getStringExtra("mAddress"));
        device.registerListener((MelodySmartListener) this);
        device.getDataService().registerListener(dataServiceListener);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        saveActionsFile();
        device.disconnect();
        device.close(getApplicationContext());
        device.disconnect();
        device.removeBond();
        return super.onUnbind(intent);
    }

    public final IBinder mBinder = new LocalBinder();

    private void saveActionsFile() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(openFileOutput(gesturesFile, MODE_PRIVATE));
            oos.reset();
            oos.writeObject(gestures);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readActionsFile() {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(openFileInput(gesturesFile));
            gestures = (ArrayList<Gesture>) ois.readObject();
            if (gestures == null) gestures = new ArrayList<>();
            refreshGestures();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                ois.close();
                refreshGestures();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createFileFirstTime() {
        ObjectOutputStream oos = null;
        try {
            List<String> files = Arrays.asList(getApplicationContext().fileList());
            if(files.indexOf(gesturesFile) == -1) {
//                oos = new ObjectOutputStream(openFileOutput(actionsFile, MODE_PRIVATE));

                preconfiguredGestures();
//                oos.writeObject(actions);
//                oos.close();
                oos = new ObjectOutputStream(openFileOutput(gesturesFile, MODE_PRIVATE));
                oos.writeObject(gestures);
                oos.close();
            }
            preconfiguredActions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void preconfiguredGestures() {
        gestures.add(new Gesture("Index Click", "108", 0));
        gestures.add(new Gesture("Index Long Click", "208", 1));
        gestures.add(new Gesture("Heart Click", "104", 2));
        gestures.add(new Gesture("Heart Long Click", "204", 3));
        gestures.add(new Gesture("Anular Click", "103", 4));
    }

    public void preconfiguredActions() {
        actions.add(new Action("Open Music", new Runnable() {
            @Override
            public void run() {
                 GloveService.this.startActivity((new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }));
        actions.add(new Action("Play'n'Pause Music", new Runnable() {
            @Override
            public void run() {
                GloveService.this.sendBroadcast((new Intent(SERVICECMD)).putExtra(CMDNAME, CMDTOGGLEPAUSE));
            }
        }));
        actions.add(new Action("Next Song", new Runnable() {
            @Override
            public void run() {
                GloveService.this.sendBroadcast((new Intent(SERVICECMD)).putExtra(CMDNAME, CMDNEXT));
            }
        }));
        actions.add(new Action("Previous Song", new Runnable() {
            @Override
            public void run() {
                GloveService.this.sendBroadcast((new Intent(SERVICECMD)).putExtra(CMDNAME, CMDPREVIOUS));
            }
        }));
        actions.add(new Action("Volume Up", new Runnable() {
            @Override
            public void run() {
                parseVolumeUp(command);
            }
        }));
        actions.add(new Action("Volume Down", new Runnable() {
            @Override
            public void run() {
                parseVolumeDown(command);
            }
        }));
        actions.add(new Action("Dial Contact", null));
        actions.add(new Action("Call Contact", null));
        for(int i = 0; i < actions.size(); i++){
            actionsNames.add(actions.get(i).getName());
        }
    }

    private boolean parseVolumeUp(String gesture) {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        String nums[] = gesture.split(" 100 ");
        for (int i = 0; i < nums.length; i++) {
            if ((nums[i].trim()).contentEquals("108"))
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        }
        Log.d("NUMSup", nums.length+"");
        return true;
    }

    private boolean parseVolumeDown(String gesture) {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        String nums[] = gesture.split(" 100 ");
        for (int i = 0; i < nums.length; i++) {
            if ((nums[i].trim()).contentEquals("208"))
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        }
        Log.d("NUMSdown", nums.length+"");
        return true;
    }

    public boolean isDialCallAction(int action){
        return ((action == indexCallAction()) || (action == indexDialAction()));
    }

    public int indexDialAction(){
        return actionsNames.indexOf("Dial Contact");
    }

    public int indexCallAction(){
        return actionsNames.indexOf("Call Contact");
    }

    public void dial(final String tel){
        Action newAction = new Action("Dial", new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+tel)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        Log.d(TAG, "starting dial");
        newAction.getR().run();
    }

    public void call(final String tel){
        Action newAction = new Action("Call", new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+tel)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        newAction.getR().run();
    }


    public int createAction(final String type){
        return actionsNames.indexOf(type);
    }







    public ArrayList<Gesture> getGestures() {
        return gestures;
    }

    public ArrayList<String> getActionsNames(){
        return  actionsNames;
    }








    public void createGesture(Gesture gesture) {
        gestures.add(gesture);
        saveActionsFile();
        refreshGestures();
    }

    public void updateGesture(Gesture gesture, int position) {
        gestures.remove(position);
        gestures.add(position, gesture);
        saveActionsFile();
        refreshGestures();
    }

    public void removeGesture(int position){
        gestures.remove(position);
        saveActionsFile();
        refreshGestures();
    }

    private void refreshGestures(){
        mapping.clear();
        for(int i = 0; i < gestures.size(); i++){
            Gesture gest = gestures.get(i);
            mapping.put(gest.getCommandBLE().trim(), gest.getAction());
        }
        mapping.size();
    }















    public void setRecordingMode(boolean state){
        recording = state;
    }

    public boolean getRecordingMode(){
        return recording;
    }

    public String getLastReceivedCommand(){
        return command;
    }


    public interface CommandListener{
        void onReceivedCommand();
    }

    public void setCommandListener(CommandListener commandListener){
        this.commandListener = commandListener;
    }
}
