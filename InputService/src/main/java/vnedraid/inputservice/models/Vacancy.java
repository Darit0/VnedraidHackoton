package vnedraid.inputservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Упрощённая модель вакансии:
 *   – содержит только те поля, которые реально используются аналитикой
 *   – обновляется по первичному ключу id (upsert)
 */
@Entity
@Table(name = "vacancy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vacancy {

    /** id вакансии на HH */
    @Id
    private String id;

    /** Должность / заголовок вакансии */
    private String title;

    /** Город */
    private String city;

    /** Работодатель */
    private String employer;

    /** Нижняя граница "вилка" зарплаты */
    private Integer salaryFrom;

    /** Верхняя граница "вилка" зарплаты */
    private Integer salaryTo;

    /** Валюта (RUR, USD …) */
    private String currency;

    /** Требуется личный автомобиль */
    private Boolean carRequired;

    /** Категории прав через запятую (B,C, …) */
    private String driverLicenseCategories;

    /** График (Полный день, Смена …) */
    private String schedule;

    /** Дата публикации */
    private OffsetDateTime publishedAt;
}
