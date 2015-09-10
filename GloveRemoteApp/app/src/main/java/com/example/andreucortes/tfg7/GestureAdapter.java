package com.example.andreucortes.tfg7;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.andreucortes.glovebluetooth.Gesture;
import com.example.andreucortes.glovebluetooth.GloveService;

import java.util.Collections;
import java.util.List;

/**
 * Created by andreucortes on 20/5/15.
 */
public class GestureAdapter extends RecyclerView.Adapter<GestureAdapter.GestureViewHolder>{
    private LayoutInflater inflater;
    List<Gesture> gestures = Collections.emptyList();

    public GestureAdapter(Context context, GloveService service){
        inflater = LayoutInflater.from(context);
        this.gestures = service.getGestures();
    }

    @Override
    public GestureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.gesture_card, parent, false);
        GestureViewHolder holder = new GestureViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(GestureViewHolder holder, int position) {
        Gesture gesture = gestures.get(position);
        holder.title.setText(gesture.getTitle());
    }

    @Override
    public int getItemCount() {
        return gestures.size();
    }

    class GestureViewHolder extends RecyclerView.ViewHolder{
        TextView title;

        public GestureViewHolder(View itemView){
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.gesture_title);
        }
    }
}
