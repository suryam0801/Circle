package circleapp.circlepackage.circle.Explore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.R;

public class CircleDisplayAdapter extends RecyclerView.Adapter<CircleDisplayAdapter.ViewHolder> {
    private List<Circle> circleList;
    private Context context;

    //contructor to set latestCircleList and context for Adapter
    public CircleDisplayAdapter(Context context, List<Circle> circleList) {
        this.context = context;
        this.circleList = circleList;
    }

    @Override
    public CircleDisplayAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.circle_card_display_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CircleDisplayAdapter.ViewHolder viewHolder, int i) {

        //set the details of each circle to its respective card.
        viewHolder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
        viewHolder.tv_circleName.setText(circleList.get(i).getName());
        viewHolder.tv_creatorName.setText(circleList.get(i).getCreatorName());
        viewHolder.tv_circleDesc.setText(circleList.get(i).getDescription());
    }

    @Override
    public int getItemCount() {
        return circleList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_circleName, tv_creatorName, tv_circleDesc;
        private AppCompatImageView foregroundImage;
        private LinearLayout container;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            tv_circleName = view.findViewById(R.id.circle_name);
            tv_creatorName = view.findViewById(R.id.circle_creatorName);
            tv_circleDesc = view.findViewById(R.id.circle_desc);
            foregroundImage = view.findViewById(R.id.explore_card_icon_foreground);
        }

    }
}