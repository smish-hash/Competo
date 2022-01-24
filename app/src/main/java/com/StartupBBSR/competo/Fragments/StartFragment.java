package com.StartupBBSR.competo.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import jp.wasabeef.glide.transformations.BlurTransformation;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.StartupBBSR.competo.Activity.AboutUsActivity;
import com.StartupBBSR.competo.Activity.EditProfileActivity;
import com.StartupBBSR.competo.Activity.FAQActivity;
import com.StartupBBSR.competo.Activity.LoginActivity;
import com.StartupBBSR.competo.Activity.ManageEventActivity;
import com.StartupBBSR.competo.Activity.SettingsActivity;
import com.StartupBBSR.competo.Models.UserModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.FragmentStartBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StartFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener{

    private static final int MY_REQUEST_CODE = 132;
    private FragmentStartBinding binding;
    private NavController navController;

    private Activity activity;

    Menu menu;

    private DrawerLayout drawerLayout;

    private NavigationView navigationView;
    private View header;
    private BottomNavigationView bottomNavigationView;

    private Animation tvFadeOut, tvFadeIn;

    private DocumentReference documentReference;
    private FirebaseFirestore firestoreDB;
    private FirebaseAuth firebaseAuth;
    private String userid;
    private DocumentSnapshot documentSnapshot;

    private Constant constant;
    private UserModel userModel;

    private AppUpdateManager appUpdateManager;

    private static final String TAG = "test";
    private static final String testTAG = "empty";

    private AlertDialog.Builder builder1;
    private AlertDialog.Builder builder2;


    private SharedPreferences sharedPreferences;
    private String theme;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            this.activity = (Activity) context;
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else
                    getActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentStartBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        builder1 = new AlertDialog.Builder(requireContext());
        builder2 = new AlertDialog.Builder(getContext());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        theme = sharedPreferences.getString("THEME", "1");

        if (theme.equals("1")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (theme.equals("2")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        FirebaseMessaging.getInstance().subscribeToTopic("Event")
                .addOnCompleteListener(task -> {
                    String msg = "Success";
                    Log.d("subscribe success", "token");
                    if (!task.isSuccessful()) {
                        msg = "Failed";
                        Log.d("subscribe failed", "token");
                    }
                });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("token failed", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    else
                    {
                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        Log.d("token success", token);
                        //sendfcm(token);

                        Map<String, Object> fcmtoken = new HashMap<>();
                        fcmtoken.put("token", token);

                        firestoreDB.collection("token").document(firebaseAuth.getUid())
                                .set(fcmtoken)
                                .addOnSuccessListener((OnSuccessListener<Void>) aVoid -> Log.d("token uploading", "DocumentSnapshot successfully written!"))
                                .addOnFailureListener((OnFailureListener) e -> Log.w("token uploading", "Error writing document", e));
                    }
                });


        setAnimations();
        setTitleText(1);

        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userid = firebaseAuth.getUid();
        constant = new Constant();
        userModel = new UserModel();

        documentReference = firestoreDB.collection(constant.getUsers()).document(userid);
        status("Online");

        /*profileFragment = new ProfileFragment();
        eventPalFragment = new EventPalFragment();

        feedFragment = new FeedFragment();
        eventFragment = new EventFragment();
        inboxNewFragment = new InboxNewFragment();

        projectFragment = new ProjectFragment();*/

        return view;
    }

    private void setAnimations() {
        tvFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.title_bar_text_animation_fade_up);
        tvFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.title_bar_text_animation_fade_in);
    }

    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        binding.getRoot(),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.ui_blue));
        snackbar.show();
    }

    /*public void sendfcm(String token)
    {
        Runnable runnable = () -> {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON,"{\n" +
                    "    \"notification\":{\n" +
                    "      \"title\":\"Portugal vs. Denmark\",\n" +
                    "      \"body\":\"great match!\"\n" +
                    "    },\n" +
                    "    \"data\" : {\n" +
                    "      \"category\" : \"chat\",\n" +
                    "    },\n" +
                    "    \"to\":\"/topics/test\"\n" +
                    "}");
            Request request = new Request.Builder()
                    .url("https://fcm.googleapis.com/fcm/send")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "key=AAAABmOW__8:APA91bFEiWxr4rRQa3M_5n-w-5XDjLnQ9nf2IgAs1r0ppfwgTLZoGgOJmRAF1pt59hHqdMZ74AmAx1lkk0HaCuLwUCsHi_M_BWEZAGwkXyp-57YJk_pGmGWwJKNEU_bnJLl7bv7VDPzy")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                Log.d("response",response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }*/

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        drawerLayout = binding.drawer;
        navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);

        float radius = getResources().getDimension(R.dimen.radius_10);
        MaterialShapeDrawable materialShapeDrawable = (MaterialShapeDrawable) navigationView.getBackground();
        materialShapeDrawable.setShapeAppearanceModel(materialShapeDrawable.getShapeAppearanceModel()
                .toBuilder().setTopRightCorner(CornerFamily.ROUNDED, radius)
                .setBottomRightCorner(CornerFamily.ROUNDED, radius)
                .build());

