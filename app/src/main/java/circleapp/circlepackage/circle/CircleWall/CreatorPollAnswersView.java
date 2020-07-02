package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseRetrievalViewModel;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;

public class CreatorPollAnswersView extends AppCompatActivity {

    private HashMap<Subscriber, String> list = new HashMap<>();
    private ImageButton bckBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_poll_answers_view);

        bckBtn = findViewById(R.id.bck_pollresults);
        PieChart pieChart = (PieChart) findViewById(R.id.barchart);
        RecyclerView recyclerView = findViewById(R.id.poll_answers_recycler_view);

        Circle circle = SessionStorage.getCircle(CreatorPollAnswersView.this);
        Broadcast broadcast = SessionStorage.getBroadcast(CreatorPollAnswersView.this);

        Poll poll = broadcast.getPoll();
        HashMap<String, String> userResponse;
        if (poll.getUserResponse() != null)
            userResponse = poll.getUserResponse();
        else
            userResponse = new HashMap<>();

        //calculating percentages
        int totalValue = 0;
        for (Map.Entry<String, Integer> entry : poll.getOptions().entrySet())
            totalValue += entry.getValue();

        if (totalValue == 0)
            totalValue = 1;
        pieChart.setUsePercentValues(true);
        pieChart.setDescription(null);
        List<PieEntry> pollValues = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : poll.getOptions().entrySet())
            pollValues.add(new PieEntry((entry.getValue() * 100) / totalValue, entry.getKey()));

        PieDataSet pieDataSet = new PieDataSet(pollValues, "Poll Results");
        pieDataSet.setValueTextSize(15f);
        PieData pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        pieChart.animateXY(1400, 1400);
        pieChart.setDrawEntryLabels(false);

        recyclerView.setLayoutManager(new LinearLayoutManager(CreatorPollAnswersView.this));
        final RecyclerView.Adapter adapter = new PollAnswerDisplayAdapter(CreatorPollAnswersView.this, list);
        recyclerView.setAdapter(adapter);

        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of(this).get(FirebaseRetrievalViewModel.class);

        LiveData<String[]> liveData = viewModel.getDataSnapsCirclePersonelLiveData(circle.getId(), "members");

        liveData.observe(this, returnArray -> {
            Subscriber member = new Gson().fromJson(returnArray[0], Subscriber.class);

            if (userResponse != null) {
                for (Map.Entry<String, String> entry : userResponse.entrySet()) {
                    if (entry.getKey().equals(member.getId())) {
                        list.put(member, entry.getValue());
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        bckBtn.setOnClickListener(v -> {
            onBackPressed();
        });
    }
}
