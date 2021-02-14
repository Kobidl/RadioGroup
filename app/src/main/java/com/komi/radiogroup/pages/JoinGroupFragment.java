package com.komi.radiogroup.pages;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.komi.radiogroup.GroupActivity;
import com.komi.radiogroup.R;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.structures.Group;
import com.komi.structures.User;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

import static com.komi.radiogroup.pages.Profile.SHARED_PREFS;
import static com.komi.radiogroup.pages.Profile.SP_UID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JoinGroupFragment#} factory method to
 * create an instance of this fragment.
 */
public class JoinGroupFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View rootView;
    private JoinGroupCallback callback;

    Group group;

    public JoinGroupFragment(Group group) {
        this.group = group;
    }

    public interface  JoinGroupCallback{
        void onJoinedGroup(Group group);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (JoinGroupFragment.JoinGroupCallback) context;
    }

//    public static JoinGroupFragment newInstance(String param1, String param2) {
//        JoinGroupFragment fragment = new JoinGroupFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_join_group, container, false);

        final ImageView imageView = rootView.findViewById(R.id.join_group_image);
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();

        imageView.setMaxHeight(metrics.widthPixels-100);

        if(group.getProfilePicturePath() != null) {
            Glide.with(imageView).load(group.getProfilePicturePath()).into(imageView);
        }else{
            //todo: show empty image
        }


        TextView nameTV = rootView.findViewById(R.id.join_group_name);
        nameTV.setText(group.getGroupName());
        TextView descTV = rootView.findViewById(R.id.join_group__desc);
        descTV.setText(group.getGroupDescription());
        TextView membersTV = rootView.findViewById(R.id.join_group_members);
        membersTV.setText(getResources().getString(R.string.total) + ":" + group.getUserMap().size());

        final FirebaseDatabaseHelper firebaseDB = FirebaseDatabaseHelper.getInstance();

        SharedPreferences sharedPreferences = rootView.getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        final String userId = sharedPreferences.getString(SP_UID, null);
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        final CircularProgressButton joinBtn = (CircularProgressButton) rootView.findViewById(R.id.join_group_btn);
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinBtn.startAnimation();

                firebaseDB.getUserById(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FirebaseDatabaseHelper.GetUserCallback() {
                    @Override
                    public void OnDataReceived(User user) {

                        firebaseDB.addUserToGroup(user, group, new FirebaseDatabaseHelper.OnGroupDataChangedCallback() {
                            @Override
                            public void onDataReceived(Group group) {
                                joinBtn.revertAnimation();
                                callback.onJoinedGroup(group);
                            }
                        });
                    }
                });
            }
        });

        return rootView;
    }
}