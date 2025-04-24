package eu.clarin.cmdi.curation.chart;

import eu.clarin.cmdi.curation.chart.conf.ChartConfig;
import org.springframework.stereotype.Component;

/**
 * The type Simple chart factory.
 */
@Component

public class SimpleChartFactoryImpl implements SimpleChartFactory {

    private final ChartConfig conf;

    /**
     * Instantiates a new Simple chart factory.
     *
     * @param conf an instance of ChartConfig that is used to pre-configure al Chart instances of this factory
     */
    public SimpleChartFactoryImpl(ChartConfig conf) {

        this.conf = conf;
    }

    /**
     * Create stacked area chart stacked area chart.
     *
     * @return a object representation of a pre-configured stacked area chart
     */
    @Override
    public StackedAreaChart createStackedAreaChart() {

        return new StackedAreaChart(conf);
    }
}
