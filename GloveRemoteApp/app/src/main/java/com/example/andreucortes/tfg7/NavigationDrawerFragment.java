package com.example.andreucortes.tfg7;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NavigationDrawerFragment extends Fragment {

    private RecyclerView recycleView;

    public static final String PREF_FILE_NAME = "testpref";
    public static final String KEY_USER_LEARNED_DRAWER = "user_learned_drawer";

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    private NavDrawerAdapter adapter;

    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;

    private View containerView;

    private static List<NavDrawer> sections;

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }

    private void createData() {
        sections = new ArrayList<>();
        String[] titles = {"Gestures", "Settings", "Pair"};
        int[] icons = {R.drawable.ic_thumbs_up_down_white_24dp, R.drawable.ic_settings_white_24dp, R.drawable.ic_settings_bluetooth_white_24dp};
        for (int i = 0; i < titles.length; i++) {
            NavDrawer navDrawer = new NavDrawer();
            navDrawer.title = titles[i];
            navDrawer.iconId = icons[i];
            sections.add(navDrawer);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createData();
        mUserLearnedDrawer = Boolean.valueOf(readFromPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, "false"));
        if (savedInstanceState != null) {
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        recycleView = (RecyclerView) layout.findViewById(R.id.drawerList);
        adapter = new NavDrawerAdapter(getActivity(), getData());
        recycleView.setAdapter(adapter);
        recycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycleView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recycleView, new ClickListener() {
            @Override
            public void onCLick(View view, int position) {
                switch (position){
                    case 0:
                        Intent mainActivity = new Intent(getActivity().getBaseContext(), MainActivity.class);
                        startActivity(mainActivity);
                        break;
                    case 2:
                        Intent pairingActivity = new Intent(getActivity().getBaseContext(), PairingActivity.class);
                        startActivity(pairingActivity);
                        break;
                }
            }

            @Override
            public void onLongClickListener(View view, int position) {
                Toast.makeText(getActivity(), "onLongClick"+ position, Toast.LENGTH_SHORT).show();
            }
        }));
        return layout;
    }

    public static List<NavDrawer> getData() {
        return sections;
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    saveToPreferences(getActivity(), PREF_FILE_NAME, mUserLearnedDrawer + "");
                }
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();

            }
        };
        if (mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(containerView);
        }
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String preferenceName, String preferenceValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, preferenceValue);
    }
}
