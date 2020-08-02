package circleapp.circlepackage.circle.ui.CircleWall;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageButton;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circlepackage.circle.Utils.ExportPollResultAsDoc;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Poll;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class CreatorPollAnswersView extends AppCompatActivity {

    private HashMap<Subscriber, String> list = new HashMap<>();
    private Button saveChartButton, saveResponsesButton;
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
    private ExportPollResultAsDoc exportPollResultAsDoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_poll_answers_view);

        setUIElements();
        try {
            calculatePieChartValues();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        saveChartButton = findViewById(R.id.exportChart);
        saveResponsesButton = findViewById(R.id.exportAsExcel);
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
                exportPollResultAsDoc = new ExportPollResultAsDoc(list);
            }
        });
    }
    private void calculatePieChartValues() throws IOException {
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
        saveChartButton.setOnClickListener(v->{
            Permissions.check(this/*context*/, WRITE_EXTERNAL_STORAGE, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    initFilePathAndSave("pdf");
                }
                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    // permission denied, block the feature.
                }
            });
        });
        saveResponsesButton.setOnClickListener(v->{
            Permissions.check(this/*context*/, WRITE_EXTERNAL_STORAGE, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    initFilePathAndSave("excel");
                }
                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    // permission denied, block the feature.
                }
            });
        });
    }

    private void initFilePathAndSave(String pdfOrExcel){
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);
        if(pdfOrExcel.equals("pdf")){
            File pdfFile = new File(path+"/"+"Poll Results "+circle.getName()+".pdf");
            try {
                exportPollResultAsDoc.printView2PDF(pieChart,pdfFile);
                shareFile(pdfFile,"pdf");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            File file = new File(path, "/" + "Poll Results "+circle.getName()+".xls");
            exportPollResultAsDoc.writeDataToExcelFile(file);
            shareFile(file,"excel");
        }
    }

    private void shareFile(File myFilePath, String pdfOrExcel){
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        if(myFilePath.exists()) {
            if(pdfOrExcel.equals("pdf"))
                intentShareFile.setType("application/pdf");
            else
                intentShareFile.setType("application/xls");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(myFilePath));

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    "Circle Poll Results");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Here are the Circle Poll Results:");

            startActivity(Intent.createChooser(intentShareFile, "Circle Poll Results"));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        liveData.removeObservers(this);
    }
}
