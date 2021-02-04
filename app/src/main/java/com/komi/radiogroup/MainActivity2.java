package com.komi.radiogroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import static com.komi.radiogroup.MusicPlayerService.PLAYER_BROADCAST;

public class MainActivity2 extends AppCompatActivity {

    private static final int NEW_SONG_ACTIVITY = 1;
    final String SONGS_OBJECT = "songs";

    public static ArrayList<Song> songs;
    public static int currentPlaying = -1;
    Button playBtn;
    SongAdapter songAdapter;

    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        RecyclerView recyclerView = findViewById(R.id.reclyer);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initSongs();

        playBtn = (Button) findViewById(R.id.main_start_btn);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentPlaying == -1){
                    playMusic();
                }else{
                    currentPlaying = -1;
                    stopMusic();
                    songAdapter.notifyDataSetChanged();
                }
            }
        });

        songAdapter = new SongAdapter(songs);
        songAdapter.setListener(new SongAdapter.MySongListener() {
            @Override
            public void onPlayClicked(int position, View view) {
                if(currentPlaying == position){
                    currentPlaying = -1;
                    stopMusic();
                }else {
                    currentPlaying = position;
                    playMusic();
                }
                songAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSongClicked(int position, View view) {
                Intent intent = new Intent(MainActivity2.this,FullPageActivity.class);
//                if(currentPlaying != position) {
//                    currentPlaying = position;
//                    playMusic();
//                }
                intent.putExtra("song_idx", position);
                startActivity(intent);
//                songAdapter.notifyDataSetChanged();
            }
        });

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN  ,ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();
                Collections.swap(songs,fromPos,toPos);
                if(currentPlaying == -1){
                    stopMusic();
                }else if(currentPlaying == fromPos){
                    currentPlaying = toPos;
                }else if(currentPlaying == toPos){
                    currentPlaying = fromPos;
                }
                updateSongsList();
                songAdapter.notifyItemMoved(fromPos,toPos);
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction == ItemTouchHelper.END ||direction == ItemTouchHelper.START) {
                    final int position = viewHolder.getAdapterPosition();
                    final Dialog dialog = new Dialog(MainActivity2.this, R.style.WideDialog);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.remove_song_dialog);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    TextView textView = dialog.findViewById(R.id.delete_song_desc);
                    textView.setText(getResources().getString(R.string.are_you_sure_remove) + " \"" + songs.get(position).getName()+"\"");
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            songAdapter.notifyDataSetChanged();
                        }
                    });

                    dialog.setOnKeyListener(new Dialog.OnKeyListener() {

                        @Override
                        public boolean onKey(DialogInterface arg0, int keyCode,
                                             KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                dialog.dismiss();
                            }
                            return true;
                        }
                    });

                    Button closeDialogBtn = (Button) dialog.findViewById(R.id.cancel_delete_btn);
                    closeDialogBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    Button deleteSongBtn = (Button) dialog.findViewById(R.id.delete_song_btn);
                    deleteSongBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            removeSong(position);
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(songAdapter);


//        playBtn = findViewById(R.id.main_play_btn);
//        playBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(isPlaying){
//                    playBtn.setText("Play");
//                    stopMusic();
//                }else{
//                    playBtn.setText("Stop");
//                    playMusic();
//                }
//                isPlaying = !isPlaying;
//            }
//        });


        ImageButton addNewSong = findViewById(R.id.add_new_song_btn);
        addNewSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this,NewSongActivity.class);
                startActivityForResult(new Intent(intent),NEW_SONG_ACTIVITY);
            }
        });

        currentPlaying = getIntent().getIntExtra("current_playing",currentPlaying);

        initReceiver();
        if(currentPlaying != -1){
            notifyOpen();
        }
    }

    private void notifyOpen() {
        Intent intent = new Intent(
                this,MusicPlayerService.class);
        intent.putExtra("command","app_created");
        startService(intent);
    }

    private void removeSong(int position) {
        songs.remove(position);
        if (position == currentPlaying || currentPlaying == -1) {
            stopMusic();
        } else if (position < currentPlaying) {
            currentPlaying--;
        }
        updateSongsList();
    }

    private void updateSongsList() {
        if(currentPlaying > -1){
            Intent intent = new Intent(
                    this,MusicPlayerService.class);
            intent.putExtra("command","update_list");
            intent.putExtra("list",songs);
            intent.putExtra("playing",currentPlaying);
            startService(intent);
        }
        saveSongs();
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter(PLAYER_BROADCAST);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getStringExtra("command");
                switch (command){
                    case "start":
                        int song = intent.getIntExtra("song_idx",-1);
                        if(currentPlaying != song) {
                            currentPlaying = song;
                            songAdapter.notifyDataSetChanged();
                        }
                        playBtn.setText(R.string.stop);
                        break;
                    case "stop":
                    case "pause":
                        currentPlaying = -1;
                        songAdapter.notifyDataSetChanged();
                        playBtn.setText(R.string.play);
                        break;

                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);
    }

    private void initSongs() {
        try {
            FileInputStream fis = this.openFileInput(SONGS_OBJECT);
            ObjectInputStream ois = new ObjectInputStream(fis);
            songs = (ArrayList<Song>)ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(songs == null) {

            songs = new ArrayList<>();
            JSONArray m_jArry;
            try {
                m_jArry = new JSONArray(loadJSONFromAsset());
                for (int i = 0; i < m_jArry.length(); i++) {
                    JSONObject jo_inside = m_jArry.getJSONObject(i);
                    String url = jo_inside.getString("url");
                    String image = jo_inside.getString("image");
                    String name = jo_inside.getString("name");
                    if(image!=null&&!image.isEmpty()){
                        int id = this.getResources().getIdentifier(image, "drawable", this.getPackageName());
                        Uri path = Uri.parse("android.resource://com.kobidl.kdplayer/" + id);
                        image = path.toString();
                    }else{
                        image = "";
                    }
                    String fileName = name.isEmpty() ? url.substring(url.lastIndexOf('/') + 1, url.length()) : name;
                    songs.add(new Song(url, fileName,image));
                }
                saveSongs();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveSongs() {
        try {
            FileOutputStream fos = this.openFileOutput(SONGS_OBJECT, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(songs);
            oos.close();
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("songs.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void playMusic(){
        Intent intent = new Intent(
                this,MusicPlayerService.class);
        intent.putExtra("list",songs);
        intent.putExtra("command","play_song");
        intent.putExtra("song_idx",currentPlaying);
        startService(intent);
        playBtn.setText(R.string.stop);
    }

    private void stopMusic(){
        try {
            Intent intent = new Intent(this, MusicPlayerService.class);
            stopService(intent);
            playBtn.setText(R.string.play);
        }catch (Exception e){

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_SONG_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                Song song = data.getParcelableExtra("song");
                songs.add(song);
                songAdapter.notifyDataSetChanged();
                saveSongs();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}