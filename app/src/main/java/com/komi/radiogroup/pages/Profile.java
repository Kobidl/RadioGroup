package com.komi.radiogroup.pages;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.komi.radiogroup.GroupMessageAdapter;
import com.komi.radiogroup.MainContainer;
import com.komi.radiogroup.R;
import com.komi.radiogroup.firebase.FirebaseDatabaseHelper;
import com.komi.radiogroup.userlater.UserGroupsIsAdminAdapter;
import com.komi.structures.Group;
import com.komi.structures.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String SHARED_PREFS = "radioGroup_sp";
    private static final String SP_UID = "latest_uid";
    private static final String SP_FULLNAME = "latest_fullname";
    private static final String SP_BIO = "latest_bio";

    final int WRITE_PERMISSION_REQUEST = 1;
    final int CAMERA_REQUEST = 1;
    final int PICK_IMAGE = 2;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String userID;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    User user;
    List<Group> groupsByUser = new ArrayList<>();
    File file;
    private StorageReference mStorageRef;

    View rootView;
    ImageView iv_profile_pic;
    TextView tv_fullName, tv_bio;
    Button btn_editProfile;

    RecyclerView groupsRecyclerView;
    UserGroupsIsAdminAdapter adapter;

    private boolean canTakeImage = false;

    public Profile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Account.
     */
    // TODO: Rename and change types and number of parameters
    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Getting storage instance
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // TODO: to allow loading this page for any user we need to pass a uid parameter and set it here
        userID = firebaseAuth.getCurrentUser().getUid();

        iv_profile_pic = rootView.findViewById(R.id.iv_profile_pic);
        tv_fullName = rootView.findViewById(R.id.tv_full_name);
        tv_bio = rootView.findViewById(R.id.tv_bio);
        btn_editProfile = rootView.findViewById(R.id.btn_edit_profile);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String latest_UID = sharedPreferences.getString(SP_UID, null);

        // If the latest profile info we have saved is of the same user, then load it from shared preferences first
        if(latest_UID != null && latest_UID.matches(firebaseAuth.getCurrentUser().getUid())){
            String latest_fullname = sharedPreferences.getString(SP_FULLNAME, null);
            String latest_bio = sharedPreferences.getString(SP_BIO, null);

            if (latest_fullname != null)
                tv_fullName.setText(latest_fullname);
            if (latest_bio != null)
                tv_bio.setText(latest_bio);


        }

        // Initializing user's Groups recyclerview and setting Listeners for data
        groupsRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_groups_by_user);
        groupsRecyclerView.setHasFixedSize(true);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new UserGroupsIsAdminAdapter(groupsByUser);
        groupsRecyclerView.setAdapter(adapter);

        FirebaseDatabaseHelper.getInstance().setGroupsByAdminIDListener(userID, new FirebaseDatabaseHelper.OnGroupsDataChangedCallback() {
            @Override
            public void onDataReceived(List<Group> groups) {
                adapter.setGroups(groups);
                adapter.notifyDataSetChanged();
            }
        });

        FirebaseDatabaseHelper.getInstance().setUserByUidListener(userID, new FirebaseDatabaseHelper.OnUserDataChangedCallback() {
            @Override
            public void onDataReceived(User nUser) {
                user = nUser;
                tv_fullName.setText(user.getFullname());
                tv_bio.setText(user.getBio());

                // Getting profile pic from storage and setting it
                if (user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
                    Glide.with(getContext())
                            .load(user.getProfilePicturePath())
                            .into(iv_profile_pic);
                } else { // Set default profile pic
                    iv_profile_pic.setImageResource(R.drawable.default_profile_pic);
                }

                // Saving latest profile info to shared preferences
                SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(SP_UID, firebaseAuth.getCurrentUser().getUid());
                editor.putString(SP_FULLNAME, user.getFullname());
                editor.putString(SP_BIO, user.getBio());
                editor.apply();
            }
        });


        //Request image permissions
        if (Build.VERSION.SDK_INT >= 23) {
            int hasWritePermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            } else {
                canTakeImage = true;
            }
        } else {
            canTakeImage = true;
        }


        btn_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Starting edit profile dialog
                final Dialog dialog = new Dialog(getContext(), R.style.WideDialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.edit_profile_dialog);
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


                //Close edit profile dialog
                Button closeDialogBtn = (Button) dialog.findViewById(R.id.close_dialog_btn);
                closeDialogBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                //Edit profile pic
                LinearLayout editProfilePic = (LinearLayout) dialog.findViewById(R.id.edit_image);
                editProfilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        openPickDialog();
                    }
                });

                if(!canTakeImage) {
                    editProfilePic.setVisibility(View.INVISIBLE);
                }
                else {
                    editProfilePic.setVisibility(View.VISIBLE);
                }


                //Edit profile name
                LinearLayout editFullnameNBioBtn = (LinearLayout) dialog.findViewById(R.id.edit_fullname_bio);
                editFullnameNBioBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();

                        final Dialog mDialog = new Dialog(getContext(), R.style.WideDialog);
                        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        mDialog.setCancelable(false);
                        mDialog.setContentView(R.layout.edit_name_bio_dialog);

                        final EditText et_name = mDialog.findViewById(R.id.et_fullname);
                        final EditText et_bio = mDialog.findViewById(R.id.et_bio);

                        et_name.setText(user.getFullname());
                        et_bio.setText(user.getBio());

                        Button btn_close = mDialog.findViewById(R.id.close_dialog_btn);
                        btn_close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                            }
                        });

                        Button btn_save = mDialog.findViewById(R.id.save_dialog_btn);
                        btn_save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String newName = et_name.getText().toString();
                                String newBio = et_bio.getText().toString();
                                user.setFullname(newName);
                                user.setBio(newBio);

                                //Updating new user info in UI
                                tv_fullName.setText(newName);
                                tv_bio.setText(newBio);
                                //Updating new user info in database
                                FirebaseDatabaseHelper.getInstance().addUserToUsers(user);
                                //Updating new user info in shared prefs
                                SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(SP_UID, firebaseAuth.getCurrentUser().getUid());
                                editor.putString(SP_FULLNAME, user.getFullname());
                                editor.putString(SP_BIO, user.getBio());
                                editor.apply();
                                //Updating new user display name in Auth
                                updateDisplayName(user.getFullname());

                                mDialog.dismiss();
                            }
                        });

                        mDialog.show();
                    }
                });

                dialog.show();
            }
        });


        return rootView;
    }



    private void openPickDialog() {
        final Dialog dialog = new Dialog(getContext(), R.style.WideDialog);
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

        Uri imageUri = FileProvider.getUriForFile(getActivity(),"com.komi.radiogroup.provider",file); //
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
                canTakeImage = false;
            }else{
                canTakeImage = true;
            }
        }
    }

    /* Camera / Gallery result */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    .into(iv_profile_pic);
            //((TextView) findViewById(R.id.upload_image_text)).setVisibility(View.INVISIBLE);

            /* Uploading image to firebase storage */
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
                                    //Saving user to db
                                    FirebaseDatabaseHelper.getInstance().addUserToUsers(user);
                                    // TODO: decide if necessary
                                    //currentUser.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(uri).build());
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
        Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FirebaseDatabaseHelper.getInstance().removeGroupsByAdminIDListener();
        FirebaseDatabaseHelper.getInstance().removeUserByUIDListener();
    }
}

