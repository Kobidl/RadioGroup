package com.komi.radiogroup.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.komi.radiogroup.R;
import com.komi.radiogroup.audio_recorder.AudioListener;
import com.komi.radiogroup.audio_recorder.AudioRecordButton;
import com.komi.radiogroup.audio_recorder.AudioRecording;
import com.komi.radiogroup.audio_recorder.RecordingItem;
import com.komi.radiogroup.userlater.MusicPlayerService;
import com.komi.structures.Group;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
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
    private ImageView statusImage;
    protected AnimationDrawable statusAnimation;
    private TextView startBtnHelperTV;
    private TextView recordHelperTV;
    Group group;

    public GroupRadioFragment(Group group) {
        this.group = group;
    }

//    public static Profile newInstance(String param1, String param2) {
//        Profile fragment = new Profile();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }


//    public static GroupRadioFragment newInstance(Group group) {
//        GroupRadioFragment fragment = new GroupRadioFragment();
//        Bundle args = new Bundle();
//        args.putParcelable("group", group);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        group = (Group) this.getArguments().getParcelable("group");
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_group_radio, container, false);

        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, READ_EXTERNAL_STORAGE},0);

        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, 0);

        mAudioRecordButton = (AudioRecordButton) rootView.findViewById(R.id.group_audio_record_button);

        startBtnHelperTV = rootView.findViewById(R.id.join_group_btn_helper);
        recordHelperTV  = rootView.findViewById(R.id.record_btn_helper);

        statusImage = rootView.findViewById(R.id.radio_status_img);

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

                }
                else {
                    stopMusic();
                }
            }
        });



        if(listening){
            mAudioRecordButton.setVisibility(View.VISIBLE);
            startStopListening.setText(R.string.stop_listening);
            Objects.requireNonNull(getActivity()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            startStopListening.setBackgroundColor(rootView.getContext().getColor(R.color.red));
            startBtnHelperTV.setText(R.string.click_here_leave_channel);
            recordHelperTV.setVisibility(View.VISIBLE);
            statusImage.setImageResource(R.drawable.online_animation);
            statusAnimation = (AnimationDrawable) statusImage.getDrawable();
            statusAnimation.start();
        }

        registerReceiver();




        return rootView;
    }

    private void sendMessage(RecordingItem recordingItem) {
        final JSONObject rootObject = new JSONObject();
        try {
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
        statusImage.setImageResource(R.drawable.online_animation);
        statusAnimation = (AnimationDrawable) statusImage.getDrawable();
        statusAnimation.start();
        mAudioRecordButton.setVisibility(View.VISIBLE);
        Objects.requireNonNull(getActivity()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = new Intent(rootView.getContext(), MusicPlayerService.class);
        intent.putExtra("action","start_listening");
        intent.putExtra("user_id",userId);
        intent.putExtra("group",group);
        rootView.getContext().startService(intent);
        startStopListening.setText(R.string.stop_listening);
        startStopListening.setBackgroundColor(rootView.getContext().getColor(R.color.red));
        startBtnHelperTV.setText(R.string.click_here_leave_channel);
        recordHelperTV.setVisibility(View.VISIBLE);
    }

    private void stopMusic(){
        try {
            statusImage.setImageResource(R.drawable.offline);
            mAudioRecordButton.setVisibility(View.GONE);
            startStopListening.setText(R.string.start_listening);
            Intent intent = new Intent(rootView.getContext(), MusicPlayerService.class);
            rootView.getContext().stopService(intent);
            Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            startStopListening.setBackgroundColor(rootView.getContext().getColor(R.color.colorPrimary));
            recordHelperTV.setVisibility(View.GONE);
            startBtnHelperTV.setText(R.string.click_here_go_live);
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
        Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroyView();
    }

}