package circleapp.circlepackage.circle.ui.Feedback;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.data.ObjectModels.Feedback;
import circleapp.circlepackage.circle.R;

public class FeedbackAdapter extends BaseAdapter {

    private Context mContext;
    private List<Feedback> feedbackList;

    public FeedbackAdapter(Context mContext, List<Feedback> feedbacklist) {
        this.mContext = mContext;
        feedbackList = feedbacklist;
    }

    @Override
    public int getCount() {
        return feedbackList.size();
    }

    @Override
    public Object getItem(int position) {
        return feedbackList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final View pview = View.inflate(mContext, R.layout.feedback_display, null);

        TextView userName = pview.findViewById(R.id.feedback_object_ownerName);
        TextView feedback = pview.findViewById(R.id.feedback_object_feedback);
        TextView timeElapsed = pview.findViewById(R.id.feedback_object_postedTime);

        //set text for textview
        final String name= feedbackList.get(position).getUserName();
        final String feed=feedbackList.get(position).getFeedback();

        final long createdTime = feedbackList.get(position).getTimestamp();
        final long currentTime = System.currentTimeMillis();
        String timeString = HelperMethods.getTimeElapsed(currentTime, createdTime);

        userName.setText(name);
        feedback.setText(feed);
        timeElapsed.setText(timeString);
        return pview;
    }
}
