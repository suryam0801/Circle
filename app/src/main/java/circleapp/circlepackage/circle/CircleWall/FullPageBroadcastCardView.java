package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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

    private Circle circle;
    private List<Broadcast> broadcastList;
    int initialBroadcastPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_page_broadcast_card_view);

        RecyclerView recyclerView = findViewById(R.id.full_page_broadcast_card_recycler_view);
        LinearLayout leftNavLayout = findViewById(R.id.full_page_broadcast_left_nav);
        LinearLayout rightNavLayout = findViewById(R.id.full_page_broadcast_right_nav);
        ImageButton leftNavButton = findViewById(R.id.full_page_broadcast_right_nav_button);
        ImageButton rightNavButton = findViewById(R.id.full_page_broadcast_right_nav_button);

        broadcastList = SessionStorage.getBroadcastList(this);
        circle = SessionStorage.getCircle(this);
        initialBroadcastPosition = getIntent().getIntExtra("position", 0);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        final RecyclerView.Adapter adapter = new FullPageBroadcastCardAdapter(this, broadcastList, circle);
        recyclerView.setAdapter(adapter);

        recyclerView.scrollToPosition(initialBroadcastPosition);

        leftNavLayout.setOnClickListener(view ->{
            --initialBroadcastPosition;
            recyclerView.scrollToPosition(initialBroadcastPosition);
            recyclerView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_right_animation));
            recyclerView.setFocusable(true);
        });
        leftNavButton.setOnClickListener(view ->{
            --initialBroadcastPosition;
            recyclerView.scrollToPosition(initialBroadcastPosition);
            recyclerView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_right_animation));
            recyclerView.setFocusable(true);
        });
        rightNavLayout.setOnClickListener(view ->{
            ++initialBroadcastPosition;
            recyclerView.scrollToPosition(initialBroadcastPosition);
            recyclerView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_left_animation));
            recyclerView.setFocusable(true);
        });

        rightNavButton.setOnClickListener(view ->{
            ++initialBroadcastPosition;
            recyclerView.scrollToPosition(initialBroadcastPosition);
            recyclerView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_left_animation));
            recyclerView.setFocusable(true);
        });
    }
}
