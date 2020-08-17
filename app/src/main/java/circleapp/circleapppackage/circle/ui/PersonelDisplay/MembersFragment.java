package circleapp.circleapppackage.circle.ui.PersonelDisplay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;

public class MembersFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RecyclerView membersDisplay;
    private RecyclerView.Adapter adapter;
    private List<Subscriber> memberList= new ArrayList<>();
    private Circle circle;
    private LiveData<String[]> liveData;
    private GlobalVariables globalVariables = new GlobalVariables();

    MembersFragment(){}

    public static MembersFragment newInstance(String param1, String param2) {
        MembersFragment fragment = new MembersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        circle = globalVariables.getCurrentCircle();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_members_list, container, false);
        membersDisplay = view.findViewById(R.id.all_members_RV);
        loadMembersList();
        return view;
    }

    private void loadMembersList() {
        //initialize membersList
        membersDisplay.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        membersDisplay.setLayoutManager(layoutManager);
        adapter = new MemberListAdapter(getActivity(), memberList,true);
        membersDisplay.setAdapter(adapter);

        CirclePersonnelViewModel viewModel = ViewModelProviders.of(this).get(CirclePersonnelViewModel.class);
        liveData = viewModel.getDataSnapsCirclePersonelLiveData(circle.getId(), "members");

        liveData.observe(this, returnArray -> {
            Subscriber subscriber = new Gson().fromJson(returnArray[0], Subscriber.class);
            if(subscriber!=null){
                String modifierType = returnArray[1];
                switch (modifierType) {
                    case "added":
                        if(!memberList.contains(subscriber)){
                            memberList.add(subscriber);
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    case "changed":
                        int index = HelperMethodsUI.returnIndexOfMemberList(memberList, subscriber);
                        memberList.remove(index);
                        memberList.add(index, subscriber);
                        adapter.notifyItemChanged(index);
                        break;
                    case "removed":
                        memberList.remove(subscriber);
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        liveData.removeObservers(this);
    }
}
