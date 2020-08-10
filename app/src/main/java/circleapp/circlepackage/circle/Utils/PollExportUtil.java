package circleapp.circlepackage.circle.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Table;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.R;

public class PollExportUtil {
    private HashMap<Subscriber, String > list;
    private int rowLength, colLength;
    public PollExportUtil(HashMap<Subscriber, String > list){ this.list = list; this.rowLength = 0;}
    public PollExportUtil(){ this.rowLength = 0; this.colLength=0;}

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
        HashMap<String, Integer> pollOptions = new HashMap<>();
        int i=0, maxLen=0;
        for(Broadcast broadcast: pollBroadcasts){
            //Poll question
            excelData[i][0] = "Poll Question:";
            excelData[i][1] = broadcast.getPoll().getQuestion();
            //Clearing past broadcast data
            pollOptions.clear();//Clear poll options
            unAnsweredMembers.removeAll(allCircleMembers);
            unAnsweredMembers.addAll(allCircleMembers);
            if(pollResponse!=null)
                pollResponse.clear();
            pollResponse = broadcast.getPoll().getUserResponse();
            //record poll responses
            excelData[++i][0]="Poll Responses:";
            pollOptions=broadcast.getPoll().getOptions();
            int pollOptionsLength = 0;
            for (Map.Entry<String, Integer> entry : pollOptions.entrySet()){
                String  option = entry.getKey();
                excelData[i+1][pollOptionsLength]=option;
                pollOptionsLength++;
                if(colLength<pollOptionsLength+1)
                    colLength=pollOptionsLength+1;
            }
            i++;//Go to line with answers strings
            int pollQuestionLevel = i;
            //If nobody answered the poll
            if(pollResponse==null){

                excelData[i][pollOptionsLength]="Un-Answered Members";
                int x=0;
                int l;
                for (l = i+1; l<i+unAnsweredMembers.size()+1;l++){
                    excelData[l][pollOptionsLength]= listOfMembers.get(unAnsweredMembers.get(x)).getName();
                    x++;
                }
                maxLen = l;
                i=maxLen+2;
                rowLength=i;
                continue;
            }
            //Remove members from unanswered list
            for(Map.Entry<String, String> entry : pollResponse.entrySet()){
                String  userId = entry.getKey();
                unAnsweredMembers.remove(userId);
            }
            //Adding name of user who responded under each poll
            for(Map.Entry<String, String> entry : pollResponse.entrySet()){
                String  userId = entry.getKey();
                String  answer = entry.getValue();
                int k;
                int answerSize = pollOptionsLength;
                for (int p=0;p<answerSize;p++){
                    if(excelData[i][p].equals(answer)){
                        k = i;
                        while (excelData[k][p]!=null){
                            k++;
                        }
                        if(maxLen<k)
                            maxLen=k;
                        excelData[k][p]=listOfMembers.get(userId).getName();
                    }
                }
            }
            //Unanswered members in the poll
            excelData[pollQuestionLevel][pollOptionsLength]="Un-Answered Members";
            int x=0;
            int l;
            for (l = pollQuestionLevel+1; l<pollQuestionLevel+1+unAnsweredMembers.size();l++){
                excelData[l][pollOptionsLength]= listOfMembers.get(unAnsweredMembers.get(x)).getName();
                x++;
            }
            if(maxLen<l)
                maxLen=l;
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

    public void writeAllPollsToExcelFile(Context context, File fileName, List<Broadcast> pollBroadcasts, List<String> allCircleMembers, HashMap<String, Subscriber> listOfMembers, String circleName){
        String [][]excelData = getAllPollResponsesForExporting(10000,10, pollBroadcasts, allCircleMembers, listOfMembers);
        for(int rowNum=0; rowNum<rowLength; rowNum++){
            for(int colNum=0; colNum<colLength; colNum++){
                if(excelData[rowNum][colNum]==null)
                    excelData[rowNum][colNum] = " ";
            }
        }

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
            excelToPdf(context, fileName, circleName);
            out.close();
        }catch(Exception e){ e.printStackTrace();}
    }

    private void excelToPdf(Context context, File inputFile, String circleName) throws DocumentException, IOException {
        //First we read the Excel file in binary format into FileInputStream
        FileInputStream input_document = new FileInputStream(inputFile);

        // Read workbook into HSSFWorkbook
        HSSFWorkbook my_xls_workbook = new HSSFWorkbook(input_document);

        // Read worksheet into HSSFSheet
        HSSFSheet my_worksheet = my_xls_workbook.getSheetAt(0);

        // To iterate over the rows
        Iterator<Row> rowIterator = my_worksheet.iterator();

        //We will create output PDF document objects at this point
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();

        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, "/" + "All Poll Results "+circleName+".pdf");

        PdfWriter.getInstance(document, new FileOutputStream(file));

        document.open();

        //we have 5 columns in the Excel sheet, so we create a PDF table with 5 columns; Note: There are ways to make this dynamic in nature, if you want to.
        PdfPTable my_table = new PdfPTable(colLength);

        //We will use the object below to dynamically add new data to the table
        PdfPCell table_cell;

        //Loop through rows.
        while((rowIterator).hasNext())
        {
            Row rowi = rowIterator.next();

            Iterator<Cell> cellIterator = rowi.cellIterator();

            while(cellIterator.hasNext())
            {
                Cell celli = cellIterator.next(); //Fetch CELL

                switch(celli.getCellType())
                {
                    //Identify CELL type you need to add more code here based on your requirement / transformations
                    case Cell.CELL_TYPE_STRING:

                        //Push the data from Excel to PDF Cell
                        table_cell = new PdfPCell(new Phrase(celli.getStringCellValue()));

                        //move the code below to suit to your needs
                        my_table.addCell(table_cell);

                        break;

                    case Cell.CELL_TYPE_NUMERIC:

                        //Push the data from Excel to PDF Cell
                        table_cell = new PdfPCell(new Phrase("" + celli.getNumericCellValue()));

                        //move the code below to suit to your needs
                        my_table.addCell(table_cell);

                        break;
                }
                //next line
            }
        }
        //Pdf dependency
        /*try {
            Drawable d = context.getResources().getDrawable(R.drawable.circle_logo);
            BitmapDrawable bitDw = ((BitmapDrawable) d);
            Bitmap bmp = bitDw.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 5, stream);
            Image image = Image.getInstance(stream.toByteArray());
            document.add(image);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        Paragraph para = new Paragraph();
        para.add("Exported By Circle");
        document.add(para);
        //Finally add the table to PDF document
        document.add(my_table);
        document.close();

        //we created our pdf file..
        input_document.close(); //close xls
    }

}
