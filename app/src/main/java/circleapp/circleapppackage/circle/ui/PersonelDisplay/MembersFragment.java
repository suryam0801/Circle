package circleapp.circleapppackage.circle.ui.PersonelDisplay;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

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

    private ListView membersDisplay;
    private List<Subscriber> memberList;
    private Circle circle;
    private LiveData<String[]> liveData;
    private GlobalVariables globalVariables = new GlobalVariables();

    MembersFragment(){}

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
        memberList = new ArrayList<>(); //initialize membersList
        final MemberListAdapter adapter = new MemberListAdapter(getContext(), memberList, true);
        membersDisplay.setAdapter(adapter);

        CirclePersonnelViewModel viewModel = ViewModelProviders.of(this).get(CirclePersonnelViewModel.class);
        liveData = viewModel.getDataSnapsCirclePersonelLiveData(circle.getId(), "members");

        liveData.observe(getViewLifecycleOwner(), returnArray -> {
            Subscriber subscriber = new Gson().fromJson(returnArray[0], Subscriber.class);
            String modifierType = returnArray[1];
            switch (modifierType) {
                case "added":
                    memberList.add(subscriber);
                    adapter.notifyDataSetChanged();
                    HelperMethodsUI.setListViewHeightBasedOnChildren(membersDisplay);
                    break;
                case "changed":
                    int index = HelperMethodsUI.returnIndexOfMemberList(memberList, subscriber);
                    memberList.remove(index);
                    memberList.add(index, subscriber);
                    adapter.notifyDataSetChanged();
                    break;
                case "removed":
                    memberList.remove(subscriber);
                    adapter.notifyDataSetChanged();
                    break;
            }
            Log.d("MemberListView",subscriber.getName());

        });
    }

    @Override
    public void onPause() {
        super.onPause();
        liveData.removeObservers(this);
    }
}
