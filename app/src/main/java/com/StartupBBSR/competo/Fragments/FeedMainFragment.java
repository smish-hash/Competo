package com.StartupBBSR.competo.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.StartupBBSR.competo.Activity.MainActivity;
import com.StartupBBSR.competo.Adapters.EventFeedAdapter;
import com.StartupBBSR.competo.Models.EventModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.FragmentFeedMainBinding;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class FeedMainFragment extends Fragment {
    private FragmentFeedMainBinding binding;
    private EventFeedAdapter adapter;

    private FirebaseFirestore firestoreDB;
    private FirebaseAuth firebaseAuth;
    private NavController navController;

    private Constant constant;
    private CollectionReference collectionReference;
    private FirestoreRecyclerOptions<EventModel> options;
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFeedMainBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getUid();
        constant = new Constant();
        collectionReference = firestoreDB.collection(constant.getEvents());


        initGreetings();
        initData();

        binding.ivFeedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).onProfileImageClick();
            }
        });

        binding.tvViewAllUpcomingEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
                FeedFragment feedFragment = (FeedFragment) navHostFragment.getParentFragment();
                feedFragment.onClickViewAllEvents();*/
                ((MainActivity) getActivity()).onViewAllEventsClick();
            }
        });


        binding.btnExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
                FeedFragment feedFragment = (FeedFragment) navHostFragment.getParentFragment();
                feedFragment.findTeamMate();*/
                ((MainActivity) getActivity()).onExploreClick();
            }
        });

        return view;
    }

    private void initGreetings() {
        DocumentReference documentReference = firestoreDB.collection(constant.getUsers()).document(userID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(getActivity()==null) {
                    return;
                }

                if (getActivity() == null) {
                    return;
                }

                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    String name = snapshot.getString(constant.getUserNameField());
                    String img = snapshot.getString(constant.getUserPhotoField());
                    if (img != null) {
                        Glide.with(getContext()).load(img).into(binding.ivFeedImage);
                    } else {
                        Glide.with(getContext()).load(R.drawable.user).into(binding.ivFeedImage);
                    }

                    // Creating random greetings
                    String[] greetings = {"Hello", "Hola", "Namaste", "Bonjour", "Kon'nichiwa", "Nǐ hǎo"};
                    Random r = new Random();
                    int randomNumber = r.nextInt(greetings.length);

                    if (name.contains(" ")) {
                        String[] names = name.split(" ");
                        binding.tvFeedHello.setText(greetings[randomNumber] + " " + names[0] + "!");
                    } else {
                        binding.tvFeedHello.setText(greetings[randomNumber] + " " + name + "!");
                    }
                }
            }
        });

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 12) {
            binding.tvFeedGreeting.setText("Good Morning");
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            binding.tvFeedGreeting.setText("Good Afternoon");
        } else if (timeOfDay >= 16 && timeOfDay < 24) {
            binding.tvFeedGreeting.setText("Good Evening");
        }
    }

    private void initData() {
        Query query = collectionReference.orderBy("eventDateStamp")
                .whereGreaterThanOrEqualTo("eventDateStamp", new Date().getTime())
                .limit(4);

        options = new FirestoreRecyclerOptions.Builder<EventModel>()
                .setQuery(query, EventModel.class)
                .build();

        initRecycler();
    }

    private void initRecycler() {

        SnapHelper snapHelper = new LinearSnapHelper();

        RecyclerView recyclerView = binding.unpcomingEventsRecyclerView;
        //recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        snapHelper.attachToRecyclerView(recyclerView);


/*        adapter = new EventFragmentAdapter(getContext(), options);
        adapter.setOnItemClickListener(new EventFragmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot snapshot) {
                EventModel model = snapshot.toObject(EventModel.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("eventDetails", model);
                bundle.putString("from", "feed");
                navController.navigate(R.id.action_feedMainFragment_to_eventDetailsFragment4, bundle);
            }
        });*/

        adapter = new EventFeedAdapter(getContext(), options);
        adapter.setOnItemClickListener(new EventFeedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot snapshot) {
                EventModel model = snapshot.toObject(EventModel.class);

                Bundle bundle = new Bundle();
                bundle.putSerializable("eventDetails", model);
                bundle.putString("from", "feed");
                navController.navigate(R.id.action_feedMainFragment_to_eventDetailsFragment4, bundle);
            }
        });

        binding.unpcomingEventsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(getActivity(), R.id.fragment_feed);
    }
}
