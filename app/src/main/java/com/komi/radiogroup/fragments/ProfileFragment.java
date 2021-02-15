package com.komi.radiogroup.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.komi.radiogroup.EditProfileActivity;
import com.komi.radiogroup.GroupActivity;
import com.komi.radiogroup.R;
import com.komi.radiogroup.adapters.UserGroupsIsAdminAdapter;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.structures.Group;
import com.komi.structures.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String SHARED_PREFS = "radioGroup_sp";
    public static final String SP_UID = "latest_uid";
    private static final String SP_FULLNAME = "latest_fullname";
    private static final String SP_BIO = "latest_bio";
    private static final String SP_IMAGE = "latest_image";

    String userID;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    User user;
    List<Group> groupsByUser = new ArrayList<>();
    File file;

    View rootView;
    ImageView iv_profile_pic;
    TextView tv_fullName, tv_bio;
    FloatingActionButton editProfileBtn;

    RecyclerView groupsRecyclerView;
    UserGroupsIsAdminAdapter adapter;

    private SharedPreferences sharedPreferences;
    private boolean isMe = true;

    public interface ProfileFragmentListener{
        void onEnterGroup();
    }

    ProfileFragmentListener listener;

    public ProfileFragment(String userID,ProfileFragmentListener listener) {
        this.userID = userID;
        this.isMe = false;
        this.listener = listener;
    }

    public ProfileFragment(){
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        //Getting storage instance
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();


        iv_profile_pic = rootView.findViewById(R.id.iv_profile_pic);
        tv_fullName = rootView.findViewById(R.id.tv_full_name);
        tv_bio = rootView.findViewById(R.id.tv_bio);
        editProfileBtn = rootView.findViewById(R.id.edit_profile_btn);
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(rootView.getContext(), EditProfileActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });
        editProfileBtn.setVisibility(View.GONE);

        if(isMe){
            userID = firebaseAuth.getCurrentUser().getUid();

            sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
            String latest_UID = sharedPreferences.getString(SP_UID, null);


            // If the latest profile info we have saved is of the same user, then load it from shared preferences first
            if (latest_UID != null && latest_UID.matches(firebaseAuth.getCurrentUser().getUid())) {
                editProfileBtn.setVisibility(View.VISIBLE);
                String latest_fullname = sharedPreferences.getString(SP_FULLNAME, null);
                String latest_bio = sharedPreferences.getString(SP_BIO, null);
                String latest_image = sharedPreferences.getString(SP_IMAGE, null);
                if (latest_fullname != null)
                    tv_fullName.setText(latest_fullname);
                if (latest_bio != null)
                    tv_bio.setText(latest_bio);
                if (latest_image != null) {
                    Glide.with(this).load(latest_image).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv_profile_pic);
                }
            }
        }

        // Initializing user's Groups recyclerview and setting Listeners for data
        groupsRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_groups_by_user);
        groupsRecyclerView.setHasFixedSize(true);
        groupsRecyclerView.setLayoutManager(new GridLayoutManager(rootView.getContext(), 2));
        adapter = new UserGroupsIsAdminAdapter(groupsByUser);
        adapter.setListener(new UserGroupsIsAdminAdapter.UserGroupListener() {
            @Override
            public void onClick(int position) {
//                if(listener!=null){
//                    listener.onGroupClicked();
//                }
                Group group = groupsByUser.get(position);
                Intent intent = new Intent(getContext(), GroupActivity.class);
                intent.putExtra("group",group);
                startActivity(intent);
                if(listener!=null){
                    listener.onEnterGroup();
                }
            }
        });
        groupsRecyclerView.setAdapter(adapter);

        isMe = userID.equals(currentUser.getUid());

        FirebaseDatabaseHelper.getInstance().setGroupsByAdminIDListener(userID,isMe, new FirebaseDatabaseHelper.OnGroupsDataChangedCallback() {
            @Override
            public void onDataReceived(List<Group> groups) {
                groupsByUser = groups;
                adapter.setGroups(groups);
                adapter.notifyDataSetChanged();
            }
        });

        FirebaseDatabaseHelper.getInstance().setUserByUidListener(userID, new FirebaseDatabaseHelper.OnUserDataChangedCallback() {
            @Override
            public void onDataReceived(User nUser) {
                user = nUser;
                if(isMe)
                    editProfileBtn.setVisibility(View.VISIBLE);
                tv_fullName.setText(user.getFullname());
                tv_bio.setText(user.getBio());

                // Getting profile pic from storage and setting it
                if (user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
                    if (getActivity() != null) {
                        Glide.with(getContext())
                                .load(user.getProfilePicturePath())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(iv_profile_pic);
                    } else { // Set default profile pic
                        iv_profile_pic.setImageResource(R.drawable.default_profile_pic);
                    }

                    // Saving latest profile info to shared preferences
                    if(isMe) {
                        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(SP_UID, firebaseAuth.getCurrentUser().getUid());
                        editor.putString(SP_FULLNAME, user.getFullname());
                        editor.putString(SP_BIO, user.getBio());
                        editor.putString(SP_IMAGE, user.getProfilePicturePath());
                        editor.apply();
                    }
                }
            }
        });


        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FirebaseDatabaseHelper.getInstance().removeGroupsByAdminIDListener();
        FirebaseDatabaseHelper.getInstance().removeUserByUIDListener();

    }
}

