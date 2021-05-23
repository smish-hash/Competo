package com.StartupBBSR.competo.Fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.StartupBBSR.competo.Adapters.TagRecyclerAdapter;
import com.StartupBBSR.competo.Models.EventModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.FragmentAddEventBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;


public class AddEventFragment extends Fragment {

    private FragmentAddEventBinding binding;

    private static final int REQUEST_PHOTO_CODE = 123;

    private TagRecyclerAdapter adapter;
    private List<String> tagDataSet;
    private ChipGroup tagChipGroup;

    private Calendar calendar;

    private Uri eventImageUri, eventImageDownloadUri;
    private UploadTask uploadTask;

    private NavController navController;


    private Constant constant;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private FirebaseStorage firebaseStorage;
    private String organizerID;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddEventBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        organizerID = firebaseAuth.getUid();

        constant = new Constant();

        calendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                updateDateLabel();
            }
        };

        TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendar.set(Calendar.HOUR, hour);
                calendar.set(Calendar.MINUTE, minute);
                updateTimeLabel();
            }
        };

        initTagDataSet();

        tagChipGroup = binding.tagsChipGroup;

        binding.topAppBar.setNavigationOnClickListener(view1 -> {
            navController.navigate(R.id.action_addEventFragment_to_manageEventMainFragment);
        });


        binding.ivPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });

        binding.DateET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(), date,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                        .show();

                binding.DateET.clearFocus();
            }
        });

        binding.TimeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(getContext(), time, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), false)
                        .show();
            }
        });

        binding.recyclerViewTags.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTags.setHasFixedSize(true);
//        binding.recyclerViewTags.addItemDecoration(
//                new DividerItemDecoration(binding.recyclerViewTags.getContext(), LinearLayoutManager.VERTICAL));

        adapter = new TagRecyclerAdapter(tagDataSet);
        adapter.setOnTagClickListener(new TagRecyclerAdapter.OnTagClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Chip chip = new Chip(getContext());

                TextView text = itemView.findViewById(R.id.tvTag);
                chip.setText(text.getText().toString());
                chip.setCloseIconVisible(true);
                chip.setCheckable(false);
                chip.setClickable(false);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tagChipGroup.removeView(view);
                    }
                });
                tagChipGroup.addView(chip);
                tagChipGroup.setVisibility(View.VISIBLE);
            }
        });
        binding.recyclerViewTags.setAdapter(adapter);


        binding.TagET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.recyclerViewTags.setVisibility(View.VISIBLE);
                String input = charSequence.toString();

                List<String> newChips = new ArrayList<>();

                for (String tag : tagDataSet) {
                    if (tag.contains(input)) {
                        newChips.add(tag);
                    }
                }


                adapter = new TagRecyclerAdapter(newChips);
                adapter.setOnTagClickListener(new TagRecyclerAdapter.OnTagClickListener() {
                    @Override
                    public void onItemClick(View itemView, int position) {
                        Chip chip = new Chip(getContext());

                        TextView text = itemView.findViewById(R.id.tvTag);
                        chip.setText(text.getText().toString());
                        chip.setCloseIconVisible(true);
                        chip.setCheckable(false);
                        chip.setClickable(false);
                        chip.setOnCloseIconClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                tagChipGroup.removeView(view);
                            }
                        });
                        tagChipGroup.addView(chip);
                        tagChipGroup.setVisibility(View.VISIBLE);

                        binding.recyclerViewTags.setVisibility(View.INVISIBLE);
                    }
                });
                binding.recyclerViewTags.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

        });

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        binding.btnUploadEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateInfo();
            }
        });

    }

    private void updateInfo() {

        binding.btnUploadEvent.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        StorageReference storageReference = firebaseStorage.getReference()
                .child(constant.getEventPosters() + "/" + firestoreDB.collection(constant.getEvents()).get());

        if (eventImageUri != null){
//            Compressing The Image
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), eventImageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

//                Higher the number, higher the quality
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                byte[] data = baos.toByteArray();

                uploadTask = storageReference.putBytes(data);

            } catch (IOException e) {
                e.printStackTrace();
            }

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getContext(), "Photo Uploaded", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Error Uploading Image:\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.btnUploadEvent.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
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
                    if (task.isSuccessful()){
                        eventImageDownloadUri = task.getResult();
                        Log.d("event", "onComplete: " + eventImageDownloadUri);
                        uploadEvent();
                    }
                }
            });

        } else {
            uploadEvent();
        }
    }

    private void uploadEvent() {

        binding.btnUploadEvent.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        // TODO: 5/22/2021 assuming nothing is blank

        String image;
        String title = binding.TitleET.getText().toString();
        String description = binding.DescriptionET.getText().toString();
        String venue = binding.VenueET.getText().toString();
        String date = binding.DateET.getText().toString();
        String time = binding.TimeET.getText().toString();
        List<String> eventTags = new ArrayList<>();

        for (int i = 0; i < binding.tagsChipGroup.getChildCount(); ++i) {
            Chip chip = (Chip) binding.tagsChipGroup.getChildAt(i);
            eventTags.add(chip.getText().toString());
        }

        if (eventImageDownloadUri != null){
            image = eventImageDownloadUri.toString();
        } else {
            image = null;
        }
        Log.d("event", "uploadEvent: " + image);

        EventModel eventModel = new EventModel(image, title, description, venue, date, time, eventTags, organizerID);

        CollectionReference collectionReference = firestoreDB.collection(constant.getEvents());

        collectionReference.add(eventModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        Toast.makeText(getContext(), "Event Uploaded", Toast.LENGTH_SHORT).show();
                        binding.btnUploadEvent.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.GONE);
                        navController.navigate(R.id.action_addEventFragment_to_manageEventMainFragment);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Upload Failed:\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.btnUploadEvent.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_PHOTO_CODE);

    }

    private void initTagDataSet() {
        tagDataSet = Arrays.asList(getResources().getStringArray(R.array.FilterChips));
    }

    private void updateDateLabel() {
        String myDateFormat = "dd/MM/yy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(myDateFormat, Locale.US);

        binding.DateET.setText(simpleDateFormat.format(calendar.getTime()));
    }

    private void updateTimeLabel() {
        String myTimeFormat = "h:m a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(myTimeFormat, Locale.US);

        binding.TimeET.setText(simpleDateFormat.format(calendar.getTime()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PHOTO_CODE && resultCode == getActivity().RESULT_OK) {
            if (data == null) {
                Toast.makeText(getContext(), "Error Fetching Image", Toast.LENGTH_SHORT).show();
                return;
            }

            eventImageUri = data.getData();

            Glide.with(getContext()).load(eventImageUri).into(binding.ivPoster);

        }
    }
}