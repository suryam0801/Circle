package circleapp.circlepackage.circle.CircleWall;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashMap;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class PollAnswerDisplayAdapter extends RecyclerView.Adapter<PollAnswerDisplayAdapter.ViewHolder> {
    private Context mContext;
    private HashMap<Subscriber, String> list;
    private int count = 0;
    GlobalVariables globalVariables = new GlobalVariables();


    public PollAnswerDisplayAdapter(Context mContext, HashMap<Subscriber, String> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public PollAnswerDisplayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poll_answer_display_cell, parent, false);
        return new PollAnswerDisplayAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PollAnswerDisplayAdapter.ViewHolder holder, int position) {

        final Subscriber member = (Subscriber) list.keySet().toArray()[position];
        final String answer = (String) list.values().toArray()[position];
        if (member.getPhotoURI().length() > 10) {
            Glide.with(mContext)
                    .load(globalVariables.getAuthenticationToken().getCurrentUser().getPhotoUrl())
                    .into(holder.profPic);
        } else if (member.getPhotoURI().equals("default")) {
            int profilePic = Integer.parseInt(String.valueOf(R.drawable.default_profile_pic));
            Glide.with(mContext)
                    .load(ContextCompat.getDrawable(mContext, profilePic))
                    .into(holder.profPic);
        } else {
            int index = Integer.parseInt(String.valueOf(member.getPhotoURI().charAt(member.getPhotoURI().length()-1)));
            index = index-1;
            Log.d("index", index+"");
            TypedArray avatarResourcePos = mContext.getResources().obtainTypedArray(R.array.AvatarValues);
            int profilePic = avatarResourcePos.getResourceId(index, 0);
            Glide.with(mContext)
                    .load(profilePic)
                    .into(holder.profPic);
        }
        ++count;
        if(count == 4) count = 0;

        //Set text for TextView
        final String nameDisplay = member.getName();
        holder.name.setText(nameDisplay);
        holder.answer.setText(answer);

        holder.container.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.item_animation_fall_down));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, answer;
        private CircleImageView profPic;
        private LinearLayout container;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.poll_answer_view_cell_container);
            name = view.findViewById(R.id.poll_answer_view_member_name);
            profPic = view.findViewById(R.id.poll_answer_view_member_profile_picture);
            answer = view.findViewById(R.id.poll_answer_view_answer);
        }
    }
}