package eu.clarin.cmdi.curation.chart.conf;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "curation.chart")
@Data
public class ChartConfig {

        private String title;

        private String xAxisLabel;

        private String yAxisLabel;

        private int width;

        private int height;

        private Map<String, String> colors = new LinkedHashMap<>();
}
