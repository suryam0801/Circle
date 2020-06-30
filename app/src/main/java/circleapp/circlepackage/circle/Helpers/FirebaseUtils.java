package circleapp.circlepackage.circle.Helpers;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import circleapp.circlepackage.circle.Explore.WorkbenchFragment;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.ObjectModels.NotifyUIObject;
import circleapp.circlepackage.circle.ObjectModels.User;

public class FirebaseUtils extends ViewModel {
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static FirebaseAuth currentUser = FirebaseAuth.getInstance();
    static DatabaseReference notifyDb = database.getReference("Notifications").child(currentUser.getCurrentUser().getUid());
    static DatabaseReference circlesDB = database.getReference("Circles");



    FirebaseQueryLiveData notifyliveData = new FirebaseQueryLiveData(notifyDb);
    FirebaseQueryLiveData circleliveData = new FirebaseQueryLiveData(circlesDB);

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return notifyliveData;
    }


    public static void FbsingleValueEvent(NotifyUIObject notifyUIObject) {
        FirebaseUtils viewModel = ViewModelProviders.of((FragmentActivity) notifyUIObject.getContext()).get(FirebaseUtils.class);
        LiveData<DataSnapshot> notifyliveData = viewModel.getDataSnapshotLiveData();

        notifyliveData.observe((LifecycleOwner) notifyUIObject.getContext(), new Observer<DataSnapshot>(){
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Notification notification = snapshot.getValue(Notification.class);
                        UiHelper.NotifyUIFragment(notifyUIObject.getContext(),notification,notifyUIObject.getPrevnotify(),notifyUIObject.getPreviousNotifs(),notifyUIObject.getThisWeekNotifs(),notifyUIObject.getAdapterPrevious(),notifyUIObject.getAdapterThisWeek(),notifyUIObject.getPreviousListView(),notifyUIObject.getThisWeekListView());
                    }
                }
            }
        });
    }

    public static void WorkbenchSetTabs(User user, RecyclerView wbrecyclerView, RecyclerView.Adapter wbadapter, List<Circle> workbenchCircleList, LinearLayout emptyDisplay)
    {

        circlesDB = database.getReference("Circles");
        circlesDB.keepSynced(true);
        circlesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                wbrecyclerView.setAdapter(wbadapter);
                Circle circle = dataSnapshot.getValue(Circle.class);

                boolean isMember = HelperMethods.isMemberOfCircle(circle, user.getUserId());

                if (circle.getCreatorID().equals(currentUser.getUid()) || isMember) {
                    workbenchCircleList.add(circle);
                    wbadapter.notifyDataSetChanged();
                    emptyDisplay.setVisibility(View.GONE);
                    WorkbenchFragment.initializeNewCount(circle);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                int position = HelperMethods.returnIndexOfCircleList(workbenchCircleList, circle);
                boolean containsCircle = HelperMethods.listContainsCircle(workbenchCircleList, circle);

                if (containsCircle) {
                    workbenchCircleList.set(position, circle);
                    wbadapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                int position = HelperMethods.returnIndexOfCircleList(workbenchCircleList, circle);
                workbenchCircleList.remove(position);
                wbadapter.notifyItemChanged(position);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void ExploreSetTabs(FragmentActivity activity, User user, List<Circle> exploreCircleList, RecyclerView.Adapter adapter, List<String> listOfFilters, RecyclerView exploreRecyclerView)
    {
        circlesDB = database.getReference("Circles");
        circlesDB.keepSynced(true);
        int index = SessionStorage.getTempIndexStore(activity);

        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call
        circlesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                Log.d("ExploreFButils",circle.toString());
                boolean isMember = HelperMethods.isMemberOfCircle(circle, user.getUserId());
                boolean isInLocation = circle.getCircleDistrict().trim().equalsIgnoreCase(user.getDistrict().trim());

                if (!isMember && circle.getVisibility().equals("Everybody")) {

                    if (circle.getCreatorName().equals("The Circle Team")) {
                        exploreCircleList.add(0, circle);
                        adapter.notifyItemInserted(0);
                    }

                    if (isInLocation) {
                        boolean circleMatchesFilter = HelperMethods.circleFitsWithinFilterContraints(listOfFilters, circle);
                        if (listOfFilters == null || listOfFilters.isEmpty()) {
                            exploreCircleList.add(adapter.getItemCount(), circle);
                            adapter.notifyItemInserted(adapter.getItemCount());
                        } else if (circleMatchesFilter) {
                            exploreCircleList.add(adapter.getItemCount(), circle);
                            adapter.notifyItemInserted(adapter.getItemCount());
                        }
                    }
                    exploreRecyclerView.scrollToPosition(index);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);

                int position = HelperMethods.returnIndexOfCircleList(exploreCircleList, circle);
                boolean isMember = HelperMethods.isMemberOfCircle(circle, user.getUserId());
                boolean containsCircle = HelperMethods.listContainsCircle(exploreCircleList, circle);

                if (containsCircle) {
                    if (isMember) {
                        exploreCircleList.remove(position);
                        adapter.notifyItemRemoved(position);
                    } else {
                        exploreCircleList.set(position, circle);
                        adapter.notifyItemChanged(position);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                int position = HelperMethods.returnIndexOfCircleList(exploreCircleList, circle);
                if (position != -1) {
                    exploreCircleList.remove(position);
                    adapter.notifyItemRemoved(position);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}