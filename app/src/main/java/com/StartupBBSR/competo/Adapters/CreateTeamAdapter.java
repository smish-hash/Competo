package com.StartupBBSR.competo.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.StartupBBSR.competo.Models.EventPalModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.databinding.CreateTeamUserItemBinding;
import com.StartupBBSR.competo.databinding.FragmentCreateTeamBinding;
import com.StartupBBSR.competo.databinding.TeamAddMemberBottomsheetLayoutBinding;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class CreateTeamAdapter extends RecyclerView.Adapter<CreateTeamAdapter.ViewHolder> {

    private CreateTeamUserItemBinding binding;
    private FragmentCreateTeamBinding createTeamBinding = null;
    private TeamAddMemberBottomsheetLayoutBinding addMemberTeamBinding = null;
    private Context context;
    private List<EventPalModel> modelList;

    public CreateTeamAdapter(Context context, List<EventPalModel> modelList, FragmentCreateTeamBinding createTeamBinding) {
        this.context = context;
        this.modelList = modelList;
        this.createTeamBinding = createTeamBinding;
    }

    public CreateTeamAdapter(Context context, List<EventPalModel> modelList, TeamAddMemberBottomsheetLayoutBinding addMemberTeamBinding) {
        this.context = context;
        this.modelList = modelList;
        this.addMemberTeamBinding = addMemberTeamBinding;
    }

    @NonNull
    @Override
    public CreateTeamAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = CreateTeamUserItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull CreateTeamAdapter.ViewHolder holder, int position) {
        final EventPalModel eventPalModel = modelList.get(position);

        holder.name.setText(eventPalModel.getName());

        if (eventPalModel.getPhoto() != null)
            Glide.with(context).load(Uri.parse(eventPalModel.getPhoto())).into(holder.image);
        else
            Glide.with(context).load(R.drawable.ic_baseline_person_24).into(holder.image);


        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (createTeamBinding != null) {
                    int tally = getSelected().size();
                    createTeamBinding.tvTeamTally.setText(tally + "/6");
                }

            }
        });

        holder.checkBox.setChecked(eventPalModel.isSelected());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventPalModel.setSelected(!eventPalModel.isSelected());
                holder.checkBox.setChecked(eventPalModel.isSelected());
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name;
        CheckBox checkBox;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = binding.userImage;
            name = binding.userName;
            checkBox = binding.userCheckBox;
            cardView = binding.cardView;
        }
    }

    public ArrayList<EventPalModel> getSelected() {
        ArrayList<EventPalModel> selected = new ArrayList<>();
        for (int i = 0; i < modelList.size(); i++) {
            if (modelList.get(i).isSelected())
                selected.add(modelList.get(i));
        }
        return selected;
    }

    public void unSelectAll() {
        for (int i = 0; i < modelList.size(); i++) {
            if (modelList.get(i).isSelected())
                modelList.get(i).setSelected(false);
        }
    }
}
