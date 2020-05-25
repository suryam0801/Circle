package circleapp.circlepackage.circle.PersonelDisplay;

import android.content.Context;
import android.text.format.DateFormat;
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
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.ViewHolder> {
    private Context mContext;
    private List<Subscriber> memberList;
    String TAG = "APPLICANT_LIST_ADAPTER";

    public MemberListAdapter(Context mContext, List<Subscriber> memberList) {
        Log.d(TAG, "SIZE: " + memberList.size());
        this.mContext = mContext;
        this.memberList = memberList;
    }

    @NonNull
    @Override
    public MemberListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_cell_item, parent, false);
        return new MemberListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Subscriber member = memberList.get(position);

        Glide.with(mContext)
                .load(member.getPhotoURI())
                .placeholder(ContextCompat.getDrawable(mContext, R.drawable.profile_image))
                .into(holder.profPic);

        //Set text for TextView
        final String nameDisplay = member.getName();
        holder.name.setText(nameDisplay);

        long createdTime =member.getTimestamp();
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        String dateString = formatter.format(new Date(createdTime));
        holder.timeElapsed.setText("Member since " + dateString);

        holder.container.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.item_animation_fall_down));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, timeElapsed;
        CircleImageView profPic;
        LinearLayout container;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.member_cell_container);
            name = view.findViewById(R.id.member_name);
            timeElapsed = view.findViewById(R.id.member_since_display);
            profPic = view.findViewById(R.id.member_profile_picture);
        }
    }
}