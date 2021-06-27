package com.StartupBBSR.competo.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.StartupBBSR.competo.Models.EventModel;
import com.StartupBBSR.competo.databinding.EventFragmentItemBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UpcomingEventAdapter extends FirestoreRecyclerAdapter<EventModel, UpcomingEventAdapter.ViewHolder> {

    private EventFragmentItemBinding binding;
    private Context context;

    private SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.US);
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.US);

    public EventFragmentAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot snapshot);
    }

    public void setOnItemClickListener(EventFragmentAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public UpcomingEventAdapter(@NonNull FirestoreRecyclerOptions<EventModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UpcomingEventAdapter.ViewHolder holder, int position, @NonNull EventModel model) {

    }

    @NonNull
    @Override
    public UpcomingEventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = EventFragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView day, month, title;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = binding.ivImage;
            day = binding.tvDateDay;
            month = binding.tvDateMonth;
            title = binding.tvTitle;


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
