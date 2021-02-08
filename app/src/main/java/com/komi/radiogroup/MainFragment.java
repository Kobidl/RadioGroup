package com.komi.radiogroup;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.FirebaseDatabase;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.radiogroup.pages.Explore;
import com.komi.radiogroup.pages.Groups;
import com.komi.radiogroup.pages.Profile;
import com.komi.radiogroup.pages.Welcome;

import java.util.Objects;

public class MainFragment extends Fragment {

    BottomNavigationView bottomNavigationView;
    Profile profileFragment = new Profile();
    Groups groupsFragment = new Groups();
    Explore exploreFragment = new Explore();
    private FragmentActivity myContext;

    public MainFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabaseHelper firebaseDatabaseHelper = FirebaseDatabaseHelper.getInstance(); //This is to make sure the listeners will start working and data will be available when needed
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        bottomNavigationView = rootView.findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_item_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Toast.makeText(rootView.getContext(), "selected:" + item.getTitle(), Toast.LENGTH_SHORT).show();
                switch (item.getItemId()){
                    case R.id.bottom_navigation_item_profile:
                        assert getFragmentManager() != null;
                        getFragmentManager().beginTransaction().replace(R.id.main_frame_layout, profileFragment).commit();
                        return true;
                    case R.id.bottom_navigation_item_explore:
                        assert getFragmentManager() != null;
                        getFragmentManager().beginTransaction().replace(R.id.main_frame_layout, exploreFragment).commit();
                        return true;
                    case R.id.bottom_navigation_item_group:
                        assert getFragmentManager() != null;
                        getFragmentManager().beginTransaction().replace(R.id.main_frame_layout, groupsFragment).commit();
                        return true;
                }
                return false;
            }
        });
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FirebaseDatabaseHelper.getInstance().removeListeners();
    }
}