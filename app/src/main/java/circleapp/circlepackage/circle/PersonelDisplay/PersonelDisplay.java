package circleapp.circlepackage.circle.PersonelDisplay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.Person;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseRetrievalViewModel;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;

public class PersonelDisplay extends AppCompatActivity {

    //    private FirebaseAuth firebaseAuth;
    private List<Subscriber> applicantsList;
    private User user;

    private ImageButton back;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personel_display);
        Circle circle = SessionStorage.getCircle(this);
        user = SessionStorage.getUser(this);

        back = findViewById(R.id.bck_applicants_display);
        RecyclerView recyclerView = findViewById(R.id.allApplicants_RV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        applicantsList = new ArrayList<>(); //initialize membersList

        back.setOnClickListener(view -> {
            finishAfterTransition();
            startActivity(new Intent(PersonelDisplay.this, CircleWall.class));
        });


        final RecyclerView.Adapter adapter = new ApplicantListAdapter(this, applicantsList, circle);
        if (user.getUserId().equalsIgnoreCase(circle.getCreatorID()))
            recyclerView.setAdapter(adapter);

        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of(this).get(FirebaseRetrievalViewModel.class);

        LiveData<String[]> liveData = viewModel.getDataSnapsCirclePersonelLiveData(circle.getId(), "applicants");

        liveData.observe(this, returnArray -> {
            Subscriber subscriber = new Gson().fromJson(returnArray[0], Subscriber.class);
            String modifier = returnArray[1];
            switch (modifier) {
                case "added":
                    applicantsList.add(subscriber);
                    adapter.notifyDataSetChanged();
                    break;
                case "removed":
                    recyclerView.setAdapter(adapter);
                    int position = 0;
                    List<Subscriber> tempList = new ArrayList<>(applicantsList);
                    //when data is changed, check if object already exists. If exists delete and rewrite it to avoid duplicates.
                    for (Subscriber sub : tempList) {
                        if (sub.getId().equals(subscriber.getId())) {
                            applicantsList.remove(position);
                            adapter.notifyItemRemoved(position);
                            break;
                        }
                        position = position + 1;
                    }
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        finishAfterTransition();
        startActivity(new Intent(PersonelDisplay.this, CircleWall.class));
    }
}
