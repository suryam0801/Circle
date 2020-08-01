package circleapp.circlepackage.circle.ui.CircleWall;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Poll;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;

public class CreatorPollAnswersView extends AppCompatActivity {

    private HashMap<Subscriber, String> list = new HashMap<>();
    private Button exportButton;
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
        exportButton = findViewById(R.id.exportResults);
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
        exportButton.setOnClickListener(v->{
            //pieChart.saveToGallery("PollResults"+broadcast.getTitle(), 100);
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES);
            File file = new File(path, "/" + "PollResults.xlsx");
            String filepath = String.format("/mnt/sdcard/PollResults%s.pdf",broadcast.getTitle());
            writeDataToExcelFile(file);
            /*File file = new File(filepath);
            try {
                printView2PDF(pieChart,file);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        });
    }
    private void printView2PDF(View content, File file) throws IOException {
        // create a new document
        PdfDocument document = new PdfDocument();

        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                content.getMeasuredWidth(), content.getMeasuredHeight(),1)
                .create();

        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);

        // draw something on the page
        content.draw(page.getCanvas());

        // finish the page
        document.finishPage(page);
        document.writeTo(new FileOutputStream(file));
        // close the document
        document.close();
    }

    private void writeDataToExcelFile(File fileName) {

        String [][] excelData = getPollResponsesForExporting();

        HSSFWorkbook myWorkBook = new HSSFWorkbook();
        HSSFSheet mySheet = myWorkBook.createSheet();
        HSSFRow myRow = null;
        HSSFCell myCell = null;

        for (int rowNum = 0; rowNum < excelData[0].length; rowNum++){
            myRow = mySheet.createRow(rowNum);

            for (int cellNum = 0; cellNum < list.size()+1 ; cellNum++){
                myCell = myRow.createCell(cellNum);
                myCell.setCellValue(excelData[rowNum][cellNum]);
            }
        }
        try{
            FileOutputStream out = new FileOutputStream(fileName);
            myWorkBook.write(out);
            Toast.makeText(this,"See your files for the xl file", Toast.LENGTH_SHORT).show();
            out.close();
        }catch(Exception e){ e.printStackTrace();}

    }

    private String[][] getPollResponsesForExporting() {
        String [][] excelData = new String [list.size()+1][2];
        excelData[0][0]="Participants";
        excelData [0][1]="Answer";
        int i = 0;
        for(Map.Entry<Subscriber, String> entry : list.entrySet()) {
            Subscriber key = entry.getKey();
            String  value = entry.getValue();
            excelData[i][0]=key.getName();
            excelData[i][1]=value;
            i++;
        }
        return excelData;
    }

    @Override
    protected void onPause() {
        super.onPause();
        liveData.removeObservers(this);
    }
}
