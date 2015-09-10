package com.example.andreucortes.tfg7;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by andreucortes on 17/5/15.
 */
public class NavDrawerAdapter extends RecyclerView.Adapter<NavDrawerAdapter.NavDrawerViewHolder> {
    private LayoutInflater inflater;
    Context context;

    List<NavDrawer> data = Collections.emptyList();

    public NavDrawerAdapter(Context context, List<NavDrawer> data) {
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
    }

    @Override
    public NavDrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.drawer_row, parent, false);
        NavDrawerViewHolder holder = new NavDrawerViewHolder(context, view);
        return holder;
    }

    @Override
    public void onBindViewHolder(NavDrawerViewHolder holder, int position) {
        NavDrawer navDrawer = data.get(position);
        holder.title.setText(navDrawer.title);
        holder.icon.setImageResource(navDrawer.iconId);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class NavDrawerViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;

        public NavDrawerViewHolder(Context context, View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.listText);
            icon = (ImageView) itemView.findViewById(R.id.listIcon);


        }
    }
}
