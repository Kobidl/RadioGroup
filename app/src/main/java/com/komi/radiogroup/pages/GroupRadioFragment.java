package com.komi.radiogroup.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.komi.radiogroup.R;
import com.komi.radiogroup.audio_recorder.AudioListener;
import com.komi.radiogroup.audio_recorder.AudioRecordButton;
import com.komi.radiogroup.audio_recorder.AudioRecording;
import com.komi.radiogroup.audio_recorder.RecordingItem;
import com.komi.radiogroup.firebase.MyFirebaseMessagingService;
import com.komi.radiogroup.userlater.MusicPlayerService;
import com.komi.structures.Group;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.komi.radiogroup.GroupActivity.group;
import static com.komi.radiogroup.GroupActivity.listening;
import static com.komi.radiogroup.userlater.MusicPlayerService.PLAYER_BROADCAST;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupRadioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupRadioFragment extends Fragment {


    private static final String GROUP_PARAM = "group";
    private final String API_TOKEN_KEY = "AAAAMJ5RH1k:APA91bG5hD4dwWDrFFdK6QUYLmm_sLW1VvfHzwh-wwZGRar93y8ZTcyUAVU_O3pGEeKWqWe4FGgUe0Rs1VD5Vym6mQ9LnHUXhv6K5K1vlMwhCLkrpMIW0P0_6gD7ZLH5DA4u8jhNmkjz";

    private AudioRecordButton mAudioRecordButton;
    private AudioRecording audioRecording;
    private Button startStopListening;
    View rootView;
    FirebaseAuth firebaseAuth;
    private String userId;
    private BroadcastReceiver broadcastReceiver;

    public GroupRadioFragment() {
        // Required empty public constructor
    }

    public static GroupRadioFragment newInstance() {
        GroupRadioFragment fragment = new GroupRadioFragment();
        Bundle args = new Bundle();

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

        nameTV.setText(group.getGroupName());
        descTV.setText(group.getGroupDescription());

        ImageView imageView = rootView.findViewById(R.id.group_image_view);
        Glide.with(this)
                .load(group.getProfilePicturePath())
                .into(imageView);

        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, READ_EXTERNAL_STORAGE},0);

        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, 0);

        audioRecording = new AudioRecording(rootView.getContext());
        mAudioRecordButton = (AudioRecordButton) rootView.findViewById(R.id.group_audio_record_button);

        userId = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        mAudioRecordButton.setOnAudioListener(new AudioListener() {
            @Override
            public void onStop(RecordingItem recordingItem) {
                sendMessage(recordingItem);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(Exception e) {
                Log.d("MainActivity", "Error: " + e.getMessage());
            }
        });

        startStopListening = rootView.findViewById(R.id.btn_start_listening);
        startStopListening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listening = !listening;
                if(listening) {
                    playMusic();
                    mAudioRecordButton.setVisibility(View.VISIBLE);
                }
                else {
                    mAudioRecordButton.setVisibility(View.GONE);
                    stopMusic();
                }
            }
        });

        if(listening){
            mAudioRecordButton.setVisibility(View.VISIBLE);
            startStopListening.setText(R.string.stop_listening);
        }

        registerReceiver();

        return rootView;
    }

    private void sendMessage(RecordingItem recordingItem) {
        final JSONObject rootObject = new JSONObject();
        try {
            Log.i("testrecording","Groupid : " + group.getGroupID());
            rootObject.put("to", "/topics/" + group.getGroupID());
            rootObject.put("data", new JSONObject().put("message", recordingItem.getFileUrl()).put("sender_id", userId));
            String url = "https://fcm.googleapis.com/fcm/send";
            RequestQueue queue = Volley.newRequestQueue(rootView.getContext());
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("testrecording","error when sending recording "+error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + API_TOKEN_KEY);
                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return rootObject.toString().getBytes();
                }
            };
            queue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void playMusic(){
        stopMusic();
        String a = "start_listening";
        Intent intent = new Intent(rootView.getContext(), MusicPlayerService.class);
        intent.putExtra("group",group);
        intent.putExtra("action",a);
        intent.putExtra("user_id",userId);
        rootView.getContext().startService(intent);
        startStopListening.setText(R.string.stop_listening);
    }

    private void stopMusic(){
        try {
            startStopListening.setText(R.string.start_listening);

            Intent intent = new Intent(rootView.getContext(), MusicPlayerService.class);
            rootView.getContext().stopService(intent);
        }catch (Exception e){

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(PLAYER_BROADCAST);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getStringExtra("action");
                switch (command){
                    case "closed":
                        stopMusic();
                        break;
                    case "start_playing":
                        mAudioRecordButton.setEnabled(false);
                        break;
                    case "stop_playing":
                        mAudioRecordButton.setEnabled(true);
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(rootView.getContext()).registerReceiver(broadcastReceiver,filter);
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(rootView.getContext()).unregisterReceiver(broadcastReceiver);
        super.onDestroyView();
    }
}