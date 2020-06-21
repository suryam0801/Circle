package circleapp.circlepackage.circle.CircleWall;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends BaseAdapter {

    private Context mContext;
    private List<Comment> CommentList;
    private int count = 0;
    private  int propic;
    int myImageList;
//    int[] myImageList = new int[]{R.drawable.avatar1, R.drawable.avatar3, R.drawable.avatar4,
//            R.drawable.avatar2, R.drawable.avatar5};


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

        long days = TimeUnit.MILLISECONDS.toDays(currentTime - createdTime);
        long hours = TimeUnit.MILLISECONDS.toHours(currentTime - createdTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - createdTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(currentTime - createdTime);

        userName.setText(name);
        comment.setText(cmnt);

        if (profPicURI.length() > 10) {
            Glide.with(mContext)
                    .load(profPicURI)
                    .into(profPic);
        } else {
            propic = Integer.parseInt(profPicURI);
            myImageList = propic;
            Glide.with(mContext)
                    .load(propic)
                    .placeholder(ContextCompat.getDrawable(mContext, myImageList))
                    .into(profPic);
        }
        ++count;
        if(count == 4) count = 0;


        if(seconds < 60) {
            timeElapsed.setText(seconds + "s ago");
        } else if (minutes >= 1 && minutes < 60){
            timeElapsed.setText(minutes + "m ago");
        } else if (hours >= 1 && hours < 24) {
            timeElapsed.setText(hours + "h ago");
        } else if (days >= 1 && days < 365 ) {
            if(days >= 7)
                timeElapsed.setText((days/7) + "w ago");
            else
                timeElapsed.setText(days + "d ago");
        }

        return pview;
    }
}