package circleapp.circlepackage.circle.Helpers;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;

public class ExploreFButils extends ViewModel {
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static FirebaseAuth currentUser = FirebaseAuth.getInstance();
    static DatabaseReference circlesDB = database.getReference("Circles");

    FirebaseQueryLiveData circleliveData = new FirebaseQueryLiveData(circlesDB);

    private final LiveData<Circle> LiveData =
            Transformations.map(circleliveData, new Deserializer());

    private class Deserializer implements Function<DataSnapshot, Circle> {
        @Override
        public Circle apply(DataSnapshot dataSnapshot) {
            Log.d("Dserialize",dataSnapshot.getValue(Circle.class).toString());
            return dataSnapshot.getValue(Circle.class);
        }
    }
    @NonNull
    public LiveData<Circle> getCircleLiveData() {
        return LiveData;
    }
    public static void ExploreSetTabs(FragmentActivity activity, User user, List<Circle> exploreCircleList, RecyclerView.Adapter adapter, List<String> listOfFilters, RecyclerView exploreRecyclerView)
    {
        circlesDB.keepSynced(true);
        int index = SessionStorage.getTempIndexStore(activity);
        ExploreFButils viewModel = ViewModelProviders.of(activity).get(ExploreFButils.class);
        LiveData<Circle> exploreliveData = viewModel.getCircleLiveData();

        exploreliveData.observe(activity, new Observer<Circle>() {
            @Override
            public void onChanged(Circle circle) {
                Log.d("ExploreFButils",circle.toString());

                if(circle!=null){
//                        Log.d("ExploreFButils",snapshot.toString());
//                        Circle circle = snapshot.getValue(Circle.class);
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

                    int position = HelperMethods.returnIndexOfCircleList(exploreCircleList, circle);
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

                    if (position != -1) {
                        exploreCircleList.remove(position);
                        adapter.notifyItemRemoved(position);
                    }

                }
            }
        });




        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call
     /*   circlesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);

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

      */
    }
}
