package circleapp.circlepackage.circle.PersonelDisplay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.R;

public class PersonelDisplay extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabItem newRequests, membersDisplay;
    public PersonelTabAdapter pagerAdapter;
    private ImageButton back;
    String userState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personel_display);

        tabLayout = findViewById(R.id.personel_tab_layout);
        back = findViewById(R.id.personel_bck_btn);
        newRequests = findViewById(R.id.personel_new_requests);
        //membersDisplay = findViewById(R.id.main_workbench_tab);
        viewPager = findViewById(R.id.personel_viewpager);
        userState = getIntent().getStringExtra("userState");

        pagerAdapter = new PersonelTabAdapter(getSupportFragmentManager(), tabLayout.getTabCount(),userState);
        viewPager.setAdapter(pagerAdapter);

        back.setOnClickListener(view -> {
            startActivity(new Intent(PersonelDisplay.this, CircleWall.class));
            finish();
        });


        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {


            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PersonelDisplay.this, CircleWall.class));
        finish();
    }
}