//        Bottom Navigation bar
        bottomNavigationView = binding.bottomNavBar;

        NavHostFragment nestedNavHostFragment =(NavHostFragment) getChildFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (nestedNavHostFragment != null) {
            navController = nestedNavHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        } else {
            Toast.makeText(getContext(), "Error getting controller", Toast.LENGTH_SHORT).show();
        }

        bottomNavigationView.setOnItemReselectedListener(item -> {
            if (item.getItemId() == R.id.btnTeamFinder) {
                loadFragment(3);
            }
        });

        /*navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.projectFragmentMenu) {
                Toast.makeText(getContext(), "Project", Toast.LENGTH_SHORT).show();
            }
        });*/

        binding.btnTeamFinder.setOnClickListener(view1 -> {
            loadFragment(3);
        });

        binding.drawerToggleIcon.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                if (!drawerLayout.isDrawerOpen(Gravity.START))
                    drawerLayout.openDrawer(Gravity.START);
            }
        });

        binding.btnInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_startFragment_to_inboxNewFragment3);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
//        Get Data when this activity starts
        getUserData();
        Log.d(testTAG, "onStart: ");
    }

    private void getUserData() {

//      get realtime data and store it in a class
        Log.d(testTAG, "getUserData: ");
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (isAdded()){
                    if (error != null) {
                        Log.d(TAG, "onEvent: " + error.toString());
                        return;
                    }

                    if (value != null && value.exists()) {
                        documentSnapshot = value;
                        saveDataToClass();
                        Log.d(TAG, "onEvent: " + value.getData());
                    }
                }
            }
        });

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot alertSnapshot = task.getResult();
                    int temp = 0;
                    if (alertSnapshot.getString(constant.getUserPhotoField()) == null || alertSnapshot.getString(constant.getUserBioField()) == null || alertSnapshot.get(constant.getUserInterestedChipsField()) == null) {
                        Log.d(testTAG, "onCompleteAlert: Photo, bio or tags null: " + alertSnapshot.getString(constant.getUserPhotoField()) + ", " + alertSnapshot.getString(constant.getUserBioField()));

                        builder1.setTitle("Tell us a bit about yourself");
                        builder1.setMessage("Complete your profile to continue");
                        builder1.setIcon(R.drawable.ic_baseline_settings_24);
                        builder1.setCancelable(false);
                        builder1.setPositiveButton("Complete profile", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                Intent intent = new Intent(getContext(), EditProfileActivity.class).putExtra(constant.getUserModelObject(), userModel);
                                startActivity(intent);
                            }
                        }).show();

//                        temp = 1;
                    }

