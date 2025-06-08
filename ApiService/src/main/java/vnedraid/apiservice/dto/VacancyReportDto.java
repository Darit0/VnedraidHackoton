package vnedraid.apiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacancyReportDto {
    private double averageSalaryFrom;
    private double averageSalaryTo;
    private double averageTotalSalary;
    private double medianSalary;
    private long count;
    private Map<Integer, Long> salaryDistribution;
    private Map<String, Long> scheduleCounts;
    private Map<String, Double> averageSalaryBySchedule;
}
