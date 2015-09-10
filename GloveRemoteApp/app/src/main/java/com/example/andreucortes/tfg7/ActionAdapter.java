package com.example.andreucortes.tfg7;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.example.andreucortes.glovebluetooth.Action;

import java.util.List;

/**
 * Created by andreucortes on 01/08/15.
 */
public class ActionAdapter extends ArrayAdapter<Action> {
    public ActionAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ActionAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public ActionAdapter(Context context, int resource, Action[] objects) {
        super(context, resource, objects);
    }

    public ActionAdapter(Context context, int resource, int textViewResourceId, Action[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public ActionAdapter(Context context, int resource, List<Action> objects) {
        super(context, resource, objects);
    }

    public ActionAdapter(Context context, int resource, int textViewResourceId, List<Action> objects) {
        super(context, resource, textViewResourceId, objects);
    }
}
