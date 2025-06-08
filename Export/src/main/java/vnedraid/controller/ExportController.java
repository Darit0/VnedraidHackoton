package vnedraid.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import vnedraid.exportFiles.JsonExportToCSV;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/vacancies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExportController {

    private final JsonExportToCSV jsonExportToCSV;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/export")
    public ResponseEntity<String> exportVacancies(
            @RequestParam String jobTitle,
            @RequestParam String city) {

        try {
            // Формируем URL с закодированными параметрами
            String encodedJobTitle = URLEncoder.encode(jobTitle, StandardCharsets.UTF_8);
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);

            String filterUrl = "http://localhost:8081/api/v1/vacancies/filter?jobTitle=" +
                    encodedJobTitle + "&city=" + encodedCity;

            // Отправляем GET-запрос и получаем JSON-ответ
            ResponseEntity<String> response = restTemplate.getForEntity(filterUrl, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Ошибка при получении данных: " + response.getStatusCode());
            }

            String jsonResponse = response.getBody();

            // Экспортируем JSON в CSV
            StringWriter stringWriter = new StringWriter();
            jsonExportToCSV.exportCsvFromString(jsonResponse, stringWriter);

            // Подготавливаем заголовки для скачивания CSV
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment")
                            .filename("jobs.csv")
                            .build()
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(stringWriter.toString());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при экспорте в CSV: " + e.getMessage());
        }
    }
}
