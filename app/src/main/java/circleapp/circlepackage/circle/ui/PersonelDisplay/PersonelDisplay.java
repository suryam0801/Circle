package circleapp.circlepackage.circle.ui.PersonelDisplay;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;

public class PersonelDisplay extends AppCompatActivity {

    //    private FirebaseAuth firebaseAuth;
    private List<Subscriber> applicantsList;
    private User user;
    private Circle circle;
    private GlobalVariables globalVariables = new GlobalVariables();
    private ImageButton back;
    private RecyclerView.Adapter adapter;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personel_display);
        circle = globalVariables.getCurrentCircle();
        user = globalVariables.getCurrentUser();

        back = findViewById(R.id.bck_applicants_display);
        RecyclerView recyclerView = findViewById(R.id.allApplicants_RV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        applicantsList = new ArrayList<>(); //initialize membersList

        back.setOnClickListener(view -> {
            finishAfterTransition();
            startActivity(new Intent(PersonelDisplay.this, CircleWall.class));
        });


        adapter = new ApplicantListAdapter(this, applicantsList, circle);
        if (user.getUserId().equalsIgnoreCase(circle.getCreatorID()))
            recyclerView.setAdapter(adapter);
        setObserverForApplicants(recyclerView);
    }
    private void setObserverForApplicants(RecyclerView recyclerView){
        CirclePersonnelViewModel viewModel = ViewModelProviders.of(this).get(CirclePersonnelViewModel.class);
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
                    removeMember(subscriber);
            }
        });
    }

    private void removeMember(Subscriber subscriber){
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        finishAfterTransition();
        startActivity(new Intent(PersonelDisplay.this, CircleWall.class));
    }
}