package circleapp.circleapppackage.circle.ui.Explore;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;

import circleapp.circleapppackage.circle.Helpers.SessionStorage;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.ui.ExploreTabbedActivity;

public class ExploreCategoryFilter extends AppCompatActivity {

    private MaterialCheckBox healthAndFitness, events, studentsAndClubs, apartmentsAndCommunities, sports,
            friendsAndFamily, foodAndEntertainment, scienceAndTech, gaming, general;

    private ImageButton back;
    private Button applyFilter;
    private List<String> selectedFilters = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_category_filter);

        back = findViewById(R.id.bck_explore_filter);
        applyFilter = findViewById(R.id.apply_filter_button);

        healthAndFitness = findViewById(R.id.health_and_fitness_filter);
        events = findViewById(R.id.events_filter);
        studentsAndClubs = findViewById(R.id.students_and_clubs_filter);
        apartmentsAndCommunities = findViewById(R.id.apartments_communities_filter);
        sports = findViewById(R.id.sports_filter);
        friendsAndFamily = findViewById(R.id.friends_and_family_filter);
        foodAndEntertainment = findViewById(R.id.food_and_entertainment_filter);
        scienceAndTech = findViewById(R.id.science_and_tech_filter);
        gaming = findViewById(R.id.gaming_filter);
        general = findViewById(R.id.general_filter);

        if (SessionStorage.getFilters(this) != null)
            selectedFilters = SessionStorage.getFilters(this);

        if (selectedFilters != null) {
            for (String filter : selectedFilters) {
                switch (filter) {
                    case "Health & Fitness":
                        healthAndFitness.setChecked(true);
                        break;
                    case "Events":
                        events.setChecked(true);
                        break;
                    case "Students & Clubs":
                        studentsAndClubs.setChecked(true);
                        break;
                    case "Apartments & Communities":
                        apartmentsAndCommunities.setChecked(true);
                        break;
                    case "Sports":
                        sports.setChecked(true);
                        break;
                    case "Friends & Family":
                        friendsAndFamily.setChecked(true);
                        break;
                    case "Food & Entertainment":
                        foodAndEntertainment.setChecked(true);
                        break;
                    case "Science & Tech":
                        scienceAndTech.setChecked(true);
                        break;
                    case "Gaming":
                        gaming.setChecked(true);
                        break;
                    case "General":
                        general.setChecked(true);
                        break;

                }
            }
        }


        healthAndFitness.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                selectedFilters.add("Health & Fitness");
            else
                selectedFilters.remove("Health & Fitness");
        });
        events.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                selectedFilters.add("Events");
            else
                selectedFilters.remove("Events");
        });
        studentsAndClubs.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                selectedFilters.add("Students & Clubs");
            else
                selectedFilters.remove("Students & Clubs");
        });
        apartmentsAndCommunities.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                selectedFilters.add("Apartments & Communities");
            else
                selectedFilters.remove("Apartments & Communities");
        });

        sports.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                selectedFilters.add("Sports");
            else
                selectedFilters.remove("Sports");
        });
        friendsAndFamily.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                selectedFilters.add("Friends & Family");
            else
                selectedFilters.remove("Friends & Family");
        });
        foodAndEntertainment.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                selectedFilters.add("Food & Entertainment");
            else
                selectedFilters.remove("Food & Entertainment");
        });
        scienceAndTech.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                selectedFilters.add("Science & Tech");
            else
                selectedFilters.remove("Science & Tech");
        });
        gaming.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                selectedFilters.add("Gaming");
            else
                selectedFilters.remove("Gaming");
        });
        general.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                selectedFilters.add("General");
            else
                selectedFilters.remove("General");
        });

        back.setOnClickListener(view -> {
            finishAfterTransition();
            Intent intent = new Intent(ExploreCategoryFilter.this, ExploreTabbedActivity.class);
            intent.putExtra("fromFilters", true);
            startActivity(intent);
        });

        applyFilter.setOnClickListener(view -> {
            finishAfterTransition();
            Intent intent = new Intent(ExploreCategoryFilter.this, ExploreTabbedActivity.class);
            intent.putExtra("fromFilters", true);
            SessionStorage.saveFilters(this, selectedFilters);
            startActivity(intent);
        });
    }
}