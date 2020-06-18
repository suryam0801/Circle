package circleapp.circlepackage.circle.CreateCircle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.R;

public class CategoryPickerAdapter extends RecyclerView.Adapter<CategoryPickerAdapter.ViewHolder> {

    private List<String> categoryList;
    private Context context;

    public CategoryPickerAdapter(Context context, List<String> categoryList, CreateCircleCategoryPicker createCircleCategoryPicker) {
        this.categoryList = categoryList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.category_picker_item, viewGroup, false);
        return new circleapp.circlepackage.circle.CreateCircle.CategoryPickerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.categoryName.setText(categoryList.get(position));
        holder.container.setOnClickListener(view -> {
            Intent intent = new Intent(context, CreateCircle.class);
            intent.putExtra("category_name", categoryList.get(position));
            context.startActivity(intent);
            ((Activity) context).finish();
        });
    }


    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryName;
        private LinearLayout container;
        public ViewHolder(View view) {
            super(view);
            categoryName = view.findViewById(R.id.category_picker_category_name);
            container = view.findViewById(R.id.category_picker_item_cointainer);
        }
    }
}