package com.komi.radiogroup;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.structures.User;

import java.io.File;
import java.util.UUID;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class EditProfileActivity extends AppCompatActivity {

    final int WRITE_PERMISSION_REQUEST = 1;
    final int CAMERA_REQUEST = 1;
    final int PICK_IMAGE = 2;

    boolean canSave;
    ImageView imageView;
    CircularProgressButton saveBtn;
    File file;

    EditText oldPassword, newPassword, newPasswordConfirm;
    TextInputLayout oldPLayout, newPLayout, newPCLayout;
    TextView uploadImageTv;
    TextView showPasswordTV;
    boolean showPassword = false;

    User user;
    private StorageReference mStorageRef;

    FirebaseUser currentUser;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity);

        user = (User) getIntent().getParcelableExtra("user");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        final EditText newProfileName = findViewById(R.id.new_profile_name);
        final EditText newProfileBio = findViewById(R.id.new_profile_bio);
        imageView = findViewById(R.id.new_profile_view);
        saveBtn = (CircularProgressButton) findViewById(R.id.save_group);

        //Getting storage instance
        mStorageRef = FirebaseStorage.getInstance().getReference();
        canSave = false;

        uploadImageTv = findViewById(R.id.upload_image_text);

        newProfileName.setText(user.getFullname());
        newProfileBio.setText(user.getBio());
        if(user.getProfilePicturePath()!=null) {
            Glide.with(this).load(user.getProfilePicturePath()).into(imageView);
            uploadImageTv.setVisibility(View.GONE);
        }
        checkCanSave();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPickDialog();
            }
        });

        oldPLayout = findViewById(R.id.old_p_layout);
        newPLayout = findViewById(R.id.new_p_layout);
        newPCLayout = findViewById(R.id.new_pc_layout);
        showPasswordTV = findViewById(R.id.show_pass_btn);
        showPasswordTV.setText( " + " + getResources().getString(R.string.change_password));
        showPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPassword = !showPassword;
                if(showPassword){
                    oldPLayout.setVisibility(View.VISIBLE);
                    newPLayout.setVisibility(View.VISIBLE);
                    newPCLayout.setVisibility(View.VISIBLE);
                    showPasswordTV.setText( " - " + getResources().getString(R.string.change_password));
                }
                else{
                    oldPLayout.setVisibility(View.INVISIBLE);
                    newPLayout.setVisibility(View.INVISIBLE);
                    newPCLayout.setVisibility(View.INVISIBLE);
                    showPasswordTV.setText( " + " + getResources().getString(R.string.change_password));
                }
            }
        });

        saveBtn = findViewById(R.id.save_group);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canSave && !showPassword) {
                    saveBtn.startAnimation();
                    FirebaseDatabaseHelper.getInstance().addUserToUsers(user);
                    updateDisplayName(user.getFullname());
                    finish();
                }
                else if (canSave && checkPasswordInput()){
                    // User info update will be called from method below
                    changeUserPasswordAndDetails(oldPassword.getText().toString(), newPassword.getText().toString());
                }
            }
        });

        newProfileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                user.setFullname(editable.toString().trim());
                checkCanSave();
            }
        });


        newProfileBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                user.setBio(editable.toString().trim());
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

        // Change password functionality code
        oldPassword = findViewById(R.id.old_password_et);
        newPassword = findViewById(R.id.new_password_et);
        newPasswordConfirm = findViewById(R.id.new_password_confirm_et);

    }



    private boolean checkPasswordInput() {
        if(oldPassword.getText().toString().isEmpty() || newPassword.getText().toString().isEmpty() || newPasswordConfirm.getText().toString().isEmpty()){
            Toast.makeText(this, "Please fill in all password fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!newPassword.getText().toString().equals(newPasswordConfirm.getText().toString())){
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void changeUserPasswordAndDetails(String oldPass, final String newPass) {
        final FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        final String email = cUser.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email,oldPass);

        cUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    cUser.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(EditProfileActivity.this, "Changing password failed", Toast.LENGTH_SHORT).show();
                            }else {
                                saveBtn.startAnimation();
                                Toast.makeText(EditProfileActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                FirebaseDatabaseHelper.getInstance().addUserToUsers(user);
                                updateDisplayName(user.getFullname());
                                finish();
                            }
                        }
                    });
                }else {
                    Toast.makeText(EditProfileActivity.this, "Old password is wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Determine if save button enabled
    private void checkCanSave() {
        if(!canSave && user.getFullname().length() >0){
            saveBtn.setEnabled(true);
            canSave = true;
        }else if(canSave &&  user.getBio().length() == 0){
            canSave = false;
            saveBtn.setEnabled(false);
        }
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

        Uri imageUri = FileProvider.getUriForFile(EditProfileActivity.this,"com.komi.radiogroup.provider",file);
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
            uploadImageTv.setVisibility(View.GONE);
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
                                    user.setProfilePicturePath(url);
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

    private void updateDisplayName(String name) {
        if (currentUser != null) {
            currentUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                }
            });
        }
        else
            return;
    }

}
