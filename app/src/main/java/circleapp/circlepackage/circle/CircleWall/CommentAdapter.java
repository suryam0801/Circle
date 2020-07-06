package circleapp.circlepackage.circle.CircleWall;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.rpc.Help;

import java.util.List;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> CommentList;
    private int count = 0;
    private User user;
    private Broadcast currentBroadcast;

    public CommentAdapter(Context mContext, List<Comment> CommentList, Broadcast currentBroadcast) {
        this.mContext = mContext;
        this.CommentList = CommentList;
        this.user = SessionStorage.getUser((Activity) mContext);
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

        final long createdTime = comment.getTimestamp();
        final long currentTime = System.currentTimeMillis();

        String timeString = HelperMethods.getTimeElapsed(currentTime, createdTime);

        boolean broadcastTimestampExists = user.getNewTimeStampsComments() != null && user.getNewTimeStampsComments().containsKey(currentBroadcast.getId());
        if (broadcastTimestampExists) {
            if (user.getNewTimeStampsComments().get(currentBroadcast.getId()) < comment.getTimestamp())
                holder.backgroundContainer.setBackground(mContext.getResources().getDrawable(R.drawable.light_blue_sharp_background));
        }

        holder.userName.setText(name);
        holder.comment.setText(cmnt);
        holder.timeElapsed.setText(timeString);
        HelperMethods.setUserProfileImage(SessionStorage.getUser((Activity) mContext), mContext.getApplicationContext(), holder.profPic);
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