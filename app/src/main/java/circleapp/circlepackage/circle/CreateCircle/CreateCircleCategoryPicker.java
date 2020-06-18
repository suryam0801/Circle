package circleapp.circlepackage.circle.CreateCircle;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.Explore.WorkbenchDisplayAdapter;
import circleapp.circlepackage.circle.R;

public class CreateCircleCategoryPicker extends AppCompatActivity {

    List<String> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_circle_category_picker);

        categoryList.add("Health & Fitness");
        categoryList.add("Events");
        categoryList.add("Students & Clubs");
        categoryList.add("Apartments & Communities");
        categoryList.add("Sports");
        categoryList.add("Friends & Family");
        categoryList.add("Food & Entertainment");
        categoryList.add("Science & Tech");
        categoryList.add("Gaming");

        RecyclerView wbrecyclerView = findViewById(R.id.category_picker_recycler_view);
        wbrecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager wblayoutManager = new LinearLayoutManager(CreateCircleCategoryPicker.this, RecyclerView.VERTICAL, false);
        wbrecyclerView.setLayoutManager(wblayoutManager);

        final RecyclerView.Adapter wbadapter = new CategoryPickerAdapter(CreateCircleCategoryPicker.this, categoryList, CreateCircleCategoryPicker.this);
        wbrecyclerView.setAdapter(wbadapter);
    }
}