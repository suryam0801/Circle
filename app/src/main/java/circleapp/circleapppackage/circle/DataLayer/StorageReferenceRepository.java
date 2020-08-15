package circleapp.circleapppackage.circle.DataLayer;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class StorageReferenceRepository {
    private GlobalVariables globalVariables = new GlobalVariables();

    public StorageReference getStorageReference(String dbReference) {
        return globalVariables.getFirebaseStorage().getReference().child(dbReference);
    }

    public void deleteStorageReference(String reference) {
        StorageReference temp = globalVariables.getFirebaseStorage().getReferenceFromUrl(reference);
        temp.delete();
    }

    public void addBroadcastImageReference(String circleId, String broadcastId, String imageUrl) {
        globalVariables.getFBDatabase().getReference("StorageReferences").child(circleId).child(broadcastId).setValue(imageUrl);
    }

    public void addCircleImageReference(String circleId, String imageUrl) {
        globalVariables.getFBDatabase().getReference("StorageReferences").child(circleId).child("CircleIcon").setValue(imageUrl);
    }

    public void removeCircleImageReference(String circleId, String imageUrl) {
        if(!imageUrl.equals("default"))
            deleteStorageReference(imageUrl);

        globalVariables.getFBDatabase().getReference("StorageReferences").child(circleId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> broadcastImageList;
                if(dataSnapshot.exists()){
                    broadcastImageList = (Set<String>) ((HashMap<String, String>) dataSnapshot.getValue());
                    for (String broadcastImage : broadcastImageList){
                        deleteStorageReference(broadcastImage);
                    }
                } else {
                    broadcastImageList = Collections.<String>emptySet();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        globalVariables.getFBDatabase().getReference("StorageReferences").child(circleId).removeValue();
    }

    public void removeBroadcastImageReference(String circleId, String broadcastId, String imageUrl) {
        deleteStorageReference(imageUrl);
        globalVariables.getFBDatabase().getReference("StorageReferences").child(circleId).child(broadcastId).removeValue();
    }


}
