package vnedraid.apiservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import vnedraid.apiservice.dto.VacancyDto;
import vnedraid.apiservice.mapper.VacancyMapper;
import vnedraid.apiservice.spec.VacancySpecifications;
import vnedraid.inputservice.models.Vacancy;
import vnedraid.inputservice.repo.VacancyRepo;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/vacancies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")          // при необходимости ограничьте
public class VacancyController {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private final VacancyRepo   repo;
    private final VacancyMapper mapper;

    /* -------------------------- paging -------------------------- */

    @GetMapping
    public List<VacancyDto> findAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Vacancy> p = repo.findAll(PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "publishedAt")));

        return p.stream().map(mapper::toDto).toList();
    }

    /* -------------------------- поиск по свободной строке -------------------------- */

    @GetMapping("/search")
    public List<VacancyDto> search(
            @RequestParam                 String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pg = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "publishedAt"));

        Specification<Vacancy> spec = VacancySpecifications.jobTitleContains(q);
        return repo.findAll(spec, pg).stream().map(mapper::toDto).toList();
    }

    /* -------------------------- util: «глубокий» URL-decode -------------------------- */

    private static String urlDeepDecode(String raw) {
        if (raw == null) return null;

        String prev = raw;
        while (true) {
            try {
                String dec = URLDecoder.decode(prev, UTF8);
                if (dec.equals(prev)) return dec;      // дальше нечего декодировать
                prev = dec;
            } catch (IllegalArgumentException ex) {    // оборванный «%»
                return prev;                           // что смогли – то вернём
            }
        }
    }

    /* -------------------------- фильтр -------------------------- */

    @GetMapping("/filter")
    public List<VacancyDto> filter(
            @RequestParam                 String jobTitle,        // ОБЯЗАТЕЛЬНЫЙ
            @RequestParam(required = false) String city,          // неснимаемый (если есть)

            @RequestParam(required = false) Boolean car,
            @RequestParam(required = false) Set<String> workFormat,
            @RequestParam(required = false) Set<String> license,

            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        /* нормализуем вход прямо здесь, чтобы city сравнивался 1-к-1 */
        jobTitle = urlDeepDecode(jobTitle);
        city     = urlDeepDecode(city);

        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));

        /* обязательные условия: jobTitle     (всегда)
                               + city         (если указан)               */
        Specification<Vacancy> mandatory = VacancySpecifications
                .jobTitleContains(jobTitle)
                .and(VacancySpecifications.cityEquals(city));

        /* дополнительные (можем постепенно снимать) */
        List<Specification<Vacancy>> optional = List.of(
                VacancySpecifications.workFormatIn(workFormat),
                VacancySpecifications.carRequired(car),
                VacancySpecifications.anyLicense(license)
        );

        /* сначала — все фильтры; если пусто — снимаем по одному слева направо */
        for (int i = 0; i <= optional.size(); i++) {
            Specification<Vacancy> spec = mandatory;
            for (int j = 0; j < optional.size() - i; j++) {
                spec = spec.and(optional.get(j));
            }

            List<VacancyDto> data = repo.findAll(spec, pageable)
                    .stream()
                    .map(mapper::toDto)
                    .toList();

            if (!data.isEmpty() || i == optional.size()) {
                return data;
            }
        }
        return List.of();   // практически недостижимо
    }
}
