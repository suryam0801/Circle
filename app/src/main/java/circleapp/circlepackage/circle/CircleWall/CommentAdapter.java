package circleapp.circlepackage.circle.CircleWall;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.rpc.Help;

import java.util.List;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends BaseAdapter {

    private Context mContext;
    private List<Comment> CommentList;
    private int count = 0;
    int[] myImageList = new int[]{R.drawable.avatar1, R.drawable.avatar3, R.drawable.avatar4,
            R.drawable.avatar2, R.drawable.avatar5};


    public CommentAdapter(Context mContext, List<Comment> CommentList) {
        this.mContext = mContext;
        this.CommentList = CommentList;
    }

    @Override
    public int getCount() {
        return CommentList.size();
    }

    @Override
    public Object getItem(int position) {
        return CommentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final View pview = View.inflate(mContext, R.layout.comment_display_card, null);

        Bundle params1 = new Bundle();
        params1.putString("newCommentsViewed", "noOfComments");

        CircleImageView profPic = pview.findViewById(R.id.comment_profilePicture);
        TextView userName = pview.findViewById(R.id.comment_object_ownerName);
        TextView comment = pview.findViewById(R.id.comment_object_comment);
        TextView timeElapsed = pview.findViewById(R.id.comments_object_postedTime);

        //set text for textview
        final String name=CommentList.get(position).getCommentorName();
        final String cmnt=CommentList.get(position).getComment();
        final String profPicURI = CommentList.get(position).getCommentorPicURL();

        final long createdTime = CommentList.get(position).getTimestamp();
        final long currentTime = System.currentTimeMillis();

        String timeString = HelperMethods.getTimeElapsed(currentTime, createdTime);

        userName.setText(name);
        comment.setText(cmnt);
        timeElapsed.setText(timeString);
        if (profPicURI.length() > 10) { //checking if its uploaded image
            Glide.with((Activity) mContext)
                    .load(profPicURI)
                    .into(profPic);
        }
        else if(profPicURI.equals("default")){
            int profilePic = Integer.parseInt(String.valueOf(R.drawable.default_profile_pic));
            Glide.with(mContext.getApplicationContext())
                    .load(ContextCompat.getDrawable(mContext.getApplicationContext(), profilePic))
                    .into(profPic);
        }
        else { //checking if it is default avatar
            int profilePic = Integer.parseInt(profPicURI);
            Glide.with(mContext.getApplicationContext())
                    .load(ContextCompat.getDrawable(mContext.getApplicationContext(), profilePic))
                    .into(profPic);
        }

        return pview;
    }
}