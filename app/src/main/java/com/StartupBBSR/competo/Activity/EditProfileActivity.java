package com.StartupBBSR.competo.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.StartupBBSR.competo.Adapters.TagRecyclerAdapter;
import com.StartupBBSR.competo.Models.UserModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.ActivityEditProfileBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding activityEditProfileBinding;


    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore firebaseDB;
    private FirebaseStorage firebaseStorage;
    private FirebaseUser firebaseUser;

    private String userId;

    private int flag = 0;

    private Constant constant;
    private UserModel userModel;

    private final int REQUEST_PHOTO_CODE = 123;
    private Uri profileImageUri, downloadUri;
    private UploadTask uploadTask;

    private TagRecyclerAdapter skillAdapter;
    private List<String> skillDataSet, skills;
    private ChipGroup skillsChipGroup;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityEditProfileBinding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(activityEditProfileBinding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDB = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        userId = firebaseAuth.getUid();

        constant = new Constant();

//        Get data from Main Activity via get Intent
        userModel = (UserModel) getIntent().getSerializableExtra(constant.getUserModelObject());

        firebaseUser = firebaseAuth.getCurrentUser();


        activityEditProfileBinding.BioTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (activityEditProfileBinding.BioTV.hasFocus()) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_SCROLL:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });


        activityEditProfileBinding.btnEditProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });

        activityEditProfileBinding.btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyInput();
            }
        });

        activityEditProfileBinding.recyclerViewSkills.setHasFixedSize(true);

        activityEditProfileBinding.editProfileToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        skillDataSet = Arrays.asList(getResources().getStringArray(R.array.FilterChips));
        skillsChipGroup = activityEditProfileBinding.skillsChipGroup;

        skillAdapter = new TagRecyclerAdapter(skillDataSet);
        skillAdapter.setOnTagClickListener((itemView, position) -> {
            Chip chip = new Chip(this);
            TextView skill = itemView.findViewById(R.id.tvTag);
            chip.setText(skill.getText().toString());
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);
            chip.setClickable(false);
            skillsChipGroup.addView(chip);
            chip.setOnCloseIconClickListener(view -> {
                skillsChipGroup.removeView(view);
            });
            skillsChipGroup.setVisibility(View.VISIBLE);
        });
        activityEditProfileBinding.recyclerViewSkills.setAdapter(skillAdapter);

        activityEditProfileBinding.etSkills.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                activityEditProfileBinding.recyclerViewSkills.setVisibility(View.VISIBLE);
                String input = charSequence.toString().toLowerCase();

                List<String> newSkills = new ArrayList<>();
                for (String skill: skillDataSet) {
                    if (skill.toLowerCase().contains(input)) {
                        newSkills.add(skill);
                    }
                }

                skillAdapter = new TagRecyclerAdapter(newSkills);

                skillAdapter.setOnTagClickListener((itemView, position) -> {
                    Chip chip = new Chip(EditProfileActivity.this);
                    TextView skill = itemView.findViewById(R.id.tvTag);
                    chip.setText(skill.getText().toString());
                    chip.setCloseIconVisible(true);
                    chip.setCheckable(false);
                    chip.setClickable(false);

                    skillsChipGroup.addView(chip);

                    chip.setOnCloseIconClickListener(view -> {
                        skillsChipGroup.removeView(view);
                    });
                    skillsChipGroup.setVisibility(View.VISIBLE);
//                    activityEditProfileBinding.recyclerViewSkills.setVisibility(View.INVISIBLE);
                });
                activityEditProfileBinding.recyclerViewSkills.setAdapter(skillAdapter);
                skillAdapter.notifyDataSetChanged();

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        setInitialData();
    }

    private void setInitialData() {
        activityEditProfileBinding.etName.setText(userModel.getUserName());
        activityEditProfileBinding.BioTV.setText(userModel.getUserBio());
        activityEditProfileBinding.etPhone.setText(userModel.getUserPhone());

        if (userModel.getUserPhoto() != null)
            loadUsingGlide(userModel.getUserPhoto());

        if (userModel.getUserChips() != null) {
            List<String> userChips = userModel.getUserChips();
            for (String s: userChips) {
                Chip chip = new Chip(this);
                chip.setText(s);
                chip.setCloseIconVisible(true);
                chip.setCheckable(false);
                chip.setClickable(false);
                skillsChipGroup.addView(chip);
                chip.setOnCloseIconClickListener(view -> {
                    skillsChipGroup.removeView(view);
                });
                skillsChipGroup.setVisibility(View.VISIBLE);
            }
        }
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_PHOTO_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PHOTO_CODE && resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Error fetching Image", Toast.LENGTH_SHORT).show();
                return;
            }

            profileImageUri = data.getData();
            loadUsingGlide(profileImageUri.toString());

        }
    }

    private void verifyInput() {
        flag = 0;

        checkEmptyField(activityEditProfileBinding.etName);
        checkEmptyField(activityEditProfileBinding.BioTV);
        checkEmptyField(activityEditProfileBinding.etPhone);
        checkphonenumber(activityEditProfileBinding.etPhone);
        checkSkills();
//        checkEmptyField(activityEditProfileBinding.etLinkedIn);

        if (flag == 4) {
            updateUser();
        } else {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkSkills() {
        skills = new ArrayList<>();

        for (int i = 0; i < skillsChipGroup.getChildCount(); ++i) {
            Chip chip = (Chip) skillsChipGroup.getChildAt(i);
            skills.add(chip.getText().toString());
        }

        if (skills.size() != 0) {
            if (skills.size() > 6) {
                Toast.makeText(this, "Cannot select more than 6 skills", Toast.LENGTH_SHORT).show();
                flag--;
            } else if (skills.size() < 3) {
                Toast.makeText(this, "Select at least 3 skills", Toast.LENGTH_SHORT).show();
                flag--;
            } else
                flag++;
        } else {
            flag--;
        }
    }

    private void checkEmptyField(EditText et) {
        if (et.getText().toString().trim().isEmpty())
            flag--;
        else
            flag++;
    }
    private void checkphonenumber(EditText et)
    {
        if(Pattern.compile("^[1-9][0-9]{9}$").matcher(et.getText().toString()).find())
        {

        }
        else
        {
            Toast.makeText(this,"please enter correct Phone Number",Toast.LENGTH_SHORT).show();
            flag--;
        }
    }

    private void updateUser() {

        activityEditProfileBinding.btnSaveProfile.setVisibility(View.GONE);
        activityEditProfileBinding.uploadingProgressBar.setVisibility(View.VISIBLE);

        StorageReference storageReference = firebaseStorage
                .getReference()
                .child(constant.getProfilePictures() + "/" + userId);

        if (profileImageUri != null) {
//            Compressing the image
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profileImageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

//                Higher the number, higher the quality
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                byte[] data = baos.toByteArray();

                uploadTask = storageReference.putBytes(data);

            } catch (IOException e) {
                e.printStackTrace();
            }
//            uploadTask = storageReference.putFile(imguri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(EditProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
//                    activityEditProfileBinding.uploadingProgressBar.setVisibility(View.GONE);
//                    activityEditProfileBinding.btnSaveProfile.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, "Error:\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    activityEditProfileBinding.uploadingProgressBar.setVisibility(View.GONE);
                    activityEditProfileBinding.btnSaveProfile.setVisibility(View.VISIBLE);
                }
            });

            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful())
                        throw task.getException();

                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        downloadUri = task.getResult();
                        saveData();
                    }
                }
            });
        } else {
            saveData();
        }
    }

    private void saveData() {

        activityEditProfileBinding.btnSaveProfile.setVisibility(View.GONE);
        activityEditProfileBinding.uploadingProgressBar.setVisibility(View.VISIBLE);

        DocumentReference documentReference = firebaseDB.collection(constant.getUsers())
                .document(userId);

        Map<String, Object> userInfo = new HashMap<>();

        userInfo.put(constant.getUserNameField(), activityEditProfileBinding.etName.getText().toString().trim());
        userInfo.put(constant.getUserPhoneField(), activityEditProfileBinding.etPhone.getText().toString().trim());
        userInfo.put(constant.getUserBioField(), activityEditProfileBinding.BioTV.getText().toString().trim());
        userInfo.put(constant.getUserInterestedChipsField(), skills);


        if (downloadUri != null)
            userInfo.put(constant.getUserPhotoField(), downloadUri.toString());

        documentReference.update(userInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        activityEditProfileBinding.btnSaveProfile.setVisibility(View.VISIBLE);
                        activityEditProfileBinding.uploadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(EditProfileActivity.this, "Data updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }


    private void loadUsingGlide(String imgurl) {
        Glide.with(this).
                load(imgurl).
                listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        activityEditProfileBinding.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        activityEditProfileBinding.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(activityEditProfileBinding.profileImage);
    }
}