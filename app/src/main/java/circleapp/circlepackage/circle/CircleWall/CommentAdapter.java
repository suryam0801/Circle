package circleapp.circlepackage.circle.CircleWall;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.data.ObjectModels.Comment;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> CommentList;
    private User user;
    private Broadcast currentBroadcast;
    private GlobalVariables globalVariables = new GlobalVariables();

    public CommentAdapter(Context mContext, List<Comment> CommentList, Broadcast currentBroadcast) {
        this.mContext = mContext;
        this.CommentList = CommentList;
        this.user = globalVariables.getCurrentUser();
        this.currentBroadcast = currentBroadcast;
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View pview = View.inflate(mContext, R.layout.comment_display_card, null);
        return new CommentAdapter.ViewHolder(pview);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        final Comment comment = CommentList.get(position);
        final String name = comment.getCommentorName();
        final String cmnt = comment.getComment();
        final String picUrl = comment.getCommentorPicURL();

        final long createdTime = comment.getTimestamp();
        final long currentTime = System.currentTimeMillis();

        String timeString = HelperMethodsUI.getTimeElapsed(currentTime, createdTime);

        boolean broadcastTimestampExists = user.getNewTimeStampsComments() != null && user.getNewTimeStampsComments().containsKey(currentBroadcast.getId());
        if (broadcastTimestampExists) {
            if (user.getNewTimeStampsComments().get(currentBroadcast.getId()) < comment.getTimestamp())
                holder.backgroundContainer.setBackground(mContext.getResources().getDrawable(R.drawable.light_blue_sharp_background));
        }

        holder.userName.setText(name);
        holder.comment.setText(cmnt);
        holder.timeElapsed.setText(timeString);
        if (picUrl.length() > 10) { //checking if its uploaded image
            Glide.with((Activity) mContext)
                    .load(picUrl)
                    .into(holder.profPic);
        } else if (picUrl.equals("default")) {
            int profilePic = Integer.parseInt(String.valueOf(R.drawable.default_profile_pic));
            Glide.with(mContext)
                    .load(ContextCompat.getDrawable(mContext, profilePic))
                    .into(holder.profPic);
        } else { //checking if it is default avatar
            int profilePic = Integer.parseInt(picUrl);
            Glide.with((Activity) mContext)
                    .load(ContextCompat.getDrawable(mContext, profilePic))
                    .into(holder.profPic);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return CommentList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profPic;
        TextView userName;
        TextView comment;
        TextView timeElapsed;
        LinearLayout backgroundContainer;

        public ViewHolder(View view) {
            super(view);
            Bundle params1 = new Bundle();
            params1.putString("newCommentsViewed", "noOfComments");

            backgroundContainer = view.findViewById(R.id.comment_display_background_container);
            profPic = view.findViewById(R.id.comment_profilePicture);
            userName = view.findViewById(R.id.comment_object_ownerName);
            comment = view.findViewById(R.id.comment_object_comment);
            timeElapsed = view.findViewById(R.id.comments_object_postedTime);
        }
    }
}