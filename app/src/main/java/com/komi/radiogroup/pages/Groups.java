package com.komi.radiogroup.pages;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.komi.radiogroup.AddGroupActivity;
import com.komi.radiogroup.GroupActivity;
import com.komi.radiogroup.GroupsAdapter;
import com.komi.radiogroup.R;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.structures.Group;

import java.util.List;

public class Groups extends Fragment {

    List<Group> groupList;
    Group group;
    View rootView;
    RecyclerView recyclerView;
    GroupsAdapter groupsAdapter;

    DatabaseReference groupsReference;
    ValueEventListener listener;

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Get groups from firebase db
                groupList = FirebaseDatabaseHelper.getInstance().getGroupsByUID(FirebaseAuth.getInstance().getCurrentUser().getUid());

                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /* Init recycler elements */
                            recyclerView = rootView.findViewById(R.id.groups_recycler);
                            recyclerView.setHasFixedSize(true);

                            recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
                            groupsAdapter = new GroupsAdapter(groupList);
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
                            registerGroupsListener(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        }
                    });
                }

            }
        }).start();

        // Init floating btn
        FloatingActionButton openNew = rootView.findViewById(R.id.group_add_btn);
        openNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(rootView.getContext(), AddGroupActivity.class);

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
        groupsReference.removeEventListener(listener);
    }

    private void registerGroupsListener(final String userID) {
        groupsReference = FirebaseDatabase.getInstance().getReference().child(FirebaseDatabaseHelper.DB_GROUPS);
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Group temp = snapshot1.getValue(Group.class);
                    if(temp.getUserMap().containsKey(userID)){
                        groupList.add(temp);
                    }
                }
                groupsAdapter.setGroups(groupList);
                groupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        groupsReference.addValueEventListener(listener);
    }

}

