package vnedraid.inputservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import java.time.OffsetDateTime;

/** Упрощённая модель вакансии */
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

    /** Нижняя граница "вилки" зарплаты */
    private Integer salaryFrom;

    /** Верхняя граница "вилки" зарплаты */
    private Integer salaryTo;

    /** Валюта (RUR, USD …) */
    private String currency;

    /** Требуется личный автомобиль */
    private Boolean carRequired;

    /** Категории прав через запятую (B,C, …) */
    private String driverLicenseCategories;

    /** Профессиональная роль(и) через запятую (Java-разработчик, Тестировщик …) */
    private String professionalRoles;

    /** График (Полный день, Сменный …) */
    private String schedule;

    /** Дата публикации */
    private OffsetDateTime publishedAt;
}
