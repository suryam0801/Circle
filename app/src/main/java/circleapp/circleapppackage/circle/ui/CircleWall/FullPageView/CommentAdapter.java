package circleapp.circleapppackage.circle.ui.CircleWall.FullPageView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import circleapp.circleapppackage.circle.Helpers.HelperMethodsBL;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Comment;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ui.CircleWall.FullPageImageDisplay;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private int postPosition;
    private Context mContext;
    private List<Comment> CommentList;
    private User user;
    private Broadcast currentBroadcast;
    private GlobalVariables globalVariables = new GlobalVariables();
    private Dialog deleteCommentConfirmation;

    public CommentAdapter(List<Comment> CommentList, Broadcast currentBroadcast, int postPosition) {
        this.CommentList = CommentList;
        this.user = globalVariables.getCurrentUser();
        this.currentBroadcast = currentBroadcast;
        this.postPosition = postPosition;
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View pview = View.inflate(mContext, R.layout.comment_display_card, null);
        return new CommentAdapter.ViewHolder(pview);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        mContext = holder.itemView.getContext();
        final Comment comment = CommentList.get(position);
        setCommentValues(holder, comment, position);
        String body = comment.getComment();
        loadImages(comment, body, holder);

    }

    private void loadImages(Comment comment, String body, ViewHolder holder) {
        if(body.contains("https://firebasestorage.googleapis.com/v0/b/circle-d8cc7.appspot.com")){

            if(user.getUserId().equals(comment.getCommentorId())){
                holder.rightImageBackgroundContainer.setVisibility(View.VISIBLE);
                Glide.with((Activity) mContext)
                        .load(body)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.rightCommentImageUpload.setVisibility(View.GONE);
                                return false;
                            }
                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.rightCommentImageUpload.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .thumbnail(0.5f)
                        .into(holder.rightCommentImage);
            }
            else {
                holder.imageBackgroundContainer.setVisibility(View.VISIBLE);
                Glide.with((Activity) mContext)
                        .load(body)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.commentImageUpload.setVisibility(View.GONE);
                                return false;
                            }
                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.commentImageUpload.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .thumbnail(0.5f)
                        .centerInside()
                        .into(holder.commentImage);
            }
        }
        else {
            Glide.with(mContext).clear(holder.itemView);
            if(user.getUserId().equals(comment.getCommentorId())){
                if(body.length()<=3){
                    holder.rightCommentShortContainer.setVisibility(View.VISIBLE);
                }
                else
                    holder.rightBackgroundContainer.setVisibility(View.VISIBLE);
            }
            else {
                holder.backgroundContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setCommentValues(ViewHolder holder, Comment comment, int pos){

        final String name = comment.getCommentorName();
        final String cmnt = comment.getComment();
        final long createdTime = comment.getTimestamp();

        String pattern = "hh:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date date = new Date(createdTime);
        String timeString = simpleDateFormat.format(date);

        holder.userName.setText(name);
        holder.imageUserName.setText(name);
        holder.comment.setText(cmnt);
        holder.timeElapsed.setText(timeString);
        holder.imageTimeStamp.setText(timeString);
        holder.rightImageTimeStamp.setText(timeString);
        holder.rightShortComment.setText(cmnt);
        holder.rightTimeElapsedShort.setText(timeString);

        //For username color change
        int hash = arrayValForName(name);
        if(hash<10&&hash>=0){
            holder.userName.setTextColor(globalVariables.getColorsForUsername()[hash]);
            holder.imageUserName.setTextColor(globalVariables.getColorsForUsername()[hash]);
        }

        holder.rightComment.setText(cmnt);
        holder.rightTimeElapsed.setText(timeString);
        if(cmnt.length()<6){
            RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.END_OF, R.id.comment_object_ownerName);
            holder.timeElapsed.setLayoutParams(params);
        }

        if(name.length()>cmnt.length()){
            RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.END_OF, R.id.comment_object_ownerName);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.comment_object_comment);
            params.setMargins(3,0,9,0);
            holder.timeElapsed.setLayoutParams(params);
        }

        holder.commentImage.setOnClickListener(v->{
            goToFullPageImageView(cmnt);
        });

        holder.rightCommentImage.setOnClickListener(v->{
            goToFullPageImageView(cmnt);
        });

        holder.rightBackgroundContainer.setOnLongClickListener(v->{
            String [] options = {"Copy", "Delete"};
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Options");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which==1){
                        showDeleteCommentDialog(comment);
                    }
                    else
                    {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", "@Me"+": " +  cmnt);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, "Copied to Clipboard",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.show();
            return true;
        });

        holder.backgroundContainer.setOnLongClickListener(v->{
            String [] options = {"Copy", "Delete"};
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Options");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which==0)
                    {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", "@"+ name+": " +  cmnt);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, "Copied to Clipboard",Toast.LENGTH_SHORT).show();
                    }
                    else if(which==1){
                        showDeleteCommentDialog(comment);
                    }
                }
            });
            builder.show();
            return true;
        });

        holder.rightCommentImage.setOnLongClickListener(v->{
            showDeleteCommentDialog(comment);
            return true;
        });

        holder.rightShortComment.setOnLongClickListener(v->{
            String [] options = {"Copy", "Delete"};
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Options");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which==1){
                        showDeleteCommentDialog(comment);
                    }
                    else
                    {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", "@Me"+": " + cmnt);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, "Copied to Clipboard",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.show();
            return true;
        });
    }

    private void goToFullPageImageView(String url){
        Intent intent = new Intent(mContext, FullPageImageDisplay.class);
        intent.putExtra("uri", url);
        intent.putExtra("indexOfComment", postPosition);
        mContext.startActivity(intent);
        ((Activity) mContext).finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int arrayValForName(String name){
        int val = name.length();
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
        private TextView userName, imageUserName;
        private TextView comment, rightComment, rightShortComment;
        private TextView timeElapsed, rightTimeElapsed, rightTimeElapsedShort;
        private LinearLayout backgroundContainer, rightBackgroundContainer, imageBackgroundContainer, rightImageBackgroundContainer, rightCommentShortContainer;
        private PhotoView commentImage, rightCommentImage;
        private TextView imageTimeStamp, rightImageTimeStamp;
        private ProgressBar commentImageUpload, rightCommentImageUpload;

        public ViewHolder(View view) {
            super(view);
            Bundle params1 = new Bundle();
            params1.putString("newCommentsViewed", "noOfComments");

            imageBackgroundContainer = view.findViewById(R.id.image_comment_display_background_container);
            backgroundContainer = view.findViewById(R.id.comment_display_background_container);
            userName = view.findViewById(R.id.comment_object_ownerName);
            imageUserName = view.findViewById(R.id.image_comment_object_ownerName);
            comment = view.findViewById(R.id.comment_object_comment);
            timeElapsed = view.findViewById(R.id.comments_object_postedTime);
            commentImage = view.findViewById(R.id.comment_image);
            imageTimeStamp = view.findViewById(R.id.comment_image_postedTime);
            commentImageUpload = view.findViewById(R.id.comment_image_upload_progress);

            rightImageBackgroundContainer = view.findViewById(R.id.image_right_comment_display_background_container);
            rightBackgroundContainer = view.findViewById(R.id.right_comment_display_background_container);
            rightComment = view.findViewById(R.id.right_comment_object_comment);
            rightTimeElapsed = view.findViewById(R.id.right_comments_object_postedTime);
            rightCommentImage = view.findViewById(R.id.right_comment_image);
            rightImageTimeStamp = view.findViewById(R.id.right_comment_image_postedTime);
            rightCommentImageUpload = view.findViewById(R.id.right_comment_image_upload_progress);
            rightCommentShortContainer = view.findViewById(R.id.right_comment_short_background_container);
            rightShortComment = view.findViewById(R.id.right_comment_short_comment);
            rightTimeElapsedShort = view.findViewById(R.id.right_comments_short_postedTime);

        }
    }
}