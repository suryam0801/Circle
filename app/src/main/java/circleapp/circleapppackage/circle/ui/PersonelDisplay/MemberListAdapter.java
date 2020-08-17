package circleapp.circleapppackage.circle.ui.PersonelDisplay;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import circleapp.circleapppackage.circle.DataLayer.CircleRepository;
import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import de.hdodenhof.circleimageview.CircleImageView;

public class MemberListAdapter extends BaseAdapter {

    private Context mContext;
    private Circle circle;
    private User user;
    private List<Subscriber> memberList;
    private TextView name, timeElapsed, userRole;
    private CircleImageView profPic;
    private LinearLayout container;
    private boolean circleWall;
    private GlobalVariables globalVariables = new GlobalVariables();
    private Dialog removeUserDialog;
    private Button removeMember;

    public MemberListAdapter(Context mContext, List<Subscriber> memberList, boolean circleWall) {
        this.mContext = mContext;
        this.memberList = memberList;
        this.circleWall = circleWall;
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
        removeMember = pview.findViewById(R.id.remove_member);
        userRole = pview.findViewById(R.id.member_role);

        final Subscriber member = memberList.get(position);

        if(circleWall){
            circle = globalVariables.getCurrentCircle();
            user = globalVariables.getCurrentUser();
            String userId = user.getUserId();
            if(circle.getMembersList().get(userId).equals("admin")){
                if(circle.getMembersList().get(member.getId())!=null){
                    if(circle.getMembersList().get(member.getId()).equals("admin")){
                        removeMember.setVisibility(View.GONE);
                        userRole.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        container.setOnLongClickListener(v->{
                            showMakeAdminDialog(member);
                            return true;
                        });
                        removeMember.setVisibility(View.VISIBLE);
                    }
                }
            }
            removeMember.setOnClickListener(v->{
                CircleRepository circleRepository = new CircleRepository();
                circleRepository.removeMember(circle, member);
            });
        }
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

    public void showMakeAdminDialog(Subscriber member) {
        removeUserDialog = new Dialog(mContext);
        removeUserDialog.setContentView(R.layout.remove_member_dialog);
        final Button closeDialogButton = removeUserDialog.findViewById(R.id.make_admin_confirm_btn);
        final Button cancel = removeUserDialog.findViewById(R.id.make_admin_cancel_btn);

        closeDialogButton.setOnClickListener(view -> {
            CircleRepository circleRepository = new CircleRepository();
            circleRepository.makeAdmin(circle, member);
            userRole.setVisibility(View.VISIBLE);
            removeMember.setVisibility(View.GONE);
            removeUserDialog.dismiss();
        });

        cancel.setOnClickListener(view -> removeUserDialog.dismiss());

        removeUserDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        removeUserDialog.show();
    }
}