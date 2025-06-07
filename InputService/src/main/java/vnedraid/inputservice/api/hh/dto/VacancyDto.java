package vnedraid.inputservice.api.hh.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VacancyDto {

    private String id;
    private Boolean premium;
    private String name;
    private Map<String, Object> department;
    @JsonProperty("has_test")
    private Boolean hasTest;
    @JsonProperty("response_letter_required")
    private Boolean responseLetterRequired;

    private Area area;
    private Salary salary;
    @JsonProperty("salary_range")
    private SalaryRange salaryRange;
    private Type type;
    private Address address;
    @JsonProperty("response_url")
    private String responseUrl;
    @JsonProperty("sort_point_distance")
    private Integer sortPointDistance;
    @JsonProperty("published_at")
    private String publishedAt;
    @JsonProperty("created_at")
    private String createdAt;
    private Boolean archived;
    @JsonProperty("apply_alternate_url")
    private String applyAlternateUrl;
    @JsonProperty("show_logo_in_search")
    private Boolean showLogoInSearch;
    @JsonProperty("show_contacts")
    private Boolean showContacts;
    @JsonProperty("insider_interview")
    private Object insiderInterview;
    private String url;
    @JsonProperty("alternate_url")
    private String alternateUrl;
    private List<Object> relations;
    private Employer employer;
    private Snippet snippet;
    private Object contacts;
    private Schedule schedule;
    @JsonProperty("working_days")
    private List<Object> workingDays;
    @JsonProperty("working_time_intervals")
    private List<Object> workingTimeIntervals;
    @JsonProperty("working_time_modes")
    private List<Object> workingTimeModes;
    @JsonProperty("accept_temporary")
    private Boolean acceptTemporary;
    @JsonProperty("fly_in_fly_out_duration")
    private List<Object> flyInFlyOutDuration;
    @JsonProperty("work_format")
    private List<Dict> workFormat;
    @JsonProperty("working_hours")
    private List<Dict> workingHours;
    @JsonProperty("work_schedule_by_days")
    private List<Dict> workScheduleByDays;
    @JsonProperty("night_shifts")
    private Boolean nightShifts;
    @JsonProperty("professional_roles")
    private List<Dict> professionalRoles;
    @JsonProperty("accept_incomplete_resumes")
    private Boolean acceptIncompleteResumes;
    private Experience experience;
    private Employment employment;
    @JsonProperty("employment_form")
    private EmploymentForm employmentForm;
    private Boolean internship;
    @JsonProperty("adv_response_url")
    private String advResponseUrl;
    @JsonProperty("is_adv_vacancy")
    private Boolean isAdvVacancy;
    @JsonProperty("adv_context")
    private Object advContext;
    /* --- любые новые поля HH появятся здесь ― JsonIgnoreProperties=true защитит парсер --- */

    /* ↓ вложенные структуры */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Area {
        private String id;
        private String name;
        private String url;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Salary {
        private Integer from;
        private Integer to;
        private String currency;
        private Boolean gross;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SalaryRange {
        private Integer min;
        private Integer max;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Type {
        private String id;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String city;
        private String street;
        private String building;
        private Double lat;
        private Double lng;
        private String description;
        private String raw;
        private Metro metro;
        @JsonProperty("metro_stations")
        private List<Metro> metroStations;
        private String id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metro {
        @JsonProperty("station_name")
        private String stationName;
        @JsonProperty("line_name")
        private String lineName;
        @JsonProperty("station_id")
        private String stationId;
        @JsonProperty("line_id")
        private String lineId;
        private Double lat;
        private Double lng;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Employer {
        private String id;
        private String name;
        private String url;
        @JsonProperty("alternate_url")
        private String alternateUrl;
        @JsonProperty("logo_urls")
        private Map<String, String> logoUrls;
        @JsonProperty("vacancies_url")
        private String vacanciesUrl;
        @JsonProperty("accredited_it_employer")
        private Boolean accreditedItEmployer;
        @JsonProperty("employer_rating")
        private Map<String, Object> employerRating;
        private Boolean trusted;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Snippet {
        private String requirement;
        private String responsibility;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Schedule {
        private String id;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Experience {
        private String id;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Employment {
        private String id;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmploymentForm {
        private String id;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Dict {
        private String id;
        private String name;
    }
}
