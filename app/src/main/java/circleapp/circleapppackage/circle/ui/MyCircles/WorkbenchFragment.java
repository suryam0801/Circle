package circleapp.circleapppackage.circle.ui.MyCircles;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import circleapp.circleapppackage.circle.DataLayer.UserRepository;
import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circleapppackage.circle.ui.CreateCircle.CreateCircleCategoryPicker;
import circleapp.circleapppackage.circle.ui.Explore.ExploreFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WorkbenchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WorkbenchFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private List<Circle> workbenchCircleList = new ArrayList<>();
    private static User user;
    private LinearLayout emptyDisplay;
    private RecyclerView.Adapter wbadapter;
    private LiveData<String[]> liveData;
    private GlobalVariables globalVariables = new GlobalVariables();

    public WorkbenchFragment() {
        // Required empty public constructor
    }

    public static WorkbenchFragment newInstance(String param1, String param2) {
        WorkbenchFragment fragment = new WorkbenchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workbench, container, false);

        user = globalVariables.getCurrentUser();

        emptyDisplay = view.findViewById(R.id.workbench_empty_display);
        ImageButton explore = view.findViewById(R.id.placeholder_explore_circle_layout);
        ImageButton create = view.findViewById(R.id.placeholder_create_circle_layout);

        //initialize  workbench recylcerview
        RecyclerView wbrecyclerView = view.findViewById(R.id.wbRecyclerView);
        wbrecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager wblayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        wbrecyclerView.setLayoutManager(wblayoutManager);
        //initializing the WorkbenchDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual circles in the recycler view
        wbadapter = new WorkbenchDisplayAdapter(workbenchCircleList, getActivity());
        wbrecyclerView.setAdapter(wbadapter);
        setEmptyPlaceholder();

        create.setOnClickListener(view12 -> {
            startActivity(new Intent(getActivity(), CreateCircleCategoryPicker.class));
            getActivity().finish();
        });

        explore.setOnClickListener(view1 -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ExploreFragment()).commit();
        });

        circlesObserver();

        return view;
    }
    private void circlesObserver(){
        MyCirclesViewModel viewModel = ViewModelProviders.of(this).get(MyCirclesViewModel.class);
        liveData = viewModel.getDataSnapsWorkbenchCircleLiveData(user.getUserId());
        liveData.observe(getViewLifecycleOwner(), returnArray -> {
            Circle circle = new Gson().fromJson(returnArray[0], Circle.class);
            String modifierType = returnArray[1];
            switch (modifierType) {
                case "added":
                    if(circle.isAdminVisibility()==true)
                        addCircle(circle);
                    break;
                case "changed":
                    if(circle.isAdminVisibility()==true)
                        changeCircle(circle);
                    break;
                case "removed":
                    removeCircle(circle);
                    break;
            }
        });
    }

    private void setEmptyPlaceholder(){
        int noOfCircleInvolvements = globalVariables.getInvolvedCircles();
        if(user.getActiveCircles()!=null)
            noOfCircleInvolvements = user.getActiveCircles().size();
        if (user.getCreatedCircles() != 0)
            noOfCircleInvolvements = noOfCircleInvolvements+user.getCreatedCircles();
        globalVariables.setInvolvedCircles(noOfCircleInvolvements);
        if(globalVariables.getInvolvedCircles()==0)
            emptyDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        workbenchCircleList.clear();
        liveData.removeObservers(this);
        super.onPause();
    }

    public void addCircle(Circle circle) {
        //add new circle to list
        if(circle.getLastActivityTimeStamp()!=0){
            workbenchCircleList = HelperMethodsUI.getCirclePostion(workbenchCircleList, circle);
        }
        else {
            workbenchCircleList.add(workbenchCircleList.size()+1,circle);
        }
        workbenchCircleList.add(circle);
        wbadapter.notifyDataSetChanged();
        UserRepository userRepository = new UserRepository();
        userRepository.initializeNewCount( circle, user);
        emptyDisplay.setVisibility(View.GONE);
    }

    public void changeCircle(Circle circle) {
        int index = HelperMethodsUI.returnIndexOfCircleList(workbenchCircleList, circle);
        workbenchCircleList.remove(index);
        workbenchCircleList.add(index, circle);
        wbadapter.notifyItemChanged(index);
    }

    public void removeCircle(Circle circle) {
        int position = HelperMethodsUI.returnIndexOfCircleList(workbenchCircleList, circle);
        workbenchCircleList.remove(position);
        wbadapter.notifyItemChanged(position);
        if(globalVariables.getInvolvedCircles()==0)
            emptyDisplay.setVisibility(View.VISIBLE);
    }
}
