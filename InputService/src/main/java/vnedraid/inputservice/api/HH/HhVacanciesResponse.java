package vnedraid.inputservice.api.HH;

import lombok.Data;

import java.util.List;

@Data
public class HhVacanciesResponse {
    private Integer pages;
    private List<Item> items;

    @Data public static class Item {
        private String id;
        private String name;
        private Area area;
        private Salary salary;
        private Experience experience;
        private List<ProfessionalRole> professional_roles;
        private String published_at;
        private String description;

        @Data public static class Area { private String name; }
        @Data public static class Salary {
            private Integer from; private Integer to; private String currency;
        }
        @Data public static class Experience { private String name; }
        @Data public static class ProfessionalRole { private String name; }
    }
}
