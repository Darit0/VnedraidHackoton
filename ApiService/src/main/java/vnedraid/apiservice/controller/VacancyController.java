package vnedraid.apiservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import vnedraid.apiservice.dto.VacancyDto;
import vnedraid.apiservice.dto.VacancyReportDto;
import vnedraid.apiservice.mapper.VacancyMapper;
import vnedraid.apiservice.spec.VacancySpecifications;
import vnedraid.inputservice.models.Vacancy;
import vnedraid.inputservice.repo.VacancyRepo;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/vacancies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VacancyController {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private final VacancyRepo   repo;
    private final VacancyMapper mapper;

    @GetMapping
    public List<VacancyDto> findAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Vacancy> p = repo.findAll(PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "publishedAt")));

        return p.stream().map(mapper::toDto).toList();
    }

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

    private static String urlDeepDecode(String raw) {
        if (raw == null) return null;

        String prev = raw;
        while (true) {
            try {
                String dec = URLDecoder.decode(prev, UTF8);
                if (dec.equals(prev)) return dec;
                prev = dec;
            } catch (IllegalArgumentException ex) {
                return prev;
            }
        }
    }

    @GetMapping("/filter")
    public List<VacancyDto> filter(
            @RequestParam                 String jobTitle,
            @RequestParam(required = false) String city,

            @RequestParam(required = false) Boolean car,
            @RequestParam(required = false) Set<String> workFormat,
            @RequestParam(required = false) Set<String> license,

            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        jobTitle = urlDeepDecode(jobTitle);
        city     = urlDeepDecode(city);

        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));

        Specification<Vacancy> mandatory = VacancySpecifications
                .jobTitleContains(jobTitle)
                .and(VacancySpecifications.cityEquals(city));

        List<Specification<Vacancy>> optional = List.of(
                VacancySpecifications.workFormatIn(workFormat),
                VacancySpecifications.carRequired(car),
                VacancySpecifications.anyLicense(license)
        );

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
        return List.of();
    }
    @GetMapping("/report")
    public VacancyReportDto report(@RequestParam String jobTitle,
                                   @RequestParam(required = false) String city,

                                   @RequestParam(required = false) Boolean car,
                                   @RequestParam(required = false) Set<String> workFormat,
                                   @RequestParam(required = false) Set<String> license,

                                   @RequestParam(defaultValue = "0")  int page,
                                   @RequestParam(defaultValue = "20") int size) {
        String decoded = urlDeepDecode(jobTitle);
        List<Vacancy> all = repo.findAll(
                VacancySpecifications.jobTitleContains(decoded)
        );

        if (all.isEmpty()) {
            return new VacancyReportDto(
                    0, 0, 0, 0, 0,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap()
            );
        }

        DoubleSummaryStatistics fromStats = all.stream()
                .map(Vacancy::getSalaryFrom)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .summaryStatistics();

        DoubleSummaryStatistics toStats = all.stream()
                .map(Vacancy::getSalaryTo)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .summaryStatistics();

        double avgFrom = fromStats.getCount() > 0 ? fromStats.getAverage() : 0;
        double avgTo   = toStats.getCount()   > 0 ? toStats.getAverage()   : 0;

        List<Double> mids = all.stream()
                .map(v -> {
                    Integer f = v.getSalaryFrom();
                    Integer t = v.getSalaryTo();
                    if (f != null && t != null)      return (f + t) / 2.0;
                    if (f != null)                   return f.doubleValue();
                    if (t != null)                   return t.doubleValue();
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        double avgTotal = mids.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        double median;
        int n = mids.size();
        if (n % 2 == 1) {
            median = mids.get(n/2);
        } else {
            median = (mids.get(n/2 - 1) + mids.get(n/2)) / 2.0;
        }

        Map<Integer, Long> histogram = mids.stream()
                .map(Double::intValue)
                .collect(Collectors.groupingBy(i -> i, Collectors.counting()));

        Map<String, List<Double>> bySchedule = all.stream()
                .filter(v -> v.getSchedule() != null)
                .collect(Collectors.groupingBy(
                        Vacancy::getSchedule,
                        Collectors.mapping(v -> {
                            Integer f = v.getSalaryFrom();
                            Integer t = v.getSalaryTo();
                            if (f != null && t != null)      return (f + t) / 2.0;
                            if (f != null)                   return f.doubleValue();
                            if (t != null)                   return t.doubleValue();
                            return null;
                        }, Collectors.filtering(Objects::nonNull, Collectors.toList()))
                ));

        Map<String, Long> scheduleCounts = bySchedule.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (long) e.getValue().size()
                ));

        Map<String, Double> avgBySchedule = bySchedule.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0)
                ));

        return new VacancyReportDto(
                avgFrom,
                avgTo,
                avgTotal,
                median,
                all.size(),
                histogram,
                scheduleCounts,
                avgBySchedule
        );
    }
}
