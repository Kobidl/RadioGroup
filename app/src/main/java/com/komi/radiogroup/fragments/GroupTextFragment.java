package com.komi.radiogroup.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.komi.radiogroup.adapters.GroupMessageAdapter;
import com.komi.radiogroup.R;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.structures.Group;
import com.komi.structures.GroupMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class GroupTextFragment extends Fragment {

    View rootView;

    RecyclerView feedRecyclerView;
    GroupMessageAdapter groupMessageAdapter;
    List<GroupMessage> groupMessages;

    DatabaseReference messagesReference;
    ValueEventListener messagesListener;

    Group group;

    public GroupTextFragment(Group group) {
        this.group = group;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflating layout
        rootView = inflater.inflate(R.layout.fragment_group_text, container, false);

        // Initializing elements
        final EditText et_message = rootView.findViewById(R.id.et_message);

        final ImageButton btn_send = rootView.findViewById(R.id.btn_send);

        et_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String message = editable.toString().trim();
                if(message.isEmpty()){
                    btn_send.setVisibility(View.GONE);
                }else{
                    btn_send.setVisibility(View.VISIBLE);
                }
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = et_message.getText().toString().trim();
                if(!message.isEmpty()) {
                    // Sending message to topic
                    //FirebaseMessagingHelper.getInstance(getContext()).sendMessageToTopic(group.getGroupID(), message);

                    // Sending message to database
                    GroupMessage groupMessage = new GroupMessage();
                    groupMessage.setType(GroupMessage.MESSAGE_TYPE_TEXT);
                    groupMessage.setGroup_ID(group.getGroupID());
                    groupMessage.setMsg_ID(UUID.randomUUID().toString());
                    groupMessage.setFrom_ID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    groupMessage.setFrom_name(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    groupMessage.setBody(message);
                    groupMessage.setTimeInMillis(System.currentTimeMillis());
                    FirebaseDatabaseHelper.getInstance().addGroupMessage(groupMessage);
                    et_message.setText("");
                }
            }
        });


        // Initializing recycler view
        groupMessages = new ArrayList<>();
        feedRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_chat);
        feedRecyclerView.setHasFixedSize(true);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        groupMessageAdapter = new GroupMessageAdapter(getContext(), groupMessages);
        feedRecyclerView.setAdapter(groupMessageAdapter);


        registerMessagesListener(group.getGroupID());

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Unregistering listener
        messagesReference.removeEventListener(messagesListener);

        // Unsubscribing from group messaging topic
        //FirebaseMessagingHelper.getInstance(getContext()).unsubscribeFromTopic(group.getGroupID());
    }

    private void registerMessagesListener(String groupID) {
        messagesReference = FirebaseDatabase.getInstance().getReference().child(FirebaseDatabaseHelper.DB_GROUP_MESSAGES).child(groupID);

        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupMessages.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    GroupMessage temp = snapshot1.getValue(GroupMessage.class);
                    if(temp.getType() == GroupMessage.MESSAGE_TYPE_TEXT){
                        groupMessages.add(temp);
                    }
                }
                //Sorting by time
                Collections.sort(groupMessages, new Comparator<GroupMessage>() {
                    @Override
                    public int compare(GroupMessage o1, GroupMessage o2) {
                        return Long.compare(o1.getTimeInMillis(), o2.getTimeInMillis());
                    }
                });
                groupMessageAdapter.setGroupMessages(groupMessages);
                groupMessageAdapter.notifyDataSetChanged();
                feedRecyclerView.scrollToPosition(groupMessageAdapter.getItemCount()-1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        messagesReference.addValueEventListener(messagesListener);
    }


}
