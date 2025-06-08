package vnedraid.exportFiles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import vnedraid.Data.JobData;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    public static void exportToCsv(List<JobData> jobs, String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {

            // Добавляем BOM для корректного открытия в Excel
            writer.print('\uFEFF');

            // Заголовок CSV
            writer.println("jobTitle,city,experience,age,source,education,workFormat,car,license");

            for (JobData job : jobs) {
                writer.println(String.join(",",
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