package vnedraid.exportFiles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import vnedraid.Data.JobData;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class JsonExportToExcel {

    public static void main(String[] args) {
        try {
            String jsonInput = "[{\"jobTitle\": \"Доставщик\", \"city\": \"Москва\", \"experience\": [\"from1To3\", \"from3To6\"], \"age\": [\"age18_30\"], \"source\": [\"hh\"], \"education\": \"higher\", \"workFormat\": [\"remotely\", \"onSite\"], \"car\": true, \"license\": [\"B\", \"C\"]}]";

            ObjectMapper objectMapper = new ObjectMapper();
            List<JobData> jobList = objectMapper.readValue(jsonInput, new TypeReference<List<JobData>>() {});

            exportToExcel(jobList, "jobs.xlsx");
            System.out.println("Excel файл успешно создан.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportToExcel(List<JobData> jobs, String fileName) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Вакансии");

        // Заголовки
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Должность", "Город", "Опыт", "Возраст", "Источник",
                "Образование", "Формат работы", "Наличие авто", "Права"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (JobData job : jobs) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(job.getJobTitle());
            row.createCell(1).setCellValue(job.getCity());
            row.createCell(2).setCellValue(String.join(";", job.getExperience()));
            row.createCell(3).setCellValue(String.join(";", job.getAge()));
            row.createCell(4).setCellValue(String.join(";", job.getSource()));
            row.createCell(5).setCellValue(job.getEducation());
            row.createCell(6).setCellValue(String.join(";", job.getWorkFormat()));
            row.createCell(7).setCellValue(job.isCar() ? "Да" : "Нет");
            row.createCell(8).setCellValue(String.join(";", job.getLicense()));
        }

        // Автоширина колонок
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            workbook.write(fos);
        }

        workbook.close();
    }
}