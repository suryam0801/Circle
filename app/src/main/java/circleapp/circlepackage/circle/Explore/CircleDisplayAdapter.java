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

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.R;

public class CircleDisplayAdapter extends RecyclerView.Adapter<CircleDisplayAdapter.ViewHolder> {
    private List<Circle> circleList;
    private Context context;
    private int counter = 0;

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

        Circle current = circleList.get(i);

        GradientDrawable wbItemBackground = new GradientDrawable();
        wbItemBackground.setShape(GradientDrawable.RECTANGLE);
        wbItemBackground.setCornerRadius(15.0f);

        GradientDrawable wbIconBackground = new GradientDrawable();
        wbIconBackground.setShape(GradientDrawable.RECTANGLE);
        wbIconBackground.setCornerRadius(15.0f);

        switch (counter%3){
            case 0:
                wbItemBackground.setColor(Color.parseColor("#D8E9FF"));
                wbIconBackground.setColor(Color.parseColor("#158BF1"));
                counter++;
                break;
            case 1:
                wbItemBackground.setColor(Color.parseColor("#FFD1E9"));
                wbIconBackground.setColor(Color.parseColor("#FF38A2"));
                counter++;
                break;
            case 2:
                wbItemBackground.setColor(Color.parseColor("#BBFFE1"));
                wbIconBackground.setColor(Color.parseColor("#11F692"));
                counter++;
                break;
        }


        viewHolder.headingBackground.setBackground(wbItemBackground);
        viewHolder.foregroundImage.setBackground(wbIconBackground);

        //set the details of each circle to its respective card.
        viewHolder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
        viewHolder.tv_circleName.setText(current.getName());
        viewHolder.tv_creatorName.setText(current.getCreatorName());
        viewHolder.tv_circleDesc.setText(current.getDescription());
        viewHolder.foregroundImage.setText(current.getName().toUpperCase().charAt(0)+"");
    }

    @Override
    public int getItemCount() {
        return circleList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_circleName, tv_creatorName, tv_circleDesc;
        private TextView foregroundImage;
        private LinearLayout container, headingBackground;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            tv_circleName = view.findViewById(R.id.circle_name);
            tv_creatorName = view.findViewById(R.id.circle_creatorName);
            tv_circleDesc = view.findViewById(R.id.circle_desc);
            headingBackground = view.findViewById(R.id.explore_card_heading_background);
            foregroundImage = view.findViewById(R.id.explore_foreground_icon);
        }

    }
}