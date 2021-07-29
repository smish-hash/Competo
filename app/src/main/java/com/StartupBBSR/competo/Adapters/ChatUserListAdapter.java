package com.StartupBBSR.competo.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.StartupBBSR.competo.Activity.ChatDetailActivity;
import com.StartupBBSR.competo.Models.EventPalModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.InboxItemBinding;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ChatUserListAdapter extends FirestoreRecyclerAdapter<EventPalModel, ChatUserListAdapter.ViewHolder> {

    private InboxItemBinding binding;
    private Context context;
    private Constant constants = new Constant();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yy KK:mm a", Locale.US);

    public ChatUserListAdapter(@NonNull FirestoreRecyclerOptions<EventPalModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull EventPalModel model) {
        holder.name.setText(model.getName());

        if (model.getPhoto() != null) {
            Glide.with(context).load(Uri.parse(model.getPhoto())).into(holder.image);
        } else {
            Glide.with(context).load(R.drawable.ic_baseline_person_24).into(holder.image);
        }

//        TODO: 6/7/2021 implement last message
//        Showing last message
        FirebaseFirestore.getInstance().collection(constants.getChats())
                .document(FirebaseAuth.getInstance().getUid())
                .collection(constants.getMessages())
                .document(model.getUserID())
                .collection(constants.getMessages())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }

                        for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                            holder.lastMessage.setText(queryDocumentSnapshot.getString("message"));
                            holder.lastMessageTime.setText(simpleDateFormat
                                    .format(new Date(Long.parseLong(queryDocumentSnapshot.get("timestamp").toString()))));
                        }
                    }
                });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("receiverID", model.getUserID());
                intent.putExtra("receiverName", model.getName());
                intent.putExtra("receiverPhoto", model.getPhoto());
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = InboxItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView name, lastMessage, lastMessageTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = binding.tvConveUsername;
            lastMessage = binding.tvConveLastMsg;
            image = binding.ivConveProfile;
            lastMessageTime = binding.tvUserLastMessageTime;
        }
    }
}
