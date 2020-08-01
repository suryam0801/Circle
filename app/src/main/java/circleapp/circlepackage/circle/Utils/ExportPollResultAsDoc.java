package circleapp.circlepackage.circle.Utils;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Poll;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;

public class ExportPollResultAsDoc {
    HashMap<Subscriber, String > list;
    public ExportPollResultAsDoc(HashMap<Subscriber, String > list){ this.list = list;}
    public ExportPollResultAsDoc(){}

    public String[][] getPollResponsesForExporting() {
        String [][] excelData = new String [list.size()+1][2];
        excelData[0][0]="Participants";
        excelData [0][1]="Answer";
        int i = 1;
        for(Map.Entry<Subscriber, String> entry : list.entrySet()) {
            Subscriber key = entry.getKey();
            String  value = entry.getValue();
            excelData[i][0]=key.getName();
            excelData[i][1]=value;
            i++;
        }
        return excelData;
    }

    public String[][] getAllPollResponsesForExporting(int noOfRows, int noOfColumns, List<Broadcast> pollBroadcasts, List<String> allCircleMembers){
        String [][] excelData = new String [noOfRows][noOfColumns];
        int i=0, maxLen=0;
        for(Broadcast broadcast: pollBroadcasts){
            excelData[i][0] = broadcast.getTitle();
            List<String > unAnsweredMembers = allCircleMembers;
            HashMap<String, String> pollResponse = broadcast.getPoll().getUserResponse();
            Set<String> answers = Collections.<String > emptySet();
            int j = 0;
            for(Map.Entry<String, String> entry : pollResponse.entrySet()){
                String  userId = entry.getKey();
                String  answer = entry.getValue();
                unAnsweredMembers.remove(userId);
                answers.add(answer);
            }
            for (String answer : answers){
                excelData[i][j] = answer;
            }
            for(Map.Entry<String, String> entry : pollResponse.entrySet()){
                String  userId = entry.getKey();
                String  answer = entry.getValue();
                for(int p = 0; p<j; p++){
                    if(excelData[i+1][p].equals(answer)){
                        int k = i+1;
                        do{
                            k++;
                            if(excelData[k][p]==null)
                                excelData[k][p]=answer;
                        }while (excelData[k][p]!=null);
                    }
                }
            }
        }
        return excelData;
    }

    public void printView2PDF(View content, File file) throws IOException {
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

    public void writeDataToExcelFile(File fileName) {

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
            out.close();
        }catch(Exception e){ e.printStackTrace();}

    }

    public void writeAllPollsToExcelFile(File fileName, List<Broadcast> pollBroadcasts, List<String> allCircleMembers){
        String [][]excelData = getAllPollResponsesForExporting(100,100, pollBroadcasts, allCircleMembers);

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
            out.close();
        }catch(Exception e){ e.printStackTrace();}
    }

}
