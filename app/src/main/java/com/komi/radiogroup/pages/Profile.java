package com.komi.radiogroup.pages;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.komi.radiogroup.EditProfileActivity;
import com.komi.radiogroup.GroupActivity;
import com.komi.radiogroup.R;
import com.komi.radiogroup.UserGroupsIsAdminAdapter;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.structures.Group;
import com.komi.structures.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.komi.radiogroup.MainFragment.logout;


public class Profile extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String SHARED_PREFS = "radioGroup_sp";
    public static final String SP_UID = "latest_uid";
    private static final String SP_FULLNAME = "latest_fullname";
    private static final String SP_BIO = "latest_bio";
    private static final String SP_IMAGE = "latest_image";

    final int WRITE_PERMISSION_REQUEST = 1;
    final int CAMERA_REQUEST = 1;
    final int PICK_IMAGE = 2;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String userID;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    User user;
    List<Group> groupsByUser = new ArrayList<>();
    File file;
    private StorageReference mStorageRef;

    View rootView;
    ImageView iv_profile_pic;
    TextView tv_fullName, tv_bio;
    FloatingActionButton editProfileBtn;

    RecyclerView groupsRecyclerView;
    UserGroupsIsAdminAdapter adapter;

    private boolean canTakeImage = false;
    private SharedPreferences sharedPreferences;
    private boolean isMe = false;

    public Profile(String userID) {
        // Required empty public constructor
        this.userID = userID;
    }

    public Profile(){
    }


//    public static Profile newInstance(String param1, String param2) {
//        Profile fragment = new Profile();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        //Getting storage instance
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        MaterialButton logoutBtn = rootView.findViewById(R.id.btn_logout);
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
        // TODO: to allow loading this page for any user we need to pass a uid parameter and set it here
        isMe = false;
        if(userID !=null) {
            logoutBtn.setVisibility(View.GONE);
        }else {
            isMe = true;
            userID = firebaseAuth.getCurrentUser().getUid();

            sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
            String latest_UID = sharedPreferences.getString(SP_UID, null);



            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPreferences.edit().clear().apply();
                    firebaseAuth.signOut();
                    logout();
                }
            });
            //btn_editProfile = rootView.findViewById(R.id.btn_edit_profile);


            // If the latest profile info we have saved is of the same user, then load it from shared preferences first
            if (latest_UID != null && latest_UID.matches(firebaseAuth.getCurrentUser().getUid())) {
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
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.HORIZONTAL, false));
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
            }
        });
        groupsRecyclerView.setAdapter(adapter);

        FirebaseDatabaseHelper.getInstance().setGroupsByAdminIDListener(userID, new FirebaseDatabaseHelper.OnGroupsDataChangedCallback() {
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

//        if(isMe) {
//            //Request image permissions
//            if (Build.VERSION.SDK_INT >= 23) {
//                int hasWritePermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
//                } else {
//                    canTakeImage = true;
//                }
//            } else {
//                canTakeImage = true;
//            }
//
//
//            iv_profile_pic.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    openPickDialog();
//                }
//            });
//        }
        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FirebaseDatabaseHelper.getInstance().removeGroupsByAdminIDListener();
        FirebaseDatabaseHelper.getInstance().removeUserByUIDListener();
    }
}

