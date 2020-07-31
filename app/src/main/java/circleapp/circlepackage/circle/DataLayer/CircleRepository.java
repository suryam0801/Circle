package circleapp.circlepackage.circle.DataLayer;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.Map;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.UserViewModel;

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

    public void updateCircleReadDiscussions(int counter, Circle circle) {
        FBTransactions fbTransactions = new FBTransactions(globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).child("noOfNewDiscussions"));
        fbTransactions.runTransactionOnIncrementalValues(counter);
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
        for(Map.Entry<String, Boolean> entry : circle.getMembersList().entrySet()) {
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

    public void acceptApplicant(String circleId, Subscriber selectedApplicant, Context context) {
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circleId).child("applicants").child(selectedApplicant.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("applicantsList").child(selectedApplicant.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circleId).child("members").child(selectedApplicant.getId()).setValue(selectedApplicant);
        globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("membersList").child(selectedApplicant.getId()).setValue(true);
        UserViewModel tempViewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsUserValueCirlceLiveData(selectedApplicant.getId());
        tempLiveData.observe((LifecycleOwner) context, dataSnapshot -> {
            User tempUser = dataSnapshot.getValue(User.class);
            if(tempUser!=null){

                HashMap<String, Boolean> activeCircles = tempUser.getActiveCircles();
                activeCircles.put(circleId, true);
                globalVariables.getFBDatabase().getReference("/Users").child(selectedApplicant.getId()).child("activeCircles").setValue(activeCircles);
            }
        });
    }

    public void rejectApplicant(String circleId, Subscriber selectedApplicant) {
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circleId).child("applicants").child(selectedApplicant.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("applicantsList").child(selectedApplicant.getId()).removeValue();
    }

    public String createDefaultCircle(String name, String description, String acceptanceType, String creatorName, String district, int noOfBroadcasts, int noOfDiscussions, String category) {
        String id = HelperMethodsUI.uuidGet();
        Circle circle = new Circle(id, name, description, acceptanceType, "Everybody", "CreatorAdmin", creatorName, category, "default", null, null, district, null, System.currentTimeMillis(), noOfBroadcasts, noOfDiscussions, true);
        globalVariables.getFBDatabase().getReference("/Circles").child(id).setValue(circle);
        return id;
    }
}
