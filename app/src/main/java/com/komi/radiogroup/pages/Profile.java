package com.komi.radiogroup.pages;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.komi.radiogroup.R;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.structures.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String SHARED_PREFS = "radioGroup_sp";
    private static final String SP_UID = "latest_uid";
    private static final String SP_FULLNAME = "latest_fullname";
    private static final String SP_BIO = "latest_bio";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    User user;

    View rootView;
    ImageView iv_profile_pic;
    TextView tv_fullName, tv_bio;
    Button btn_editProfile;

    public Profile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Account.
     */
    // TODO: Rename and change types and number of parameters
    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        iv_profile_pic = rootView.findViewById(R.id.iv_profile_pic);
        tv_fullName = rootView.findViewById(R.id.tv_full_name);
        tv_bio = rootView.findViewById(R.id.tv_bio);
        btn_editProfile = rootView.findViewById(R.id.btn_edit_profile);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String latest_UID = sharedPreferences.getString(SP_UID, null);

        // If the latest profile info we have saved is of the same user, then load it from shared preferences first
        if(latest_UID != null && latest_UID.matches(firebaseAuth.getCurrentUser().getUid())){
            Toast.makeText(getActivity(), "Loading from SP", Toast.LENGTH_SHORT).show();
            String latest_fullname = sharedPreferences.getString(SP_FULLNAME, null);
            String latest_bio = sharedPreferences.getString(SP_BIO, null);

            if (latest_fullname != null)
                tv_fullName.setText(latest_fullname);
            if (latest_bio != null)
                tv_bio.setText(latest_bio);


        }

        // Loading current users details into
        new Thread(new Runnable() {
            @Override
            public void run() {
                user = FirebaseDatabaseHelper.getInstance().getUserByUID(firebaseAuth.getCurrentUser().getUid());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        tv_fullName.setText(user.getFullname());
                        tv_bio.setText(user.getBio());
                        // TODO: get the profile pic from storage and set it
                        //iv_profile_pic.setImageResource();

                        // Saving latest profile info to shared preferences
                        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(SP_UID, firebaseAuth.getCurrentUser().getUid());
                        editor.putString(SP_FULLNAME, user.getFullname());
                        editor.putString(SP_BIO, user.getBio());
                        editor.apply();
                        Toast.makeText(getActivity(), "Saved to SP", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        }).start();

        btn_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return rootView;
    }
}