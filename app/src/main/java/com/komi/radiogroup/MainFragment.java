package com.komi.radiogroup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.radiogroup.pages.Explore;
import com.komi.radiogroup.pages.Groups;
import com.komi.radiogroup.pages.Profile;

import static com.komi.radiogroup.pages.Profile.SHARED_PREFS;


public class MainFragment extends Fragment {

    BottomNavigationView bottomNavigationView;
    Profile profileFragment = new Profile();
    Groups groupsFragment = new Groups();
    Explore exploreFragment = new Explore();
    private FragmentActivity myContext;
    static MainFragmentListener callback;
    ImageView rightToolbarBtn;

    public MainFragment() {
        // Required empty public constructor
    }

    interface MainFragmentListener{
        void onLogout();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (MainFragmentListener) context;
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

        //Set toolbar
        final TextView titleTV = rootView.findViewById(R.id.toolbar_title);
        titleTV.setText(R.string.group_details);
        ImageButton backBtn = rootView.findViewById(R.id.toolbar_back_btn);
        backBtn.setVisibility(View.GONE);

        bottomNavigationView = rootView.findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_item_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.bottom_navigation_item_profile:
                        assert getFragmentManager() != null;
                        getFragmentManager().beginTransaction().replace(R.id.main_frame_layout, profileFragment).commit();
                        titleTV.setText(R.string.profile);

                        rightToolbarBtn = rootView.findViewById(R.id.toolbar_right_icon);
                        rightToolbarBtn.setVisibility(View.VISIBLE);
                        rightToolbarBtn.setImageResource(R.drawable.logout);
                        rightToolbarBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                                sharedPreferences.edit().clear().apply();
                                FirebaseAuth.getInstance().signOut();
                                logout();
                            }
                        });
//                        toolbarMenu.findItem(R.id.add_group_btn).setVisible(false);
                        return true;
                    case R.id.bottom_navigation_item_explore:
                        assert getFragmentManager() != null;
                        getFragmentManager().beginTransaction().replace(R.id.main_frame_layout, exploreFragment).commit();
                        titleTV.setText(R.string.explore);
//                        toolbarMenu.findItem(R.id.add_group_btn).setVisible(false);
                        if(rightToolbarBtn!=null) {
                            rightToolbarBtn.setOnClickListener(null);
                            rightToolbarBtn.setVisibility(View.GONE);
                            rightToolbarBtn = null;
                        }
                        return true;
                    case R.id.bottom_navigation_item_group:
                        assert getFragmentManager() != null;
//                        toolbarMenu.findItem(R.id.add_group_btn).setVisible(true).setEnabled(true);
                        getFragmentManager().beginTransaction().replace(R.id.main_frame_layout, groupsFragment).commit();
                        titleTV.setText(R.string.app_name);
                        if(rightToolbarBtn!=null) {
                            rightToolbarBtn.setOnClickListener(null);
                            rightToolbarBtn.setVisibility(View.GONE);
                            rightToolbarBtn = null;
                        }
                        return true;
                }
                return false;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_item_group);
        return rootView;
    }

    public static void logout(){
        if(callback!=null)
            callback.onLogout();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}