package vnedraid.inputservice.api.hh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("hh.monitoring")
@Data
public class MonitoringProps {
    private List<Request> requests = new ArrayList<>();
    @Data public static class Request { private String text; private int area; }
}
