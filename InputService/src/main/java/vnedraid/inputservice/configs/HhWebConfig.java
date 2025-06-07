package vnedraid.inputservice.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import vnedraid.inputservice.services.impl.CollectorHH;

import java.time.Duration;

@Configuration
public class HhWebConfig {

    @Bean
    public WebClient hhWebClient(ObjectMapper mapper, WebClient.Builder builder) {

        Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(mapper);
        Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(mapper);

        return builder
                .baseUrl("https://api.hh.ru")
                .defaultHeader(HttpHeaders.USER_AGENT, "Vnedraid-InputService/1.0")
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(25))
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)))
                .codecs(c -> {
                    c.defaultCodecs().jackson2JsonDecoder(decoder);
                    c.defaultCodecs().jackson2JsonEncoder(encoder);
                    c.defaultCodecs().maxInMemorySize(5 * 1024 * 1024);
                })
                .build();
    }
}

