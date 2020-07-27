package circleapp.circlepackage.circle.ui.PersonelDisplay;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class MemberListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Subscriber> memberList;
    private TextView name, timeElapsed;
    private CircleImageView profPic;
    private LinearLayout container;
    private GlobalVariables globalVariables = new GlobalVariables();

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
        String picUrl = member.getPhotoURI();

        User user = globalVariables.getCurrentUser();

        HelperMethodsUI.setUserProfileImage(user, mContext.getApplicationContext(), profPic);

        //Set text for TextView
        final String nameDisplay = member.getName();
        name.setText(nameDisplay);

        long createdTime =member.getTimestamp();
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        String dateString = formatter.format(new Date(createdTime));
        timeElapsed.setText("Member since " + dateString);
        if (picUrl.length() > 10) { //checking if its uploaded image
            Glide.with((Activity) mContext)
                    .load(picUrl)
                    .into(profPic);
        } else if (picUrl.equals("default")) {
            int profilePic = Integer.parseInt(String.valueOf(R.drawable.default_profile_pic));
            Glide.with(mContext)
                    .load(ContextCompat.getDrawable(mContext, profilePic))
                    .into(profPic);
        } else { //checking if it is default avatar
            int index = Integer.parseInt(String.valueOf(picUrl.charAt(picUrl.length()-1)));
            index = index-1;
            TypedArray avatarResourcePos = mContext.getResources().obtainTypedArray(R.array.AvatarValues);
            int profilePic = avatarResourcePos.getResourceId(index, 0);
            Glide.with((Activity) mContext)
                    .load(ContextCompat.getDrawable(mContext, profilePic))
                    .into(profPic);
        }

        return pview;
    }
}