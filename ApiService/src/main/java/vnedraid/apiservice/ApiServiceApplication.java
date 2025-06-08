package vnedraid.apiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
		scanBasePackages = {
				"vnedraid.apiservice",       // собственные контроллеры
				"vnedraid.inputservice"      // модели, репозитории, сервисы
		})
@EntityScan("vnedraid.inputservice.models")
@EnableJpaRepositories(basePackages = "vnedraid.inputservice.repo")
public class ApiServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ApiServiceApplication.class, args);
	}
}
