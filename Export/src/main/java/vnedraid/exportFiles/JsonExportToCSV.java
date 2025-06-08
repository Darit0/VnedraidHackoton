package vnedraid.exportFiles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import vnedraid.Data.JobData;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
@Service
public class JsonExportToCSV {

    public void interpritatorCSV(String jsonInput) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<JobData> jobList = objectMapper.readValue(jsonInput, new TypeReference<List<JobData>>() {});

            exportToCsv(jobList, "jobs.csv");
            System.out.println("CSV файл успешно создан.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportCsvFromString(String jsonInput, java.io.Writer writer) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<JobData> jobList = objectMapper.readValue(jsonInput, new TypeReference<List<JobData>>() {});
        exportToCsv(jobList, writer);
    }

    // Перегруженный метод для записи в файл
    public void exportToCsv(List<JobData> jobs, String fileName) throws IOException {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(fileName), StandardCharsets.UTF_8)) {
            exportToCsv(jobs, writer); // вызываем существующий метод
        }
    }

    public static void exportToCsv(List<JobData> jobs, java.io.Writer writer) throws IOException {
        try (PrintWriter pw = new PrintWriter(writer)) {
            pw.print('\uFEFF'); // Добавляем BOM для корректного открытия в Excel
            //заголовки
            pw.println("jobTitle,city,experience,age,source,education,workFormat,car,license");

            for (JobData job : jobs) {
                pw.println(String.join(",",
                        quote(job.getJobTitle()),
                        quote(job.getCity()),
                        quote(String.join(";", job.getExperience())),
                        quote(String.join(";", job.getAge())),
                        quote(String.join(";", job.getSource())),
                        quote(job.getEducation()),
                        quote(String.join(";", job.getWorkFormat())),
                        String.valueOf(job.isCar()),
                        quote(String.join(";", job.getLicense()))
                ));
            }
        }
    }

    private static String quote(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}