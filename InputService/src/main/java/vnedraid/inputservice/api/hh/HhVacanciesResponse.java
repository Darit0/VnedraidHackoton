package vnedraid.inputservice.api.hh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HhVacanciesResponse {
    private Integer pages;
    private List<Item> items;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Item {
        private String id;
        private String name;
        private Area area;
        private Salary salary;
        private Experience experience;
        private List<ProfessionalRole> professional_roles;
        private String published_at;
        private String description;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Area {
            private String name;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Salary {
            private Integer from;
            private Integer to;
            private String currency;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Experience {
            private String name;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ProfessionalRole {
            private String name;
        }
    }
}
