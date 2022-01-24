package com.StartupBBSR.competo.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.StartupBBSR.competo.Models.EventModel;
import com.StartupBBSR.competo.databinding.EventFragmentItemBinding;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EventFragmentAdapter extends RecyclerView.Adapter<EventFragmentAdapter.ViewHolder> {

    private EventFragmentItemBinding binding;
    private Context context;
    private List<EventModel> mList;


    private SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.US);
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.US);

    public OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(EventModel model);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public EventFragmentAdapter(Context context, List<EventModel> mList) {
        this.context = context;
        this.mList = mList;
    }

    @NonNull
    @Override
    public EventFragmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = EventFragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final EventModel model = mList.get(position);
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

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, day, month,description;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = binding.tvTitle;
            description = binding.tvDescription;
            day = binding.tvDateDay;
            month = binding.tvDateMonth;
            image = binding.ivImage;


            image.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        EventModel model = mList.get(position);
                        listener.onItemClick(model);
                    }
                }
            });
        }

    }
}
