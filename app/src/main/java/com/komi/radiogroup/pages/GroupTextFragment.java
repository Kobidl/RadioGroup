package com.komi.radiogroup.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.komi.radiogroup.R;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.radiogroup.firebase.FirebaseMessagingHelper;
import com.komi.structures.Group;
import com.komi.structures.GroupMessage;

import java.util.UUID;

public class GroupTextFragment extends Fragment {

    Group group;
    View rootView;

    public GroupTextFragment() {

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

        // Subscribing to group messaging topic
        //FirebaseMessagingHelper.getInstance(getContext()).subscribeToTopic(group.getGroupID());

        // Initializing elements
        final EditText et_message = rootView.findViewById(R.id.et_message);

        Button btn_send = rootView.findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = et_message.getText().toString();

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
        });






        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Unsubscribing from group messaging topic
        //FirebaseMessagingHelper.getInstance(getContext()).unsubscribeFromTopic(group.getGroupID());
    }

    public void setGroup(Group group) {
        this.group = group;
    }



}
