package circleapp.circlepackage.circle.ui.PersonelDisplay;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;

public class ApplicantsFragment extends Fragment {

    private List<Subscriber> applicantsList;
    private User user;
    private Circle circle;
    private GlobalVariables globalVariables = new GlobalVariables();
    private ImageButton back;
    private RecyclerView.Adapter adapter;

    ApplicantsFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_applicants_list, container, false);
        circle = globalVariables.getCurrentCircle();
        user = globalVariables.getCurrentUser();

        back = view.findViewById(R.id.bck_applicants_display);
        RecyclerView recyclerView = view.findViewById(R.id.allApplicants_RV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        applicantsList = new ArrayList<>(); //initialize membersList


        adapter = new ApplicantListAdapter(getContext(), applicantsList, circle);
        if (user.getUserId().equalsIgnoreCase(circle.getCreatorID()))
            recyclerView.setAdapter(adapter);
        setObserverForApplicants(recyclerView);
        return view;
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
}
