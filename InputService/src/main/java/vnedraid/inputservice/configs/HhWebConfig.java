package vnedraid.inputservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import vnedraid.inputservice.services.impl.CollectorHH;

@Configuration
public class HhWebConfig {
    @Bean
    public WebClient hhWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.hh.ru")
                .defaultHeader(HttpHeaders.USER_AGENT, "Vnedraid-InputService/1.0")
                .build();
    }
}
