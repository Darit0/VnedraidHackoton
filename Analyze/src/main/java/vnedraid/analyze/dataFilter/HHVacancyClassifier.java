package vnedraid.analyze.dataFilter;

import vnedraid.analyze.LLM.OrionGptClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HHVacancyClassifier {

    private static final OrionGptClient gptClient = new OrionGptClient(
            1, "Team0MYSiPLOee4F", "example.com");
    private static final String DIALOG_ID = "vacancy_classifier_dialog_001";

    public static void main(String[] args) throws Exception {
        String title = "Java Разработчик";
        String description = "Разработка backend-части приложений на Java. Использование Spring Boot, Hibernate.";

        classifyVacancy(title, description).thenAccept(System.out::println);
    }

    public static CompletableFuture<String> classifyVacancy(String title, String description) {
        String prompt = """
                Ты являешься помощником HR-специалиста.
                Ниже тебе представлена вакансия:
                
                Заголовок: %s
                Описание: %s
                
                Определи, какая это профессия на основе ключевых слов. Возможные варианты:
                - Java Developer
                - Frontend Developer
                - DevOps Engineer
                - QA Engineer
                - Product Manager
                - Data Scientist
                - C++ Developer
                - Python Developer
                - Mobile Developer
                - Кладовщик
                - Грузчик
                - Доставщик
                - Курьер
                - Бухгалтер
                - Секретарь
                - Менеджер
                - Юрист
                
                Если ни одна не подходит — напиши 'ничего'.
                Не добавляй объяснений, только один вариант.
                """.formatted(title, description);

        try {
            return gptClient.sendMessage(DIALOG_ID, prompt)
                    .thenAccept(response -> System.out.println("📩 Отправлено."))
                    .thenCompose(v ->
                            // Задержка на 5 секунд перед получением ответа
                            CompletableFuture.runAsync(() -> {},
                                    CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
                            )
                    )
                    .thenCompose(v -> gptClient.getResponse(DIALOG_ID))
                    .thenApply(HHVacancyClassifier::extractAnswer)
                    .exceptionally(ex -> {
                        System.err.println("❌ Ошибка при классификации: " + ex.getMessage());
                        return "ничего";
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String extractAnswer(String response) {
        // Предположим, что ответ — строка с названием профессии
        return response.trim().replaceAll("[^\\w\\s]", "").trim();
    }
}