package be.uhasselt.dwi_application.utility.excel;

import be.uhasselt.dwi_application.model.basic.InstructionPerformanceData;
import be.uhasselt.dwi_application.model.basic.Time;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

public class ExcelWriter {
    private static final String FILE_NAME = "assembly_stats.xlsx";
    private static final ReentrantLock lock = new ReentrantLock();

    public static void append(InstructionPerformanceData data) {
        lock.lock();
        System.out.println("\u001B[32m" + "<Writing to Excel>" + "\u001B[0m");

        try {
            Workbook workbook;
            Sheet sheet;
            File file = new File(FILE_NAME);

            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0);
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Assembly Stats");

                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("ID");
                header.createCell(1).setCellValue("Assembly");
                header.createCell(2).setCellValue("Description");
                header.createCell(3).setCellValue("Type");
                header.createCell(4).setCellValue("Time (readable)");
                header.createCell(5).setCellValue("Time (s)");
            }

            int lastRow = sheet.getLastRowNum();
            Row dataRow = sheet.createRow(lastRow + 1);

            Time t = data.executionTime();
            Instruction i = data.instruction();

            System.out.println("Data: " + data);

            dataRow.createCell(0).setCellValue(data.id());
            dataRow.createCell(1).setCellValue(data.assembly().getName());
            dataRow.createCell(2).setCellValue(i.getDescription());
            dataRow.createCell(3).setCellValue(i.getType());
            dataRow.createCell(4).setCellValue(t.getReadable());
            dataRow.createCell(5).setCellValue(t.getSeconds());

            FileOutputStream fileOut = new FileOutputStream(FILE_NAME);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
