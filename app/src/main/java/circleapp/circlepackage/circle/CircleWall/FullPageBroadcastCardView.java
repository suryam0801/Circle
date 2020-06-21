package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.R;

public class FullPageBroadcastCardView extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference broadcastCommentsDB, circlesDB, broadcastDB, userDB;
    private Circle circle;
    private List<Broadcast> broadcastList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_page_broadcast_card_view);

        broadcastList = SessionStorage.getBroadcastList(this);
        circle = SessionStorage.getCircle(this);

        RecyclerView recyclerView = findViewById(R.id.full_page_broadcast_card_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        final RecyclerView.Adapter adapter = new FullPageBroadcastCardAdapter(this, broadcastList, circle);
        recyclerView.setAdapter(adapter);
    }
}
