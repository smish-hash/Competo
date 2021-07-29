package com.StartupBBSR.competo.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.StartupBBSR.competo.Models.TeamMessageModel;
import com.StartupBBSR.competo.databinding.ReceiverTeamTextItemBinding;
import com.StartupBBSR.competo.databinding.SenderTeamTextItemBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TeamChatAdapter extends RecyclerView.Adapter {

    private ReceiverTeamTextItemBinding receiverTeamTextItemBinding;
    private SenderTeamTextItemBinding senderTeamTextItemBinding;
    private Context context;
    private FirebaseUser fUser;

    private int SENDER_VIEW_TYPE = 1, RECEIVER_VIEW_TYPE = 2;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yy KK:mm a", Locale.US);

    private List<TeamMessageModel> mMessage;

    public TeamChatAdapter(Context context, List<TeamMessageModel> mMessage) {
        this.context = context;
        this.mMessage = mMessage;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            senderTeamTextItemBinding = SenderTeamTextItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new SenderViewHolder(senderTeamTextItemBinding.getRoot());
        } else {
            receiverTeamTextItemBinding = ReceiverTeamTextItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ReceiverViewHolder(receiverTeamTextItemBinding.getRoot());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final TeamMessageModel teamMessageModel = mMessage.get(position);

        if (holder.getClass() == SenderViewHolder.class) {
            senderTeamTextItemBinding.tvSenderText.setText(teamMessageModel.getMessage());
            senderTeamTextItemBinding.tvSenderTextTime.setText(simpleDateFormat.format(new Date(Long.parseLong(teamMessageModel.getTimestamp().toString()))));
        } else {
            receiverTeamTextItemBinding.tvSenderName.setText(teamMessageModel.getSenderName());
            receiverTeamTextItemBinding.tvReceiverText.setText(teamMessageModel.getMessage());
            receiverTeamTextItemBinding.tvReceiverTextTime.setText(simpleDateFormat.format(new Date(Long.parseLong(teamMessageModel.getTimestamp().toString()))));
        }
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mMessage.get(position).getSenderID().equals(fUser.getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return mMessage.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        private TextView receivedMessage, senderName, receivedTime;


        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);

            receivedMessage = receiverTeamTextItemBinding.tvReceiverText;
            receivedTime = receiverTeamTextItemBinding.tvReceiverTextTime;
            senderName = receiverTeamTextItemBinding.tvSenderName;
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        private TextView sentMessage, sentTime;


        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            sentMessage = senderTeamTextItemBinding.tvSenderText;
            sentTime = senderTeamTextItemBinding.tvSenderTextTime;
        }
    }
}
