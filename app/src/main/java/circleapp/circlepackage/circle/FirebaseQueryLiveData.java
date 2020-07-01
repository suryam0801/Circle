package circleapp.circlepackage.circle;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import circleapp.circlepackage.circle.ObjectModels.Circle;

public class FirebaseQueryLiveData extends LiveData<DataSnapshot> {
    private static final String LOG_TAG = "FirebaseQueryLiveData";

    private final Query query;
    private final MyValueEventListener listener = new MyValueEventListener();
    private final MyChildListener childListener = new MyChildListener();

    public FirebaseQueryLiveData(Query query) {
        this.query = query;
    }

    public FirebaseQueryLiveData(DatabaseReference ref) {
        this.query = ref;
    }

    @Override
    protected void onActive() {
        Log.d(LOG_TAG, "onActive");
//        query.addValueEventListener(listener);
        Log.d(LOG_TAG, "childListener-onActive");
        query.addChildEventListener(childListener);
    }

    @Override
    protected void onInactive() {
        Log.d(LOG_TAG, "onInactive");
//        query.removeEventListener(listener);
        Log.d(LOG_TAG, "childListener-onInActive");
        query.removeEventListener(childListener);
    }

    private class MyChildListener implements ChildEventListener
    {

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            setValue(snapshot);

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            Circle circle = snapshot.getValue(Circle.class);
            Log.d(LOG_TAG, "Circle data-"+circle.toString());
            setValue(snapshot);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            setValue(snapshot);
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            setValue(snapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    private class MyValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                setValue(snapshot);
            }

    }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(LOG_TAG, "Can't listen to query " + query, databaseError.toException());
        }
    }
}