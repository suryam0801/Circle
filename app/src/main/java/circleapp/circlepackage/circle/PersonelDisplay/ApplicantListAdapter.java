package circleapp.circlepackage.circle.PersonelDisplay;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.Notification.SendNotification;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

public class ApplicantListAdapter extends RecyclerView.Adapter<ApplicantListAdapter.ViewHolder> {
    private Context mContext;
    private List<Subscriber> ApplicantList;
    private Circle circle;
    String TAG = "APPLICANT_LIST_ADAPTER";
    private FirebaseDatabase database;
    private DatabaseReference circlesPersonelDB, circleDB;
    private String state;

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

        database = FirebaseDatabase.getInstance();
        circlesPersonelDB = database.getReference("CirclePersonel");
        circleDB = database.getReference("Circles");

        Glide.with(mContext)
                .load(selectedApplicant.getPhotoURI())
                .placeholder(ContextCompat.getDrawable(mContext, R.drawable.person_singing))
                .into(holder.profPic);

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

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circlesPersonelDB.child(circle.getId()).child("applicants").child(selectedApplicant.getId()).removeValue();
                circlesPersonelDB.child(circle.getId()).child("members").child(selectedApplicant.getId()).setValue(selectedApplicant);
                circleDB.child(circle.getId()).child("membersList").child(selectedApplicant.getId()).setValue(true);
                state="Accepted";
                SendNotification.sendnotification(state,circle.getId(),circle.getName(),selectedApplicant.getId());
            }
        });

        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circlesPersonelDB.child(circle.getId()).child("applicants").child(selectedApplicant.getId()).removeValue();
                circleDB.child(circle.getId()).child("applicantsList").child(selectedApplicant.getId()).removeValue();
                state="Rejected";
                SendNotification.sendnotification(state,circle.getId(),circle.getName(),selectedApplicant.getId());
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