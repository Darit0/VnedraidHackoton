package vnedraid.inputservice.configs;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer allowCtrlChars() {
        return builder -> builder.featuresToEnable(
                JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature());
    }
}