package circleapp.circlepackage.circle.PersonelDisplay;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.rpc.Help;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class MemberListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Subscriber> memberList;
    String TAG = "APPLICANT_LIST_ADAPTER";
    TextView name, timeElapsed;
    CircleImageView profPic;
    LinearLayout container;

    public MemberListAdapter(Context mContext, List<Subscriber> memberList) {
        this.mContext = mContext;
        this.memberList = memberList;
    }
    @Override
    public int getCount() {
        return memberList.size();
    }

    @Override
    public Object getItem(int position) {
        return memberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final View pview = View.inflate(mContext, R.layout.member_cell_item, null);

        container = pview.findViewById(R.id.member_cell_container);
        name = pview.findViewById(R.id.member_name);
        timeElapsed = pview.findViewById(R.id.member_since_display);
        profPic = pview.findViewById(R.id.member_profile_picture);

        final Subscriber member = memberList.get(position);

        User user = SessionStorage.getUser((Activity) mContext);

        HelperMethods.setUserProfileImage(user, mContext, profPic);

        //Set text for TextView
        final String nameDisplay = member.getName();
        name.setText(nameDisplay);

        long createdTime =member.getTimestamp();
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        String dateString = formatter.format(new Date(createdTime));
        timeElapsed.setText("Member since " + dateString);

        return pview;
    }
}