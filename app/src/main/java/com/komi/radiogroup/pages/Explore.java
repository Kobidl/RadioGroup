package com.komi.radiogroup.pages;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.komi.radiogroup.GroupActivity;
import com.komi.radiogroup.GroupsAdapter;
import com.komi.radiogroup.R;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.structures.Group;

import java.util.ArrayList;
import java.util.List;


public class Explore extends Fragment {

    View rootView;
    Group group;
    List<Group> groupList;
    RecyclerView recyclerView;
    GroupsAdapter groupsAdapter;
    private FrameLayout loader;


    public Explore() {
        // Required empty public constructor
    }

    public static Explore newInstance(String param1, String param2) {
        Explore fragment = new Explore();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_explore, container, false);

        /* Init recycler elements */
        recyclerView = rootView.findViewById(R.id.explore_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        groupList = new ArrayList<>();
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

        loader = rootView.findViewById(R.id.loader);
        ImageView loaderIV = rootView.findViewById(R.id.loader_image_view);
        final AnimationDrawable loaderAnimation = (AnimationDrawable) loaderIV.getDrawable();
        loaderAnimation.start();

        recyclerView.setAdapter(groupsAdapter);

        // Setting the group listener
        setListenerWithSubstring(""); //Empty substring for init so we get all groups

        // TODO: set a listener that will search on user input and get rid of the button
        final EditText et_search = rootView.findViewById(R.id.et_search);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Updating the listener to search with the substring
                String substring = s.toString().trim();
                FirebaseDatabaseHelper.getInstance().removeExploreListener();
                loader.setVisibility(View.VISIBLE);
                setListenerWithSubstring(substring);
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FirebaseDatabaseHelper.getInstance().removeExploreListener();
    }

    public void setListenerWithSubstring(String substring) {
        FirebaseDatabaseHelper.getInstance().setExploreListener(FirebaseAuth.getInstance().getCurrentUser().getUid(),substring ,new FirebaseDatabaseHelper.OnExploreDataChangedCallback() {
            @Override
            public void onDataReceived(List<Group> groups) {
                groupList = groups;
                groupsAdapter.setGroups(groups);
                groupsAdapter.notifyDataSetChanged();
                loader.setVisibility(View.GONE);
            }
        });
    }

}