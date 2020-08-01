package circleapp.circlepackage.circle.Utils;

import android.graphics.pdf.PdfDocument;
import android.view.View;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;

public class ExportPollResultAsDoc {
    HashMap<Subscriber, String > list;
    public ExportPollResultAsDoc(HashMap<Subscriber, String > list){ this.list = list;}

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
}
