import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import vnedraid.analyze.LLM.OrionGptClient;

public class HHVacancyClassifier {

    // === Настройки ===
    private static final String HH_API_URL = "https://api.hh.ru/vacancies";
    private static final String ORION_API_KEY = "OrVrQoQ6T43vk0McGmHOsdvvTiX446RJ";
    private static final int OS_CODE = 12;
    private static final String USER_DOMAIN = "team61";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final OrionGptClient gptClient = new OrionGptClient(OS_CODE, ORION_API_KEY, USER_DOMAIN);

    // === Ключевые слова для фильтрации вакансий ===
    private static final Set<String> FILTER_KEYWORDS = new HashSet<>(Arrays.asList(
            "java", "python", "разработчик", "программист", "frontend", "backend", "devops", "qa", "engineer",
            "офис", "администратор", "менеджер", "бухгалтер", "склад", "кладовщик", "доставка", "курьер"
    ));

    // === Пользовательский словарь названий профессий ===
    private static final Map<String, String> CUSTOM_TITLES = new HashMap<>();

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String searchUrl = HH_API_URL + "?text=все&area=113&per_page=5";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(searchUrl))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode searchResult = mapper.readTree(response.body());

        for (JsonNode item : searchResult.get("items")) {
            String vacancyId = item.get("id").asText();
            filterAndClassify(vacancyId, client);
        }
    }

    private static void filterAndClassify(String vacancyId, HttpClient client) throws Exception {
        System.out.println("\n=== Загружаем вакансию: " + vacancyId + " ===");

        String vacancyUrl = "https://api.hh.ru/vacancies/"  + vacancyId;
        HttpRequest vacancyRequest = HttpRequest.newBuilder()
                .uri(URI.create(vacancyUrl))
                .GET()
                .build();

        HttpResponse<String> vacancyResponse = client.send(vacancyRequest, HttpResponse.BodyHandlers.ofString());
        JsonNode vacancyData = mapper.readTree(vacancyResponse.body());

        String title = vacancyData.has("name") ? vacancyData.get("name").asText().toLowerCase() : "";
        String description = vacancyData.has("description") ? vacancyData.get("description").asText().toLowerCase() : "";

        if (!containsAnyKeyword(title + " " + description, FILTER_KEYWORDS)) {
            System.out.println("❌ Вакансия не соответствует ни одной категории. Пропускаем.");
            return;
        }

        classifyVacancy(vacancyId, vacancyData);
    }

    private static void classifyVacancy(String vacancyId, JsonNode vacancyData) throws Exception {
        System.out.println("✅ Вакансия прошла фильтр. Начинаем анализ...");

        String title = vacancyData.has("name") ? vacancyData.get("name").asText() : "";
        String city = vacancyData.has("area") && vacancyData.get("area").has("name")
                ? vacancyData.get("area").get("name").asText() : "";

        Integer salaryFrom = vacancyData.has("salary") && vacancyData.get("salary").has("from")
                ? vacancyData.get("salary").get("from").asInt() : null;

        Integer salaryTo = vacancyData.has("salary") && vacancyData.get("salary").has("to")
                ? vacancyData.get("salary").get("to").asInt() : null;

        String description = vacancyData.has("description") ? vacancyData.get("description").asText() : "";
        String workFormat = vacancyData.has("schedule") && vacancyData.get("schedule").has("name")
                ? vacancyData.get("schedule").get("name").asText() : "";
        String publishedAt = vacancyData.has("published_at") ? vacancyData.get("published_at").asText() : "";

        String dialogId = USER_DOMAIN + "_vac_" + vacancyId;
        String cleanDescription = preprocess(description);

        if (CUSTOM_TITLES.containsKey(cleanDescription)) {
            String cachedTitle = CUSTOM_TITLES.get(cleanDescription);
            System.out.println("📌 Используем кэшированное название: " + cachedTitle);
            outputResult(cachedTitle, vacancyData, city, salaryFrom, salaryTo, workFormat, publishedAt);
            return;
        }

        String prompt = buildPrompt(title, description);
        CompletableFuture<Void> sendFuture = gptClient.sendMessage(dialogId, prompt).thenRunAsync(() -> {
            try {
                Thread.sleep(5000); // Ждём выполнения модели
                String answer = extractAnswer(gptClient.getResponse(dialogId).get());

                System.out.println("🔍 Ответ от LLM: '" + answer + "'");

                String jobTitle;

                // Если LLM не смогла определить профессию — генерируем новое название
                if (answer.equalsIgnoreCase("ничего") || answer.equals("Не найдено")) {
                    System.out.println("🧠 Генерируем новое название...");
                    jobTitle = generateCustomTitle(title, description);
                } else {
                    jobTitle = answer.trim();
                }

                CUSTOM_TITLES.put(cleanDescription, jobTitle); // Сохраняем в кэш
                gptClient.completeSession(dialogId);

                outputResult(jobTitle, vacancyData, city, salaryFrom, salaryTo, workFormat, publishedAt);

            } catch (Exception e) {
                System.err.println("❌ Ошибка при обработке вакансии: " + e.getMessage());
                try {
                    String fallbackTitle = generateCustomTitle(title, description);
                    outputResult(fallbackTitle, vacancyData, city, salaryFrom, salaryTo, workFormat, publishedAt);
                } catch (Exception ex) {
                    try {
                        outputResult("Неизвестная профессия", vacancyData, city, salaryFrom, salaryTo, workFormat, publishedAt);
                    } catch (Exception exc) {
                        throw new RuntimeException(exc);
                    }
                }
            }
        });

        sendFuture.join(); // ждем завершения
    }

    private static void outputResult(String jobTitle, JsonNode vacancyData, String city,
                                     Integer salaryFrom, Integer salaryTo, String workFormat,
                                     String publishedAt) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jobTitle", jobTitle);
        result.put("city", city);
        result.put("salary_from", salaryFrom);
        result.put("salary_to", salaryTo);
        result.put("gender", "");
        result.put("experience", new ArrayList<>());
        result.put("age", new ArrayList<>());
        result.put("education", "");
        result.put("work_format", workFormat);
        result.put("description", vacancyData.get("description").asText());
        result.put("category_of_car_rights", "");
        result.put("data_of_publication", publishedAt);

        String jsonResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        System.out.println("\n✅ Результат в формате JSON:");
        System.out.println(jsonResponse);
    }

    private static boolean containsAnyKeyword(String text, Set<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String buildPrompt(String title, String description) {
        return """
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
                - Бугхалтер
                - Секретарша
                - Менеджер
                - Юрист
                
                Если ни одна не подходит — напиши 'ничего'.
                Не добавляй объяснений, только один вариант.
                """.formatted(title, description);
    }

    private static String generateCustomTitle(String title, String description) throws Exception {
        String prompt = """
            Ты являешься помощником HR-специалиста.
            На основе следующего описания вакансии придумай подходящее название профессии (одно слово или короткая фраза).
            Не добавляй объяснений, только само название.
            
            Заголовок: %s
            Описание: %s
            
            Примеры:
            - Личный помощник
            - Администратор офиса
            - Кладовщик
            - Java Developer
            """.formatted(title, description);

        String dialogId = UUID.randomUUID().toString();
        gptClient.sendMessage(dialogId, prompt).get();
        Thread.sleep(5000);
        String rawAnswer = extractAnswer(gptClient.getResponse(dialogId).get());
        gptClient.completeSession(dialogId);

        String generatedTitle = rawAnswer.trim();
        System.out.println("🧠 Придумано новое название: " + generatedTitle);
        return generatedTitle;
    }

    private static String extractAnswer(String jsonResponse) {
        System.out.println("🔍 Сырой ответ от LLM: " + jsonResponse); // ✅ Добавили вывод
        try {
            JsonNode node = mapper.readTree(jsonResponse);
            return node.has("message") ? node.get("message").asText().trim() : "Не найдено";
        } catch (Exception e) {
            return "Ошибка разбора";
        }
    }

    private static String preprocess(String description) {
        return description.toLowerCase().replaceAll("\\s+", " ").trim();
    }
}