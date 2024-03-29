package circleapp.circleapppackage.circle.ui.PersonelDisplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import circleapp.circleapppackage.circle.DataLayer.CircleRepository;
import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Helpers.SendNotification;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class ApplicantListAdapter extends RecyclerView.Adapter<ApplicantListAdapter.ViewHolder> {
    private Context mContext;
    private List<Subscriber> ApplicantList;
    private Circle circle;
    String TAG = "APPLICANT_LIST_ADAPTER";
    private String state;
    private int propic;
    private String role = "normal";

    public ApplicantListAdapter(Context mContext, List<Subscriber> ApplicantList, Circle circle) {
        this.mContext = mContext;
        this.ApplicantList = ApplicantList;
        this.circle = circle;
    }

    @NonNull
    @Override
    public ApplicantListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.applicant_list_item, parent, false);
        return new ApplicantListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicantListAdapter.ViewHolder holder, int position) {
        final Subscriber selectedApplicant = ApplicantList.get(position);

        HelperMethodsUI.setMemberProfileImage(selectedApplicant.getPhotoURI(), mContext, holder.profPic);

        //Set text for TextView
        final String nameDisplay = selectedApplicant.getName();
        holder.name.setText(nameDisplay);

        //implemennt time unit conversion
        long currentTime = System.currentTimeMillis();
        long createdTime = selectedApplicant.getTimestamp();
        long days = TimeUnit.MILLISECONDS.toDays(currentTime - createdTime);
        long hours = TimeUnit.MILLISECONDS.toHours(currentTime - createdTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - createdTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(currentTime - createdTime);

        if (seconds < 60) {
            holder.timeElapsed.setText("Applied " + seconds + "s ago");
        } else if (minutes > 1 && minutes < 60) {
            holder.timeElapsed.setText("Applied " + minutes + "m ago");
        } else if (hours > 1 && hours < 24) {
            holder.timeElapsed.setText("Applied " + hours + "h ago");
        } else if (days > 1 && days < 365) {
            if (days > 7)
                holder.timeElapsed.setText("Applied " + (days / 7) + "w ago");
            else
                holder.timeElapsed.setText("Applied " + days + "d ago");
        }
        CircleRepository circleRepository = new CircleRepository();
        //send notification on accepting
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add user to member list, update users active circle count by 1
                circleRepository.acceptApplicant(circle.getId(), selectedApplicant, mContext, role);
                state = "Accepted";
                SendNotification.sendnotification(state, circle.getId(), circle.getName(), selectedApplicant.getId(),selectedApplicant.getToken_id(),selectedApplicant.getName());

            }
        });
        //send notification on rejecting
        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //remove user from applicants list of circle
                circleRepository.rejectApplicant(circle.getId(), selectedApplicant);
                state = "Rejected";
                SendNotification.sendnotification(state, circle.getId(), circle.getName(), selectedApplicant.getId(), selectedApplicant.getToken_id(), selectedApplicant.getName());
            }
        });
 
        holder.container.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.item_animation_fall_down));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return ApplicantList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, timeElapsed;
        CircleImageView profPic;
        Button accept, reject;
        LinearLayout container;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.applicant_cell_container);
            name = view.findViewById(R.id.applicant_name);
            timeElapsed = view.findViewById(R.id.applicant_time_since_applied);
            accept = view.findViewById(R.id.applicant_accept);
            reject = view.findViewById(R.id.applicant_reject);
            profPic = view.findViewById(R.id.applicant_profile_picture);
        }
    }
}