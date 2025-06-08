package vnedraid.exportFiles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import vnedraid.Data.JobData;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class JsonExportToCSV {

    public void interpritatorCSV() {
        try {
            // Пример входного JSON
            String jsonInput = "[{\"jobTitle\": \"Доставщик\", \"city\": \"Москва\", \"experience\": [\"from1To3\", \"from3To6\"], \"age\": [\"age18_30\"], \"source\": [\"hh\"], \"education\": \"higher\", \"workFormat\": [\"remotely\", \"onSite\"], \"car\": true, \"license\": [\"B\", \"C\"]}]";

            ObjectMapper objectMapper = new ObjectMapper();
            List<JobData> jobList = objectMapper.readValue(jsonInput, new TypeReference<List<JobData>>() {});

            exportToCsv(jobList, "jobs.csv");
            System.out.println("CSV файл успешно создан.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportToCsv(List<JobData> jobs, String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
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
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}