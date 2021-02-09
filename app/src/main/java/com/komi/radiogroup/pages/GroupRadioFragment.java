package com.komi.radiogroup.pages;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.komi.radiogroup.R;
import com.komi.radiogroup.userlater.MusicPlayerService;
import com.komi.structures.Group;

import static com.komi.radiogroup.GroupActivity.group;
import static com.komi.radiogroup.GroupActivity.listening;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupRadioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupRadioFragment extends Fragment {


    private static final String GROUP_PARAM = "group";

    private Button startStopListening;
    View rootView;

    public GroupRadioFragment() {
        // Required empty public constructor
    }

    public static GroupRadioFragment newInstance(Group group) {
        GroupRadioFragment fragment = new GroupRadioFragment();
        Bundle args = new Bundle();
        args.putSerializable(GROUP_PARAM, group);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_group_radio, container, false);
        TextView nameTV = rootView.findViewById(R.id.group_name_tv);
        TextView descTV = rootView.findViewById(R.id.group_desc_tv);
        TextView membersTV = rootView.findViewById(R.id.group_members_tv);
        membersTV.setText(getResources().getText(R.string.members) + ":" + group.getUserList().size());

        startStopListening = rootView.findViewById(R.id.btn_start_listening);
        startStopListening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listening = !listening;
                if(listening)
                    playMusic();
                else
                    stopMusic();
            }
        });

        nameTV.setText(group.getGroupName());
        descTV.setText(group.getGroupDescription());

        ImageView imageView = rootView.findViewById(R.id.group_image_view);
        Glide.with(this)
                .load(group.getProfilePicturePath())
                .into(imageView);

        return rootView;
    }

    private void playMusic(){
        Intent intent = new Intent(rootView.getContext(), MusicPlayerService.class);
        intent.putExtra("command","start_listening");
        intent.putExtra("group",group);
        rootView.getContext().startService(intent);
        startStopListening.setText(R.string.stop_listening);
    }

    private void stopMusic(){
        try {
            Intent intent = new Intent(rootView.getContext(), MusicPlayerService.class);
            rootView.getContext().stopService(intent);
            startStopListening.setText(R.string.start_listening);
        }catch (Exception e){

        }
    }
}