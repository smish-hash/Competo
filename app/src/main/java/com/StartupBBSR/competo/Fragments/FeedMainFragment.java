package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

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
        randomanim ();
        initData();

        binding.ivFeedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
                if (navHostFragment != null) {
                    FeedFragment feedFragment = (FeedFragment) navHostFragment.getParentFragment();
                    if (feedFragment != null)
                        feedFragment.onProfileImageClick();
                }
            }
        });

        binding.cvPosterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
                if (navHostFragment != null) {
                    FeedFragment feedFragment = (FeedFragment) navHostFragment.getParentFragment();
                    if (feedFragment != null)
                        feedFragment.onclickproject ();
                }
            }
        });


        binding.tvViewAllUpcomingEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
                FeedFragment feedFragment = (FeedFragment) navHostFragment.getParentFragment();
                if (feedFragment != null) {
                    feedFragment.onClickViewAllEvents();
                }
            }
        });


        binding.btnprojectExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
                if (navHostFragment != null) {
                    FeedFragment feedFragment = (FeedFragment) navHostFragment.getParentFragment();
                    if (feedFragment != null)
                        feedFragment.onclickproject();
                }

//                This also works, but cannot unselect selected icon in bottom bar :)
                /*if (getActivity() != null) {
                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.eventPalFragmentMenu);
                }*/
            }

        });
        binding.btnfinderExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
                if (navHostFragment != null) {
                    FeedFragment feedFragment = (FeedFragment) navHostFragment.getParentFragment();
                    if (feedFragment != null)
                        feedFragment.findTeamMate();
                }

//
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
                        binding.tvFeedHello.setText(getString(R.string.greeting, greetings[randomNumber], names[0]));
                    } else {
                        binding.tvFeedHello.setText(getString(R.string.greeting, greetings[randomNumber], name));
                    }
                }
            }
        });



        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay < 12) {
            binding.tvFeedGreeting.setText(R.string.goodMorning);
        } else if (timeOfDay < 16) {
            binding.tvFeedGreeting.setText(R.string.goodAfternoon);
        } else {
            binding.tvFeedGreeting.setText(R.string.goodEvening);
        }
    }

    private void randomanim(){
        int[] Animation = {R.raw.home_animation_1,R.raw.home_animation_2,R.raw.home_animation_3};
        Random r = new Random();
        int randomNumber = r.nextInt(Animation.length);
        binding.cvPosterImage.setAnimation (Animation[randomNumber]);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        snapHelper.attachToRecyclerView(recyclerView);

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
        navController = Navigation.findNavController(view);
    }
}
