package vnedraid.inputservice.configs;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import vnedraid.inputservice.services.impl.CollectorHH;

import java.time.Duration;

@Configuration
public class HhWebConfig {

    @Bean
    public WebClient hhWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.hh.ru")
                .defaultHeader(HttpHeaders.USER_AGENT, "Vnedraid-InputService/1.0")
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")   // 👈 Brotli off
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(25))        // read timeout
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)))
                .codecs(c -> c.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();
    }
}

