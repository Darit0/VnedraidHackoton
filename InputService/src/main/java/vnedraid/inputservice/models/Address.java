package vnedraid.inputservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Встраиваемый адрес вакансии.
 * Хранится в тех же строках таблицы vacancy_full, что и Vacancy.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;

    @Column(name = "building")
    private String building;

    /** широта (может быть null) */
    @Column(name = "lat")
    private String lat;

    /** долгота (может быть null) */
    @Column(name = "lon")
    private String lon;
}
