package circleapp.circleapppackage.circle.ui.CreateCircle;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.ui.ExploreTabbedActivity;

public class CreateCircleCategoryPicker extends AppCompatActivity {

    private List<String> categoryList = new ArrayList<>();
    private List<Drawable> iconList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_circle_category_picker);
        setCategoryValues();
        setRecyclerView();

    }

    private void setRecyclerView(){
        RecyclerView wbrecyclerView = findViewById(R.id.category_picker_recycler_view);
        wbrecyclerView.setNestedScrollingEnabled(false);
        wbrecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager wblayoutManager = new LinearLayoutManager(CreateCircleCategoryPicker.this, RecyclerView.VERTICAL, false);
        wbrecyclerView.setLayoutManager(wblayoutManager);

        final RecyclerView.Adapter wbadapter = new CategoryPickerAdapter(CreateCircleCategoryPicker.this, categoryList, iconList);
        wbrecyclerView.setAdapter(wbadapter);
    }

    private void setCategoryValues(){
        categoryList.add("General");
        categoryList.add("Health & Fitness");
        categoryList.add("Events");
        categoryList.add("Students & Clubs");
        categoryList.add("Apartments & Communities");
        categoryList.add("Sports");
        categoryList.add("Friends & Family");
        categoryList.add("Food & Entertainment");
        categoryList.add("Science & Tech");
        categoryList.add("Gaming");

        iconList.add(getResources().getDrawable(R.drawable.general_icon));
        iconList.add(getResources().getDrawable(R.drawable.barbell));
        iconList.add(getResources().getDrawable(R.drawable.calendar));
        iconList.add(getResources().getDrawable(R.drawable.students));
        iconList.add(getResources().getDrawable(R.drawable.buildings));
        iconList.add(getResources().getDrawable(R.drawable.football));
        iconList.add(getResources().getDrawable(R.drawable.friends));
        iconList.add(getResources().getDrawable(R.drawable.popcorn));
        iconList.add(getResources().getDrawable(R.drawable.smartphone));
        iconList.add(getResources().getDrawable(R.drawable.gaming));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        finishAfterTransition();
        startActivity(new Intent(this, ExploreTabbedActivity.class));
    }
}