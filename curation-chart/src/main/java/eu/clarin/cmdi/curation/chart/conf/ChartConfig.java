package eu.clarin.cmdi.curation.chart.conf;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The type Chart config.
 */
@Component
@ConfigurationProperties(prefix = "curation.chart")
@Data
public class ChartConfig {
        //title shown on the top of each chart
        private String title;
        // label printed under the x-Axis
        private String xAxisLabel;
        // label printed left from the y-Axis
        private String yAxisLabel;
        // width of the printed chart
        private int width;
        // height of the printed chart
        private int height;
        // color schema which maps a series name to a color.
        // the order of mapping is important, since the same order is used in the legend
        private Map<String, String> colors = new LinkedHashMap<>();
}
