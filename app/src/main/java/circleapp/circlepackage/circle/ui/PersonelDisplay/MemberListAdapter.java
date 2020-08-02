package circleapp.circlepackage.circle.ui.PersonelDisplay;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class MemberListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Subscriber> memberList;
    private TextView name, timeElapsed;
    private CircleImageView profPic;
    private LinearLayout container;

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

        HelperMethodsUI.setUserProfileImage(member.getPhotoURI(), mContext.getApplicationContext(), profPic);

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