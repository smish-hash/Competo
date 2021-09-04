package com.StartupBBSR.competo.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.StartupBBSR.competo.Models.EventModel;
import com.StartupBBSR.competo.databinding.EventFeedItemBinding;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EventFeedAdapter extends FirestoreRecyclerAdapter<EventModel, EventFeedAdapter.ViewHolder> {
    private EventFeedItemBinding binding;

    private Context context;


    private SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.US);
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.US);

    public OnItemClickListener listener;

    @Override
    protected void onBindViewHolder(@NonNull EventFeedAdapter.ViewHolder holder, int position, @NonNull EventModel model) {
        String day = "", month = "";

        if (model.getEventDateStamp() != null) {
            day = dayFormat.format(new Date(Long.parseLong(model.getEventDateStamp().toString())));
            month = monthFormat.format(new Date(Long.parseLong(model.getEventDateStamp().toString())));
        }

        holder.day.setText(day);
        holder.month.setText(month);
        holder.title.setText(model.getEventTitle());
        holder.description.setText(model.getEventDescription());
        Glide.with(context).load(model.getEventPoster()).into(holder.image);
    }

    @NonNull
    @Override
    public EventFeedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = EventFeedItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot snapshot);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public EventFeedAdapter(Context context, @NonNull FirestoreRecyclerOptions<EventModel> options) {
        super(options);
        this.context = context;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, day, month, description;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = binding.tvTitle;
            description = binding.tvDescription;
            day = binding.tvDateDay;
            month = binding.tvDateMonth;
            image = binding.ivImage;


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
                            listener.onItemClick(snapshot);
                        }
                    }
                }
            });
        }
    }
}
