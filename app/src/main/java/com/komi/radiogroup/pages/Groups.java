package com.komi.radiogroup.pages;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.komi.radiogroup.EditGroupActivity;
import com.komi.radiogroup.GroupActivity;
import com.komi.radiogroup.GroupsAdapter;
import com.komi.radiogroup.R;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.structures.Group;
import com.komi.structures.GroupMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Groups extends Fragment {

    List<Group> groupList;
    Group group;
    View rootView;
    RecyclerView recyclerView;
    GroupsAdapter groupsAdapter;


    public Groups() {
        // Required empty public constructor
    }


    public static Groups newInstance() {
        Groups fragment = new Groups();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        groupList = new ArrayList<>();
        recyclerView = rootView.findViewById(R.id.groups_recycler);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        groupsAdapter = new GroupsAdapter(rootView.getContext(),groupList);
        groupsAdapter.setListener(new GroupsAdapter.GroupListener() {
            @Override
            public void onClick(int position, View view) {
                Intent intent = new Intent(rootView.getContext(), GroupActivity.class);
                group = groupList.get(position);

                intent.putExtra("group",group);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(groupsAdapter);

        final FrameLayout loader = rootView.findViewById(R.id.loader);
        ImageView loaderIV = rootView.findViewById(R.id.loader_image_view);
        final AnimationDrawable loaderAnimation = (AnimationDrawable) loaderIV.getDrawable();
        loaderAnimation.start();


        FirebaseDatabaseHelper.getInstance().setSortedGroupsByUIDListener(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FirebaseDatabaseHelper.OnGroupsDataChangedCallback() {
            @Override
            public void onDataReceived(List<Group> groups) {
                groupList = groups;
                Collections.sort(groups, new Comparator<Group>() {
                    @Override
                    public int compare(Group o1, Group o2) {
                        return Long.compare(o1.getTimeStamp(), o2.getTimeStamp());
                    }
                });
                groupsAdapter.setGroups(groups);
                groupsAdapter.notifyDataSetChanged();
                loaderAnimation.stop();
                loader.setVisibility(View.GONE);

            }
        });

        // Init floating btn
        FloatingActionButton openNew = rootView.findViewById(R.id.group_add_btn);
        openNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(rootView.getContext(), EditGroupActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //Unregistering listener
        FirebaseDatabaseHelper.getInstance().removeSortedGroupsByUIDListener();
    }

}

