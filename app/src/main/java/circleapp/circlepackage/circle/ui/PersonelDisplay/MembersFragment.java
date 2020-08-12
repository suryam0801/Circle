package circleapp.circlepackage.circle.ui.PersonelDisplay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;

public class MembersFragment extends Fragment {

    private ListView membersDisplay;
    private List<Subscriber> memberList;
    private Circle circle;
    private LiveData<String[]> liveData;
    private GlobalVariables globalVariables = new GlobalVariables();

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
        final MemberListAdapter adapter = new MemberListAdapter(getContext(), memberList);
        membersDisplay.setAdapter(adapter);

        CirclePersonnelViewModel viewModel = ViewModelProviders.of(this).get(CirclePersonnelViewModel.class);

        liveData = viewModel.getDataSnapsCirclePersonelLiveData(circle.getId(), "members");

        liveData.observe(this, returnArray -> {
            Subscriber subscriber = new Gson().fromJson(returnArray[0], Subscriber.class);
            memberList.add(subscriber);
            adapter.notifyDataSetChanged();
            HelperMethodsUI.setListViewHeightBasedOnChildren(membersDisplay);

        });
    }
}
