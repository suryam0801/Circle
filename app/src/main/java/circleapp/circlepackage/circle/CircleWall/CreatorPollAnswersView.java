package circleapp.circlepackage.circle.CircleWall;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageButton;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.LocalObjectModels.Poll;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;

public class CreatorPollAnswersView extends AppCompatActivity {

    private HashMap<Subscriber, String> list = new HashMap<>();
    private ImageButton bckBtn;
    private GlobalVariables globalVariables = new GlobalVariables();
    private PieChart pieChart;
    private PieDataSet pieDataSet;
    private PieData pieData;
    private Poll poll;
    private HashMap<String, String> userResponse;
    private RecyclerView pollResponsesRecyclerView;
    private LiveData<String[]> liveData;
    private CirclePersonnelViewModel circlePersonnelViewModel;
    private Circle circle;
    private Broadcast broadcast;
    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_poll_answers_view);

        setUIElements();
        calculatePieChartValues();
        setResponsesObserver();
        //init adapter
        pollResponsesRecyclerView.setLayoutManager(new LinearLayoutManager(CreatorPollAnswersView.this));
        adapter = new PollAnswerDisplayAdapter(CreatorPollAnswersView.this, list);
        pollResponsesRecyclerView.setAdapter(adapter);

        bckBtn.setOnClickListener(v -> {
            onBackPressed();
        });
    }
    private void setUIElements(){

        bckBtn = findViewById(R.id.bck_pollresults);
        pieChart = (PieChart) findViewById(R.id.barchart);
        pollResponsesRecyclerView = findViewById(R.id.poll_answers_recycler_view);

        circle = globalVariables.getCurrentCircle();
        broadcast = globalVariables.getCurrentBroadcast();

        poll = broadcast.getPoll();
        if (poll.getUserResponse() != null)
            userResponse = poll.getUserResponse();
        else
            userResponse = new HashMap<>();
    }
    private void setResponsesObserver(){
        circlePersonnelViewModel = ViewModelProviders.of(this).get(CirclePersonnelViewModel.class);
        liveData = circlePersonnelViewModel.getDataSnapsCirclePersonelLiveData(circle.getId(), "members");
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
    }
    private void calculatePieChartValues(){
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

        pieDataSet = new PieDataSet(pollValues, "Poll Results");
        pieDataSet.setValueTextSize(15f);
        pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        pieChart.animateXY(1400, 1400);
        pieChart.setDrawEntryLabels(false);
    }
    @Override
    protected void onPause() {
        super.onPause();
        liveData.removeObservers(this);
    }
}
