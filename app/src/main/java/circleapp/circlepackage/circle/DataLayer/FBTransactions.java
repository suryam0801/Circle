package circleapp.circlepackage.circle.DataLayer;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class FBTransactions {
    private final DatabaseReference ref;

    public FBTransactions(DatabaseReference ref) {
        this.ref = ref;
    }

    public void runTransactionOnIncrementalValues(int counter){
        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                } else {
                    mutableData.setValue(currentValue + counter);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(
                    DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                System.out.println("Transaction completed");
            }
        });
    }
}
