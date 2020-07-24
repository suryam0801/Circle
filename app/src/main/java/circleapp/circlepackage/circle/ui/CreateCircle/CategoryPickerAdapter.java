package circleapp.circlepackage.circle.ui.CreateCircle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import circleapp.circlepackage.circle.R;

public class CategoryPickerAdapter extends RecyclerView.Adapter<CategoryPickerAdapter.ViewHolder> {

    private List<String> categoryList;
    private List<Drawable> iconList;
    private Context context;

    public CategoryPickerAdapter(Context context, List<String> categoryList, List<Drawable> iconList) {
        this.categoryList = categoryList;
        this.iconList = iconList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.category_picker_item, viewGroup, false);
        return new CategoryPickerAdapter.ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.categoryName.setText(categoryList.get(position));
        holder.iconImageView.setBackground(iconList.get(position));
        holder.container.setOnClickListener(view -> {
            sendCategoryToCreateCircle(position);
        });
    }

    private void sendCategoryToCreateCircle(int position){
        Intent intent = new Intent(context, CreateCircle.class);
        intent.putExtra("category_name", categoryList.get(position));
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryName;
        private AppCompatImageView iconImageView;
        private LinearLayout container;
        public ViewHolder(View view) {
            super(view);
            categoryName = view.findViewById(R.id.category_picker_category_name);
            container = view.findViewById(R.id.category_picker_item_cointainer);
            iconImageView = view.findViewById(R.id.category_picker_icon_display);
        }
    }
}
