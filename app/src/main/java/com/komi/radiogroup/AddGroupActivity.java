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
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.structures.Group;
import com.komi.structures.User;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public class AddGroupActivity extends AppCompatActivity {

    final int WRITE_PERMISSION_REQUEST = 1;
    final int CAMERA_REQUEST = 1;
    final int PICK_IMAGE = 2;
    boolean canSave;
    ImageView imageView;
    Button saveBtn;
    File file;
    CheckBox cb_private;

    Group group;

    private StorageReference mStorageRef;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group_layout);
        UUID uuid = UUID.randomUUID();

        //Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.new_group);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);

        //Getting storage instance
        mStorageRef = FirebaseStorage.getInstance().getReference();

        canSave = false;

        //Creating new empty group
        group = new Group();
        group.setGroupID(uuid.toString());
        group.setGroupName("");
        group.setGroupDescription("");
        group.setPrivate(false);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        group.setAdminID(currentUser.getUid());

        User user = new User(currentUser.getUid(),currentUser.getDisplayName(),currentUser.getDisplayName());
        group.addUserToUserList(user);

        //Init elements
        imageView = findViewById(R.id.new_group_view);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               openPickDialog();
            }
        });

         saveBtn = findViewById(R.id.save_group);
         saveBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(canSave){
                     Intent data = new Intent();
                     FirebaseDatabaseHelper.getInstance().addGroupToGroups(group);
//                     data.putExtra("group", (Parcelable) group);
                     setResult(RESULT_OK, data);
                     finish();
                 };
             }
         });

         cb_private = findViewById(R.id.cb_private);
         cb_private.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 group.setPrivate(isChecked);
             }
         });

        final EditText newGroupName = findViewById(R.id.new_group_name);

        final EditText newGroupDescription = findViewById(R.id.new_group_description);

        newGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                group.setGroupName(editable.toString().trim());
                checkCanSave();
            }
        });


        newGroupDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                group.setGroupDescription(editable.toString().trim());
            }
        });


        //Request image permissions
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

    //Determine if save button enabled
    private void checkCanSave() {
        if(!canSave && group.getGroupName().length() >0){
            saveBtn.setEnabled(true);
            canSave = true;
        }else if(canSave &&  group.getGroupName().length() == 0){
            canSave = false;
            saveBtn.setEnabled(false);
        }
    }


    /* If back button pressed on toolbar */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPickDialog() {
        final Dialog dialog = new Dialog(this, R.style.WideDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.choose_image_dialog);
        saveBtn.setEnabled(false);

        dialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    saveBtn.setEnabled(true);
                }
                return true;
            }
        });

        Button closeDialogBtn = (Button) dialog.findViewById(R.id.close_dialog_btn);
        closeDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                saveBtn.setEnabled(true);
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

        Uri imageUri = FileProvider.getUriForFile(AddGroupActivity.this,"com.komi.radiogroup.provider",file);
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

    /* No Permissions to access images */
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

    /* Camera / Gallery result */
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

            /* Showing image */
            Glide.with(this)
                    .load(file.getAbsoluteFile())
                    .into(imageView);
            ((TextView) findViewById(R.id.upload_image_text)).setVisibility(View.INVISIBLE);

            /* Uploading image to firbase storage */
            UUID uuid = UUID.randomUUID();
            final StorageReference imageRef = mStorageRef.child("images/"+uuid.toString()+"_"+file.getName());
            imageRef.putFile(Uri.fromFile(file.getAbsoluteFile()))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
                                    group.setProfilePicturePath(url);
                                    saveBtn.setEnabled(true);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            Log.d("failed upload",exception.toString());
                        }
                    });
        }
    }

    /* Extracting path from uri */
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
