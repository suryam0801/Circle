package circleapp.circlepackage.circle.MainDisplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;

public class Explore extends AppCompatActivity {

    private String TAG = Explore.class.getSimpleName();
    private List<Circle> circleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //persistence automatically handles offline behavior
        database.setPersistenceEnabled(true);

        final DatabaseReference circles = database.getReference("Circles");
        //synchronizes and stores local copy of data
        circles.keepSynced(true);

        //Creating a testUser
        final List<String> locationTags = new ArrayList<>();
        locationTags.add("psg");
        final List<String> interestTags = new ArrayList<>();
        interestTags.add("juggling");
        final User user = new User("Surya", "Manivannan", "+17530043008", "default",
                locationTags, interestTags, "UUID", 0,0,0,"TOKEN_ID");


        //singe value listener for Circles Collection
        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call
        circles.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //filter through each Circle in the Circles database
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    //casts the datasnapshot to Circle Object
                    Circle circle = postSnapshot.getValue(Circle.class);

                    //filter for only circles associated with matching user location and interests
                    for(String locIterator : user.getLocationTags()){
                        if(circle.getLocationTags().contains(locIterator)){
                            for (String intIterator : user.getInterestTags()){
                                if(circle.getInterestTags().contains(intIterator))
                                    circleList.add(circle);
                            }
                        }
                    }
                }

                Log.d(TAG, String.valueOf(circleList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
