package circleapp.circlepackage.circle.CircleWall;

import android.content.Context;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.PersonelDisplay.MemberListAdapter;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class PollAnswerDisplayAdapter extends RecyclerView.Adapter<PollAnswerDisplayAdapter.ViewHolder> {
    private Context mContext;
    private HashMap<Subscriber, String> list;
    String TAG = "APPLICANT_LIST_ADAPTER";
    private int count = 0;
    int[] myImageList = new int[]{R.drawable.person_blonde_head, R.drawable.person_job, R.drawable.person_singing,
            R.drawable.person_teacher, R.drawable.person_woman_dancing};


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

        Glide.with(mContext)
                .load(member.getPhotoURI())
                .placeholder(ContextCompat.getDrawable(mContext, myImageList[count]))
                .into(holder.profPic);

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
        TextView name, answer;
        CircleImageView profPic;
        LinearLayout container;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.poll_answer_view_cell_container);
            name = view.findViewById(R.id.poll_answer_view_member_name);
            profPic = view.findViewById(R.id.poll_answer_view_member_profile_picture);
            answer = view.findViewById(R.id.poll_answer_view_answer);
        }
    }
}