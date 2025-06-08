package vnedraid.analyze.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vnedraid.analyze.dto.FilterDTO;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FilterController {


    @PostMapping("/filter")
    public ResponseEntity<String> filter(@RequestBody FilterDTO request) {
        System.out.println("Получен фильтр-запрос:");
        System.out.println("Должность: " + request.getJobTitle());
        System.out.println("Город: " + request.getCity());
        System.out.println("Опыт: " + request.getExperience());
        System.out.println("Возраст: " + request.getAge());
        System.out.println("Источник: " + request.getSource());
        System.out.println("Образование: " + request.getEducation());
        System.out.println("Формат работы: " + request.getWorkFormat());
        System.out.println("Машина: " + request.isCar());
        System.out.println("Права: " + request.getLicense());

        // В будущем можно добавить аналитику, фильтрацию и т.д.

        return ResponseEntity.ok("Фильтр успешно обработан");
    }
}