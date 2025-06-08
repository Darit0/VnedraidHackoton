package vnedraid.analyze.dto;

import java.util.List;

public class FilterDTO {
    private String jobTitle;
    private String city;
    private List<String> experience;
    private List<String> age;
    private List<String> source;
    private String education;
    private List<String> workFormat;
    private boolean car;
    private List<String> license;

    // Геттеры и сеттеры
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public List<String> getExperience() { return experience; }
    public void setExperience(List<String> experience) { this.experience = experience; }

    public List<String> getAge() { return age; }
    public void setAge(List<String> age) { this.age = age; }

    public List<String> getSource() { return source; }
    public void setSource(List<String> source) { this.source = source; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public List<String> getWorkFormat() { return workFormat; }
    public void setWorkFormat(List<String> workFormat) { this.workFormat = workFormat; }

    public boolean isCar() { return car; }
    public void setCar(boolean car) { this.car = car; }

    public List<String> getLicense() { return license; }
    public void setLicense(List<String> license) { this.license = license; }
}
