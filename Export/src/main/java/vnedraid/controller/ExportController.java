package vnedraid.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import vnedraid.exportFiles.JsonExportToCSV;

import java.io.IOException;
import java.io.StringWriter;

@RestController
@RequestMapping("/api/v1/vacancies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExportController {

    private final JsonExportToCSV jsonExportToCSV;

    @GetMapping("/export")
    public ResponseEntity<String> exportVacancies(@RequestParam String vacancyJson) {
        try {
            // Создаем StringWriter для хранения CSV в памяти
            StringWriter stringWriter = new StringWriter();

            // Преобразуем JSON в CSV и записываем в StringWriter
            jsonExportToCSV.exportCsvFromString(vacancyJson, stringWriter);

            // Формируем заголовки для скачивания файла
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "csv"));
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment")
                            .filename("jobs.csv")
                            .build()
            );

            // Возвращаем CSV в теле ответа
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(stringWriter.toString());

        } catch (IOException e) {
            // Возвращаем ошибку в случае исключения
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при экспорте в CSV: " + e.getMessage());
        }
    }

}
