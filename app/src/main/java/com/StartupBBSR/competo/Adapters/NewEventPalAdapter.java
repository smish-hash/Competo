package com.StartupBBSR.competo.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.StartupBBSR.competo.Models.EventPalModel;
import com.StartupBBSR.competo.databinding.EventPalUserItemBinding;
import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class NewEventPalAdapter extends RecyclerView.Adapter<NewEventPalAdapter.ViewHolder> {

    private EventPalUserItemBinding binding;
    private Context context;
    private List<EventPalModel> modelList;

    //    Listener Member Variable
    private NewEventPalAdapter.OnItemClickListener listener;

    //    Listener Interface
    public interface OnItemClickListener {
        void onButtonClick(EventPalModel model);
    }

    public void setOnItemClickListener(NewEventPalAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public NewEventPalAdapter(Context context, List<EventPalModel> modelList) {
        this.context = context;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public NewEventPalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = EventPalUserItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull NewEventPalAdapter.ViewHolder holder, int position) {
        final EventPalModel eventPalModel = modelList.get(position);

        holder.Name.setText(eventPalModel.getName());
        holder.About.setText(eventPalModel.getBio());
        Glide.with(context).load(eventPalModel.getPhoto()).into(holder.Image);

        holder.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        InterestChipAdapter adapter = new InterestChipAdapter(eventPalModel.getChips());
        holder.recyclerView.setAdapter(adapter);

    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView Name, About;
        private ImageView Image;
        private RecyclerView recyclerView;
        private Button btnSendMessageRequestEventPal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            Name = binding.tvEventPalUserName;
            About = binding.tvEventPalUserAbout;
            Image = binding.ivEventPalUserImage;
            recyclerView = binding.eventPalUserSkillRecyclerView;
            btnSendMessageRequestEventPal = binding.btnSendMessage;

            btnSendMessageRequestEventPal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            EventPalModel model = modelList.get(position);
                            listener.onButtonClick(model);
                        }
                    }
                }
            });
        }
    }
}
