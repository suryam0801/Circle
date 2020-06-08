package circleapp.circlepackage.circle.Explore;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.R;

public class WorkbenchDisplayAdapter extends RecyclerView.Adapter<WorkbenchDisplayAdapter.ViewHolder> {

    private List<Circle> MycircleList;
    private Context context;
    private int counter = 0;

    //contructor to set MycircleList and context for Adapter
    public WorkbenchDisplayAdapter(List<Circle> mycircleList, Context context) {
        this.MycircleList = mycircleList;
        this.context = context;
    }

    @NonNull
    @Override
    public WorkbenchDisplayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.workbench_circle_display, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkbenchDisplayAdapter.ViewHolder holder, int position) {

        Circle circle = MycircleList.get(position);

        GradientDrawable wbItemBackground = new GradientDrawable();
        wbItemBackground.setShape(GradientDrawable.OVAL);

        wbItemBackground.setColor(Color.parseColor("#E0F0FF"));
        wbItemBackground.setStroke(4, Color.parseColor("#158BF1"));
        holder.tv_MycircleName.setTextColor(Color.parseColor("#158BF1"));

        //set the details of each circle to its respective card.
        holder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
        holder.container.setBackground(wbItemBackground);
        holder.tv_MycircleName.setText(circle.getName());
        holder.tv_circleCreatorName.setText(circle.getCreatorName());

        if(circle.getMembersList()!=null){
            if(circle.getMembersList().keySet().size()>3){
                holder.membersDisplay.setText("+" + circle.getMembersList().keySet().size());
            } else {
                holder.membersDisplay.setText("+0" + circle.getMembersList().keySet().size());
            }
        } else {
            holder.membersDisplay.setText("+0");
        }

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(circle.getTimestamp());
        String date = DateFormat.format("dd MMM, yyyy", cal).toString();
        holder.tv_circleCreatedDateWB.setText(date);
    }

    @Override
    public int getItemCount() {
        return MycircleList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_MycircleName, tv_circleCreatorName, tv_circleCreatedDateWB;
        private Button unreadNotif;
        private LinearLayout container;
        private Button membersDisplay;
        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.wbContainer);
            tv_MycircleName = view.findViewById(R.id.wbcircleName);
            tv_circleCreatorName = view.findViewById(R.id.wbcircle_creatorName);
            membersDisplay = view.findViewById(R.id.wb_members_count_button);
            tv_circleCreatedDateWB = view.findViewById(R.id.circle_created_date);
            unreadNotif = view.findViewById(R.id.unreadNotifButton);
        }
    }
}
