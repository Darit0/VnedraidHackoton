package vnedraid.apiservice.spec;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import vnedraid.inputservice.models.Vacancy;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JPA-Specifications для entity {@link Vacancy}.
 */
public final class VacancySpecifications {

    private VacancySpecifications() {}

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private static String urlDeepDecode(String raw) {
        if (raw == null) return null;
        String prev = raw;
        while (true) {
            try {
                String dec = URLDecoder.decode(prev, UTF8);
                if (dec.equals(prev)) return dec;
                prev = dec;
            } catch (IllegalArgumentException e) {
                return prev;
            }
        }
    }

    /** city = ? (полное совпадение) */
    public static Specification<Vacancy> cityEquals(String city) {
        return (root, q, cb) ->
                (city == null || city.isBlank())
                        ? cb.conjunction()
                        : cb.equal(root.get("city"), city);
    }

    /**
     * Находит строку <code>text</code> (без учёта регистра) либо
     * в <code>title</code>, либо в <code>professionalRoles</code>.
     * <p>Используется дополнительные приёмы:
     * <ul>
     *   <li>глубокий URL-decode;</li>
     *   <li>сведение к нижнему регистру;</li>
     *   <li>поиск по «корню» (первые 5 символов) — чтобы
     *       частично оборванное слово тоже нашлось.</li>
     * </ul>
     */
    public static Specification<Vacancy> jobTitleContains(String text) {
        return (root, q, cb) -> {
            if (text == null || text.isBlank()) return cb.conjunction();

            String norm = urlDeepDecode(text)
                    .toLowerCase()
                    .replaceAll("\\s+", " ")
                    .trim();
            if (norm.length() > 5) norm = norm.substring(0, 5);

            String pattern = "%" + norm + "%";

            Expression<String> title = cb.lower(root.get("title"));
            Expression<String> role  = cb.lower(root.get("professionalRoles"));

            return cb.or(cb.like(title, pattern),
                    cb.like(role,  pattern));
        };
    }

    /** carRequired = true / false */
    public static Specification<Vacancy> carRequired(Boolean needCar) {
        return (root, q, cb) ->
                needCar == null ? cb.conjunction()
                        : cb.equal(root.get("carRequired"), needCar);
    }

    private static final Map<String, String> WF_MAP = Map.of(
            "onSite",   "Полный день",
            "remotely", "Удаленная работа",
            "hybrid",   "Гибкий график",
            "journey",  "Сменный график"
    );

    public static Specification<Vacancy> workFormatIn(Set<String> wf) {
        return (root, q, cb) -> {
            if (wf == null || wf.isEmpty()) return cb.conjunction();

            Set<String> mapped = wf.stream()
                    .map(WF_MAP::get)
                    .filter(s -> s != null)
                    .collect(Collectors.toSet());

            return mapped.isEmpty() ? cb.conjunction()
                    : root.get("schedule").in(mapped);
        };
    }

    /** driverLicenseCategories содержит ХОТЯ БЫ одну из переданных */
    public static Specification<Vacancy> anyLicense(Set<String> cats) {
        return (root, q, cb) -> {
            if (cats == null || cats.isEmpty()) return cb.conjunction();

            Predicate p = cb.disjunction();
            Expression<String> field = root.get("driverLicenseCategories");
            for (String cat : cats) {
                p = cb.or(p, cb.like(field, "%" + cat.trim() + "%"));
            }
            return p;
        };
    }

    public static Specification<Vacancy> experienceIn(List<String> exp) {
        return (root, q, cb) -> cb.conjunction();
    }
}
