package com.StartupBBSR.competo.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.StartupBBSR.competo.Models.MessageModel;
import com.StartupBBSR.competo.databinding.ReceiverTextItemBinding;
import com.StartupBBSR.competo.databinding.SenderTextItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NewChatAdapter extends RecyclerView.Adapter {

    private ReceiverTextItemBinding receiverTextItemBinding;
    private SenderTextItemBinding senderTextItemBinding;

    private Context context;
    private FirebaseUser fUser;

    private int SENDER_VIEW_TYPE = 1, RECEIVER_VIEW_TYPE = 2;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yy KK:mm a", Locale.US);

    private List<MessageModel> mMessage;

    public NewChatAdapter(Context context, List<MessageModel> mMessage) {
        this.context = context;
        this.mMessage = mMessage;
    }


    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        if (viewType == SENDER_VIEW_TYPE) {
            senderTextItemBinding = SenderTextItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new SenderViewHolder(senderTextItemBinding.getRoot());
        } else {
            receiverTextItemBinding = ReceiverTextItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ReceiverViewHolder(receiverTextItemBinding.getRoot());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        final MessageModel messageModel = mMessage.get(position);

        if (holder.getClass() == SenderViewHolder.class) {
            senderTextItemBinding.tvSenderText.setText(messageModel.getMessage());
            senderTextItemBinding.tvSenderTextTime.setText(simpleDateFormat
                    .format(new Date(Long.parseLong(messageModel.getTimestamp().toString()))));

            if (position == 0) {
                if (messageModel.getSeen()) {
                    senderTextItemBinding.tvTextSeen.setText("Delivered");
                } else {
                    senderTextItemBinding.tvTextSeen.setText("Sent");
                }
            } else {
                senderTextItemBinding.tvTextSeen.setVisibility(View.GONE);
            }

        } else {
            receiverTextItemBinding.tvReceiverText.setText(messageModel.getMessage());
            receiverTextItemBinding.tvReceiverTextTime.setText(simpleDateFormat
                    .format(new Date(Long.parseLong(messageModel.getTimestamp().toString()))));
        }
    }

    @Override
    public int getItemCount() {
        return mMessage.size();
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
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        private TextView receivedMessage, receivedTime;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedMessage = receiverTextItemBinding.tvReceiverText;
            receivedTime = receiverTextItemBinding.tvReceiverTextTime;
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        private TextView sentMessage, sentTime, tv_seen;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            sentMessage = senderTextItemBinding.tvSenderText;
            sentTime = senderTextItemBinding.tvSenderTextTime;
            tv_seen = senderTextItemBinding.tvTextSeen;
        }
    }
}
