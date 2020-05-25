package circleapp.circlepackage.circle.Explore;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.R;

public class WorkbenchDisplayAdapter extends RecyclerView.Adapter<WorkbenchDisplayAdapter.ViewHolder> {

    private List<Circle> MycircleList;
    private Context context;

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

        GradientDrawable wbItemBackground = new GradientDrawable();
        wbItemBackground.setShape(GradientDrawable.OVAL);

        wbItemBackground.setColor(Color.parseColor("#E0F0FF"));
        wbItemBackground.setStroke(4, Color.parseColor("#158BF1"));
        holder.tv_MycircleName.setTextColor(Color.parseColor("#158BF1"));

        //set the details of each circle to its respective card.
        holder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
        holder.tv_MycircleName.setBackground(wbItemBackground);
        holder.tv_MycircleName.setText(MycircleList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return MycircleList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_MycircleName;
        private LinearLayout container;
        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.wbContainer);
            tv_MycircleName = view.findViewById(R.id.wbcircle);

        }
    }
}
