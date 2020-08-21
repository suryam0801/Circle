package circleapp.circleapppackage.circle.ui.PersonelDisplay;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import circleapp.circleapppackage.circle.DataLayer.CircleRepository;
import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.viewpager.widget.PagerAdapter.POSITION_NONE;

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.ViewHolder> {

    private Context mContext;
    private Circle circle;
    private User user;
    private List<Subscriber> memberList;
    private boolean circleWall;
    private GlobalVariables globalVariables = new GlobalVariables();
    private Dialog removeUserDialog;

    public MemberListAdapter(){}

    public MemberListAdapter(Context mContext, List<Subscriber> memberList, boolean circleWall) {
        this.mContext = mContext;
        this.memberList = memberList;
        this.circleWall = circleWall;
    }

    @NonNull
    @Override
    public MemberListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_cell_item, viewGroup, false);
        return new MemberListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberListAdapter.ViewHolder holder, int position) {

        final Subscriber member = memberList.get(position);
        if(circleWall){
            circle = globalVariables.getCurrentCircle();
            user = globalVariables.getCurrentUser();
            String userId = user.getUserId();
            if(circle.getMembersList().get(userId).equals("admin")){
                if(circle.getMembersList().get(member.getId())!=null){
                    if(circle.getMembersList().get(member.getId()).equals("admin")){
                        holder.userRole.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        holder.container.setOnClickListener(v->{
                            showMakeAdminDialog(member, holder);
                        });
                    }
                }
            }
        }
        HelperMethodsUI.setMemberProfileImage(member.getPhotoURI(), mContext.getApplicationContext(), holder.profPic);

        //Set text for TextView
        final String nameDisplay = member.getName();
        holder.name.setText(nameDisplay);

        long createdTime =member.getTimestamp();
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        String dateString = formatter.format(new Date(createdTime));
        holder.timeElapsed.setText("Member since " + dateString);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public void showMakeAdminDialog(Subscriber member, ViewHolder holder) {
        removeUserDialog = new Dialog(mContext);
        removeUserDialog.setContentView(R.layout.remove_member_dialog);
        final Button closeDialogButton = removeUserDialog.findViewById(R.id.make_admin_confirm_btn);
        final Button cancel = removeUserDialog.findViewById(R.id.make_admin_cancel_btn);
        final TextView title = removeUserDialog.findViewById(R.id.make_admin_dialog_title);

        title.setText("Do you want to make "+ member.getName() +" an Admin?");

        closeDialogButton.setOnClickListener(view -> {
            CircleRepository circleRepository = new CircleRepository();
            circleRepository.makeAdmin(circle, member);
            holder.userRole.setVisibility(View.VISIBLE);
            removeUserDialog.dismiss();
        });

        cancel.setOnClickListener(view -> removeUserDialog.dismiss());

        removeUserDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        removeUserDialog.show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, timeElapsed, userRole;
        private CircleImageView profPic;
        private LinearLayout container;

        public ViewHolder(@NonNull View pview) {
            super(pview);
            container = pview.findViewById(R.id.member_cell_container);
            name = pview.findViewById(R.id.member_name);
            timeElapsed = pview.findViewById(R.id.member_since_display);
            profPic = pview.findViewById(R.id.member_profile_picture);
            userRole = pview.findViewById(R.id.member_role);
        }
    }
}