package circleapp.circlepackage.circle.Utils;

import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;

public class ExportPollResultAsDoc {
    private HashMap<Subscriber, String > list;
    private int rowLength, colLength;
    public ExportPollResultAsDoc(HashMap<Subscriber, String > list){ this.list = list; this.rowLength = 0;}
    public ExportPollResultAsDoc(){ this.rowLength = 0; this.colLength=0;}

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
            rowLength = i;
        }
        return excelData;
    }

    public String[][] getAllPollResponsesForExporting(int noOfRows, int noOfColumns, List<Broadcast> pollBroadcasts, List<String> allCircleMembers, HashMap<String, Subscriber> listOfMembers){
        String [][] excelData = new String [noOfRows][noOfColumns];
        List<String> unAnsweredMembers = new ArrayList<>();
        Set<String> answers = new HashSet<String>();
        HashMap<String, String> pollResponse = new HashMap<>();
        int i=0, maxLen=0;
        for(Broadcast broadcast: pollBroadcasts){
            excelData[i][0] = broadcast.getPoll().getQuestion();
            unAnsweredMembers.removeAll(allCircleMembers);
            unAnsweredMembers.addAll(allCircleMembers);
            if(pollResponse!=null)
                pollResponse.clear();
            pollResponse = broadcast.getPoll().getUserResponse();
            Log.d("BroadcastQuestion",unAnsweredMembers.size()+"");
            if(pollResponse==null){

                excelData[maxLen+1][0]="Un-Answered Members";
                int x=0;
                int l = maxLen+2;
                for (; l<maxLen+2+unAnsweredMembers.size();l++){
                    excelData[l][0]= listOfMembers.get(unAnsweredMembers.get(x)).getName();
                    x++;
                }
                maxLen = l;
                i=maxLen+2;
                rowLength=i;
                continue;
            }

            int j = 0;
            if(answers!=null)
                answers.clear();
            answers.addAll(pollResponse.values());
            for(Map.Entry<String, String> entry : pollResponse.entrySet()){
                String  userId = entry.getKey();
                unAnsweredMembers.remove(userId);
            }
            for (String answer : answers){
                excelData[i+1][j] = answer;
                j++;
                if(colLength<=j)
                    colLength=j;
            }
            for(Map.Entry<String, String> entry : pollResponse.entrySet()){
                String  userId = entry.getKey();
                String  answer = entry.getValue();
                for(int p = 0; p<j; p++){
                    if(excelData[i+1][p].equals(answer)){
                        int k = i+2;
                        do{
                            if(excelData[k][p]==null){
                                String username= listOfMembers.get(userId).getName();
                                excelData[k][p]=username;
                            }
                            if(maxLen<=k)
                                maxLen=k;
                            k++;
                        }while (excelData[k][p]!=null);
                    }
                }
            }
            excelData[maxLen+1][0]="Un-Answered Members";
            int x=0;
            int l = maxLen+2;
            for (; l<maxLen+2+unAnsweredMembers.size();l++){
                excelData[l][0]= listOfMembers.get(unAnsweredMembers.get(x)).getName();
                x++;
            }
            maxLen = l;
            i=maxLen+2;
            rowLength = i;
        }
        return excelData;
    }

    public void writeDataToExcelFile(File fileName) {

        String [][] excelData = getPollResponsesForExporting();

        HSSFWorkbook myWorkBook = new HSSFWorkbook();
        HSSFSheet mySheet = myWorkBook.createSheet();
        HSSFRow myRow = null;
        HSSFCell myCell = null;

        for (int rowNum = 0; rowNum < rowLength; rowNum++){
            myRow = mySheet.createRow(rowNum);

            for (int cellNum = 0; cellNum < 2 ; cellNum++){
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

    public void writeAllPollsToExcelFile(File fileName, List<Broadcast> pollBroadcasts, List<String> allCircleMembers, HashMap<String, Subscriber> listOfMembers){
        String [][]excelData = getAllPollResponsesForExporting(10000,10, pollBroadcasts, allCircleMembers, listOfMembers);

        HSSFWorkbook myWorkBook = new HSSFWorkbook();
        HSSFSheet mySheet = myWorkBook.createSheet();
        HSSFRow myRow = null;
        HSSFCell myCell = null;

        for (int rowNum = 0; rowNum < rowLength; rowNum++){
            myRow = mySheet.createRow(rowNum);

            for (int cellNum = 0; cellNum < colLength ; cellNum++){
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
