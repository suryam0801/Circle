package circleapp.circlepackage.circle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import circleapp.circlepackage.circle.Login.PhoneLogin;
import circleapp.circlepackage.circle.MainDisplay.Explore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth currentUser;
    private FirebaseFirestore db;
    public static final String TAG = MainActivity.class.getSimpleName();
    private String userDoc,userId;
    private boolean currentUserstate,userDocstate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //To set the Fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);
        getSupportActionBar().hide();

        currentUser=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        currentUserstate = currentUser.getCurrentUser()!=null;


         DocumentReference docRef = db.collection("Users").document(currentUser.getUid());
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            startActivity(new Intent(MainActivity.this, PhoneLogin.class));
                        } else {
                            Log.d(TAG, currentUser.getCurrentUser().getUid());
                            startActivity(new Intent(MainActivity.this, Explore.class));
                            finish();
                        }

                    }
                });

    }
    @Override
    protected void onStart() {
        super.onStart();

        DocumentReference docRef = db.collection("Users").document(currentUser.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    startActivity(new Intent(MainActivity.this, PhoneLogin.class));
                } else {
                    Log.d(TAG, currentUser.getCurrentUser().getUid());
                    startActivity(new Intent(MainActivity.this, Explore.class));
                    finish();
                }

            }
        });

    }
}