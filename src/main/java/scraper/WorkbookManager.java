package scraper;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class WorkbookManager {

    private Workbook generateWorkbook() {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Profiles Data");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Profile Name");
        headerRow.createCell(1).setCellValue("Profile About");
        headerRow.createCell(2).setCellValue("Profile Description");
        headerRow.createCell(3).setCellValue("Profile Experience");
        headerRow.createCell(4).setCellValue("Profile Education");
        headerRow.createCell(5).setCellValue("Profile Open To Work");
        headerRow.createCell(6).setCellValue("Profile Link");

        // Auto-size columns
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }

        // create unique time-stamp for each generated file when run.
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH-mm");
        String timestamp = sdf.format(new Date());

        // Write the workbook to a file
        try (FileOutputStream fileOut = new FileOutputStream(String.format("profiles_data_%s.xlsx", timestamp))) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close the workbook
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return workbook;
    }

    private void insertDataToWorkbook(Set<Profile> profiles, Workbook workbook) {
        Sheet sheet = workbook.getSheet("Profiles Data");

        int rowIndex = 1;
        profiles.forEach((profile) -> {
            Row dataRow = sheet.createRow(rowIndex);
            dataRow.createCell(0).setCellValue(profile.getName());
            dataRow.createCell(1).setCellValue(profile.getAbout());
            dataRow.createCell(2).setCellValue(profile.getDescription());
            dataRow.createCell(3).setCellValue(profile.getExperience());
            dataRow.createCell(4).setCellValue(profile.getEducation());
            dataRow.createCell(5).setCellValue(profile.isOpenToWork() ? "Yes" : "No");
            dataRow.createCell(6).setCellValue(profile.getLink());
        });
    }

    public void saveProfilesToWorkBook(Set<Profile> profiles) {
        insertDataToWorkbook(profiles, generateWorkbook());
    }
}
