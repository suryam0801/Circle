package circleapp.circlepackage.circle.ui.CircleWall.FullPageView;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lzyzsd.randomcolor.RandomColor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import circleapp.circlepackage.circle.DataLayer.CommentsRepository;
import circleapp.circlepackage.circle.Helpers.HelperMethodsBL;
import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Comment;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> CommentList;
    private User user;
    private Broadcast currentBroadcast;
    private GlobalVariables globalVariables = new GlobalVariables();
    private Dialog deleteCommentConfirmation;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        final Comment comment = CommentList.get(position);
        final String name = comment.getCommentorName();
        final String cmnt = comment.getComment();
        final String picUrl = comment.getCommentorPicURL();

        final long createdTime = comment.getTimestamp();
        final long currentTime = System.currentTimeMillis();

        String pattern = "hh:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date date = new Date(createdTime);
        String timeString = simpleDateFormat.format(date);

        holder.userName.setText(name);
        holder.comment.setText(cmnt);
        holder.timeElapsed.setText(timeString);
        //For username color change
        int[] color = globalVariables.getColorsForUsername();
        int hash = arrayValForName(name);
        if(hash<10&&hash>=0)
            holder.userName.setTextColor(color[hash]);

        holder.rightComment.setText(cmnt);
        holder.rightTimeElapsed.setText(timeString);

        /*holder.rightBackgroundContainer.setOnLongClickListener(v->{
            showDeleteCommentDialog(comment);
            return true;
        });*/

        if(user.getUserId().equals(comment.getCommentorId())){
            holder.backgroundContainer.setVisibility(View.GONE);
            holder.rightBackgroundContainer.setVisibility(View.VISIBLE);
        }
        else {
            holder.backgroundContainer.setVisibility(View.VISIBLE);
            holder.rightBackgroundContainer.setVisibility(View.GONE);
        }
    }

    private int arrayValForName(String name){
        int val = name.charAt(0);
        return val%10;
    }
    public void showDeleteCommentDialog(Comment comment){
        deleteCommentConfirmation= new Dialog(mContext);
        deleteCommentConfirmation.setContentView(R.layout.delete_comment_popup);
        final Button closeDialogButton = deleteCommentConfirmation.findViewById(R.id.delete_comment_confirm_btn);
        final Button cancel = deleteCommentConfirmation.findViewById(R.id.delete_comment_cancel_btn);

        closeDialogButton.setOnClickListener(view -> {
            HelperMethodsBL.deleteComment(globalVariables.getCurrentCircle().getId(),currentBroadcast.getId(), comment);
            deleteCommentConfirmation.dismiss();
            Toast.makeText(mContext, "Comment Deleted!", Toast.LENGTH_SHORT).show();
        });

        cancel.setOnClickListener(view -> deleteCommentConfirmation.dismiss());

        deleteCommentConfirmation.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteCommentConfirmation.show();
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
        TextView userName;
        TextView comment, rightComment;
        TextView timeElapsed, rightTimeElapsed;
        LinearLayout backgroundContainer, rightBackgroundContainer;

        public ViewHolder(View view) {
            super(view);
            Bundle params1 = new Bundle();
            params1.putString("newCommentsViewed", "noOfComments");

            backgroundContainer = view.findViewById(R.id.comment_display_background_container);
            userName = view.findViewById(R.id.comment_object_ownerName);
            comment = view.findViewById(R.id.comment_object_comment);
            timeElapsed = view.findViewById(R.id.comments_object_postedTime);

            rightBackgroundContainer = view.findViewById(R.id.right_comment_display_background_container);
            rightComment = view.findViewById(R.id.right_comment_object_comment);
            rightTimeElapsed = view.findViewById(R.id.right_comments_object_postedTime);

        }
    }
}
/*        For showing a blue border on newly arrived comments
        boolean broadcastTimestampExists = user.getNewTimeStampsComments() != null && user.getNewTimeStampsComments().containsKey(currentBroadcast.getId());
        if (broadcastTimestampExists) {
            if (user.getNewTimeStampsComments().get(currentBroadcast.getId()) < comment.getTimestamp())
                holder.backgroundContainer.setBackground(mContext.getResources().getDrawable(R.drawable.light_blue_sharp_background));
        }*/