/*                    if (temp != 1 && (List<String>) alertSnapshot.get(constant.getUserInterestedChipsField()) == null) {
                        Log.d(testTAG, "onCompleteAlert: Chips null: " + (List<String>) alertSnapshot.get(constant.getUserInterestedChipsField()));
                        builder2.setTitle("Add your skills");
                        builder2.setMessage("Add the skills most relevant to you\nTap on the 'Add skills' button in the profile page");
                        builder2.setIcon(R.drawable.ic_baseline_settings_24);
                        builder2.setCancelable(false);
                        builder2.setPositiveButton("Add skills", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadFragment(5);
                                dialogInterface.dismiss();
                            }
                        }).show();
                    }*/
                }
            }
        });
    }


    private void saveDataToClass() {
        getActivity().getIntent().putExtra(constant.getUserModelObject(), userModel);

        userModel.setUserName(documentSnapshot.getString(constant.getUserNameField()));
        userModel.setUserEmail(documentSnapshot.getString(constant.getUserEmailField()));
        userModel.setUserPhoto(documentSnapshot.getString(constant.getUserPhotoField()));
        userModel.setUserBio(documentSnapshot.getString(constant.getUserBioField()));
        userModel.setUserLinkedin(documentSnapshot.getString(constant.getUserLinkedinField()));
        userModel.setUserPhone(documentSnapshot.getString(constant.getUserPhoneField()));
        userModel.setUserRole(documentSnapshot.getString(constant.getUserisUserField()));
        userModel.setOrganizerRole(documentSnapshot.getString(constant.getUserisOrganizerField()));
        userModel.setUserChips((List<String>) documentSnapshot.get(constant.getUserInterestedChipsField()));
        userModel.setUserID(documentSnapshot.getString(constant.getUserIdField()));

        Log.d(TAG, "saveDataToClass: " + userModel.getUserChips());

        checkRole();
        updateHeader();
    }

    protected void setTitleText(int position) {
        switch (position) {
            case 1:
                binding.tvTitleBar.startAnimation(tvFadeOut);
                binding.tvTitleBar.setText("Feed");
                binding.tvTitleBar.startAnimation(tvFadeIn);
                break;
            case 2:
                binding.tvTitleBar.startAnimation(tvFadeOut);
                binding.tvTitleBar.setText("Events");
                binding.tvTitleBar.startAnimation(tvFadeIn);
                break;
            case 3:
                binding.tvTitleBar.startAnimation(tvFadeOut);
                binding.tvTitleBar.setText("Team Finder");
                binding.tvTitleBar.startAnimation(tvFadeIn);
                break;
            case 4:
                binding.tvTitleBar.startAnimation(tvFadeOut);
                binding.tvTitleBar.setText("Projects");
                binding.tvTitleBar.startAnimation(tvFadeIn);
                break;
            case 5:
                binding.tvTitleBar.startAnimation(tvFadeOut);
                binding.tvTitleBar.setText("Profile");
                binding.tvTitleBar.startAnimation(tvFadeIn);
                break;
            default:
                Toast.makeText(getContext(), "Wrong fragment", Toast.LENGTH_SHORT).show();

        }
    }

    protected void loadFragment(int position) {
        //switching fragment
        /*if (fragment != null) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .commit();
        }*/
        switch (position) {
            case 1:
                setTitleText(1);
                bottomNavigationView.setSelectedItemId(R.id.feedFragmentMenu);
                navController.navigate(R.id.feedFragmentMenu);
                break;
            case 2:
                setTitleText(2);
                bottomNavigationView.setSelectedItemId(R.id.eventFragmentMenu);
                navController.navigate(R.id.eventFragmentMenu);
                break;
            case 3:
                setTitleText(3);
                binding.btnTeamFinder.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.button_team_finder_animation));
                bottomNavigationView.setSelectedItemId(R.id.dummy);
                navController.navigate(R.id.eventPalFragmentMenu);
                break;
            case 4:
                setTitleText(4);
                bottomNavigationView.setSelectedItemId(R.id.projectFragmentMenu);
                navController.navigate(R.id.projectFragmentMenu);
                break;
            case 5:
                setTitleText(5);
                bottomNavigationView.setSelectedItemId(R.id.profileFragmentMenu);
                navController.navigate(R.id.profileFragmentMenu);
                break;
            default:
                Toast.makeText(getContext(), "Wrong fragment", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_logOut) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("weather");
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Log Out");
            builder.setMessage("Are you sure you want to log out?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    logout();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
        }
        else if (id == R.id.settings)
            startActivity(new Intent(getContext(), SettingsActivity.class));

        else if (id == R.id.menu_addEvent)
            startActivity(new Intent(getContext(), ManageEventActivity.class));

        else if (id == R.id.menu_faq)
            startActivity(new Intent(getContext(), FAQActivity.class));

        else if (id == R.id.menu_about_us)
            startActivity(new Intent(getContext(), AboutUsActivity.class));

        return true;
    }

    private void logout() {
        onPause();
        FirebaseMessaging.getInstance().unsubscribeFromTopic("Event");
        FirebaseMessaging.getInstance().deleteToken();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();
    }

    private void checkRole() {
        menu = navigationView.getMenu();
        if (userModel.getOrganizerRole().equals("1"))
            menu.findItem(R.id.menu_addEvent).setVisible(true);
        else if (userModel.getUserRole().equals("1"))
            menu.findItem(R.id.menu_addEvent).setVisible(false);
    }

    private void updateHeader() {
        TextView tvname = header.findViewById(R.id.tvHeaderName);
        TextView tvRole = header.findViewById(R.id.tvHeaderRole);
        ImageView ivprofile = header.findViewById(R.id.header_image);
        ImageView ivprofilebackground = header.findViewById(R.id.headerBackgroundImage);

        tvname.setText(userModel.getUserName());

        if (userModel.getUserRole().equals("1"))
            tvRole.setText("User");
        else
            tvRole.setText("Organizer");

        String imguri = userModel.getUserPhoto();
        if (imguri != null) {
//            Clear image
            loadUsingGlide(imguri, ivprofile, 1, 1);
//            Blurred Background
            loadUsingGlide(imguri, ivprofilebackground, 25, 5);
        }
    }


    private void loadUsingGlide(String imgurl, ImageView imageView, int radius, int sampling) {
        Glide.with(getContext()).
                load(imgurl).
                apply(RequestOptions.bitmapTransform(new BlurTransformation(radius, sampling)))
                .into(imageView);
    }

    @Override
    public void onPause() {
        super.onPause();
        status("Offline");
        Log.d("status", "onPause: Offline");
    }

    @Override
    public void onResume() {
        super.onResume();
        status("Online");
        Log.d("status", "onResume: Online");

    }

    private void status(String status) {
        documentReference.update("status", status);
    }

}