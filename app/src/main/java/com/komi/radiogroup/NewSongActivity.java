package com.komi.radiogroup;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

public class NewSongActivity extends Activity {

    final int WRITE_PERMISSION_REQUEST = 1;
    final int CAMERA_REQUEST = 1;
    final int PICK_IMAGE = 2;
    boolean canSave;
    ImageView imageView;
    Button saveBtn;
    EditText newSongUrl;
    File file;

    Song song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_song_layout);
        canSave = false;
        song = new Song();
        song.setName("");
        song.setUrl("");
        imageView = findViewById(R.id.new_song_image_view);
        
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               openPickDialog();
            }
        });

         saveBtn = findViewById(R.id.save_new_song);

        ImageButton backBtn = (ImageButton) findViewById(R.id.new_song_back);
         backBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 finish();
             }
         });

         saveBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(canSave){
                     Intent data = new Intent();
                     data.putExtra("song", (Parcelable) song);
                     setResult(RESULT_OK, data);
                     finish();
                 }
             }
         });

        newSongUrl = findViewById(R.id.new_song_url);
        final EditText newSongName = findViewById(R.id.new_song_name);
        newSongUrl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!view.isFocused()){
                    String url = newSongUrl.getText().toString();
                    song.setUrl(url);
                    String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
                    newSongName.setText(fileName);
                }
            }
        });

        newSongUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                song.setUrl(editable.toString().trim());
                checkCanSave();
            }
        });


        newSongName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                song.setName(editable.toString().trim());
                checkCanSave();
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            } else {
                imageView.setVisibility(View.VISIBLE);
            }
        } else {
            imageView.setVisibility(View.VISIBLE);
        }
    }

    private void checkCanSave() {
        if(!canSave && song.getName().length() >0 && song.getUrl().length() >0){
            saveBtn.setEnabled(true);
            canSave = true;
        }else if(canSave &&  song.getName().length() == 0 && song.getUrl().length() == 0){
            canSave = false;
            saveBtn.setEnabled(false);
        }
    }

    private void openPickDialog() {
        final Dialog dialog = new Dialog(this, R.style.WideDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.choose_image_dialog);

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

        Button closeDialogBtn = (Button) dialog.findViewById(R.id.close_dialog_btn);
        closeDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        LinearLayout takeImageBtn = (LinearLayout) dialog.findViewById(R.id.take_image);
        takeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                takeImage();
            }
        });

        LinearLayout chooseImageBtn = (LinearLayout) dialog.findViewById(R.id.choose_image);
        chooseImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                chooseImage();
            }
        });

        dialog.show();
    }

    private void takeImage(){
        String fileName = java.util.UUID.randomUUID().toString() + ".jpg";
        file = new File(Environment.getExternalStorageDirectory(), fileName);

        Uri imageUri = FileProvider.getUriForFile(NewSongActivity.this,"com.kobidl.kdplayer.provider",file);
        //Uri fileUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    private void chooseImage(){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if(requestCode == WRITE_PERMISSION_REQUEST){
            if(grantResults[0]!= PackageManager.PERMISSION_GRANTED){
                imageView.setVisibility(View.INVISIBLE);
            }else{
                imageView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
//                imageView.setImageDrawable(Drawable.createFromPath(file.getAbsolutePath()));
            }
            if (requestCode == PICK_IMAGE) {
                Uri imageUri = data.getData();
                file = new File(getRealPathFromURI(imageUri));
//                imageView.setImageDrawable(Drawable.createFromPath(file.getAbsolutePath()));
            }
            Glide.with(this)
                    .load(file.getAbsoluteFile())
                    .into(imageView);
            ((TextView) findViewById(R.id.upload_image_text)).setVisibility(View.INVISIBLE);
            song.setImage(file.getAbsolutePath());
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
