package com.komi.radiogroup.pages;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.komi.radiogroup.AddGroupActivity;
import com.komi.radiogroup.GroupsAdapter;
import com.komi.radiogroup.R;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.structures.Group;

import java.util.List;

public class Groups extends Fragment {


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
        final View rootView = inflater.inflate(R.layout.fragment_groups, container, false);
        List<Group> groupList = FirebaseDatabaseHelper.getInstance().getGroups();

        RecyclerView recyclerView = rootView.findViewById(R.id.groups_recycler);
        recyclerView.setHasFixedSize(true);

        FloatingActionButton openNew = rootView.findViewById(R.id.group_add_btn);
        openNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(rootView.getContext(), AddGroupActivity.class);
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        GroupsAdapter groupsAdapter = new GroupsAdapter(groupList);
        groupsAdapter.setListener(new GroupsAdapter.GroupListener() {
            @Override
            public void onClick(int position, View view) {

            }
        });

        recyclerView.setAdapter(groupsAdapter);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}