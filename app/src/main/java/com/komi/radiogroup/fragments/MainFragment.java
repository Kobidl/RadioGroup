package com.komi.radiogroup.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.komi.radiogroup.R;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;

import static com.komi.radiogroup.fragments.ProfileFragment.SHARED_PREFS;


public class MainFragment extends Fragment {

    BottomNavigationView bottomNavigationView;
    ProfileFragment profileFragment = new ProfileFragment();
    GroupsFragment groupsFragment = new GroupsFragment();
    ExploreFragment exploreFragment = new ExploreFragment();
    private FragmentActivity myContext;
    static MainFragmentListener callback;
    ImageView rightToolbarBtn;

    public MainFragment() {
        // Required empty public constructor
    }

    public interface MainFragmentListener{
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
        //bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_item_explore);
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
                               logout();
                            }
                        });
//                        toolbarMenu.findItem(R.id.add_group_btn).setVisible(false);
                        return true;
                    case R.id.bottom_navigation_item_explore:
                        assert getFragmentManager() != null;
                        getFragmentManager().beginTransaction().replace(R.id.main_frame_layout, exploreFragment).commit();
                        titleTV.setText(R.string.explore);
                        rightToolbarBtn = rootView.findViewById(R.id.toolbar_right_icon);
                        rightToolbarBtn.setVisibility(View.VISIBLE);
                        rightToolbarBtn.setImageResource(R.drawable.ic_search);
                        rightToolbarBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                exploreFragment.showSearch();
                            }
                        });
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

    public void logout(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(getResources().getString(R.string.logout));
        builder.setMessage(getResources().getString(R.string.are_you_sure_logout));

        builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                sharedPreferences.edit().clear().apply();
                FirebaseAuth.getInstance().signOut();
                if(callback!=null)
                    callback.onLogout();
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}