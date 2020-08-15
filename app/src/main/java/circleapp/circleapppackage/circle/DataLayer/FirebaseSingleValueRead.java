package circleapp.circleapppackage.circle.DataLayer;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseSingleValueRead extends LiveData<DataSnapshot> {
    private static final String LOG_TAG = "FirebaseQueryLiveData";

    private final Query query;
    private final MyChildValueListener listener = new MyChildValueListener();

    public FirebaseSingleValueRead(Query query) {
        this.query = query;
    }

    public FirebaseSingleValueRead(DatabaseReference ref) {
        this.query = ref;
    }

    @Override
    protected void onActive() {
        query.addValueEventListener(listener);
    }

    @Override
    protected void onInactive() {
        query.removeEventListener(listener);
    }

    private class MyChildValueListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            setValue(snapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }
}