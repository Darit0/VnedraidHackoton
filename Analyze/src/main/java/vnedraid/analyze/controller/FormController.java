package vnedraid.analyze.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vnedraid.analyze.dto.FormData;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // чтобы разрешить CORS с фронта
public class FormController {

    @PostMapping("/form")
    public ResponseEntity<Map<String, Boolean>> handleForm(@RequestBody FormData data) {
        System.out.println("Получены данные формы: " + data.getName() + ", " + data.getEmail());
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}
