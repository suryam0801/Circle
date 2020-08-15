package circleapp.circleapppackage.circle.DataLayer;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.Map;

import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.UserViewModel;

public class CircleRepository {
    private GlobalVariables globalVariables = new GlobalVariables();

    public String getCircleId() {
        String circleId = globalVariables.getFBDatabase().getReference("/Circles").push().getKey();
        return circleId;
    }

    public void createUserMadeCircle(Circle circle, Subscriber subscriber) {
        StorageReferenceRepository storageReferenceRepository = new StorageReferenceRepository();
        globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).setValue(circle);
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circle.getId()).child("members").child(subscriber.getId()).setValue(subscriber);
        if(!circle.getBackgroundImageLink().equals("default"))
            storageReferenceRepository.addCircleImageReference(circle.getId(),circle.getBackgroundImageLink());
    }

    public void deleteCircle(Circle circle, User user) {
        //reducing created circle count
        int currentCreatedCount = 0;
        if (user.getCreatedCircles() > 0)
            currentCreatedCount = user.getCreatedCircles() - 1;
        HashMap<String, Boolean> circleList = user.getActiveCircles();
        if(circleList!=null){
            if(circleList.containsKey(circle.getId()))
                circleList.remove(circle.getId());
        }
        user.setCreatedCircles(currentCreatedCount);
        user.setActiveCircles(circleList);
        globalVariables.saveCurrentUser(user);

        String key;
        for(Map.Entry<String, String > entry : circle.getMembersList().entrySet()) {
            key = entry.getKey();
            globalVariables.getFBDatabase().getReference("/Users").child(key).child("activeCircles").child(circle.getId()).removeValue();
        }

        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circle.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Users").child(user.getUserId()).child("createdCircles").setValue(currentCreatedCount);
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circle.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circle.getId()).removeValue();
        StorageReferenceRepository storageReferenceRepository = new StorageReferenceRepository();
        storageReferenceRepository.removeCircleImageReference(circle.getId(), circle.getBackgroundImageLink());
    }

    public void exitCircle(Circle circle, User user) {
        //reducing active circle count
        HashMap<String, Boolean> circleList = user.getActiveCircles();
        if(circleList!=null)
        circleList.remove(circle.getId());

        user.setActiveCircles(circleList);
        globalVariables.saveCurrentUser(user);

        globalVariables.getFBDatabase().getReference("/Users").child(user.getUserId()).child("activeCircles").child(circle.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circle.getId()).child("members").child(user.getUserId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).child("membersList").child(user.getUserId()).removeValue();
    }

    public void acceptApplicant(String circleId, Subscriber selectedApplicant, Context context, String role) {
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circleId).child("applicants").child(selectedApplicant.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("applicantsList").child(selectedApplicant.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circleId).child("members").child(selectedApplicant.getId()).setValue(selectedApplicant);
        globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("membersList").child(selectedApplicant.getId()).setValue(role);
        UserViewModel tempViewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsUserValueCirlceLiveData(selectedApplicant.getId());
        tempLiveData.observe((LifecycleOwner) context, dataSnapshot -> {
            User tempUser = dataSnapshot.getValue(User.class);
            if(tempUser!=null){
                tempLiveData.removeObservers((LifecycleOwner) context);
                HashMap<String, Boolean> activeCircles;
                if(tempUser.getActiveCircles()==null)
                    activeCircles = new HashMap<>();
                else
                    activeCircles = tempUser.getActiveCircles();
                activeCircles.put(circleId, true);
                globalVariables.getFBDatabase().getReference("/Users").child(selectedApplicant.getId()).child("activeCircles").setValue(activeCircles);
            }
        });
    }

    public void rejectApplicant(String circleId, Subscriber selectedApplicant) {
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circleId).child("applicants").child(selectedApplicant.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("applicantsList").child(selectedApplicant.getId()).removeValue();
    }

    public String createDefaultCircle(String name, String description, String acceptanceType, String creatorName, String district, int noOfBroadcasts, String category) {
        String id = HelperMethodsUI.uuidGet();
        Circle circle = new Circle(id, name, description, acceptanceType, "Everybody", "CreatorAdmin", creatorName, category, "default", null, null, null, district, null, System.currentTimeMillis(), noOfBroadcasts, true, 1);
        globalVariables.getFBDatabase().getReference("/Circles").child(id).setValue(circle);
        return id;
    }

    public void updateNoOfCommentsInBroadcast(Circle circle, Broadcast broadcast){
        FBTransactions fbTransactions = new FBTransactions(globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).child("noOfCommentsPerBroadcast").child(broadcast.getId()));
        fbTransactions.runTransactionOnIncrementalValues(1);
    }

    public void addUsersToCircle(Circle c, String role){
        if(globalVariables.getUsersList()!=null){

            for(String userId: globalVariables.getUsersList()){
                globalVariables.getFBDatabase().getReference("/Users").child(userId).child("activeCircles").child(c.getId()).setValue(true);
                globalVariables.getFBDatabase().getReference("/Circles").child(c.getId()).child("membersList").child(userId).setValue(role);
            }
        }
        globalVariables.setUsersList(null);
    }
}
