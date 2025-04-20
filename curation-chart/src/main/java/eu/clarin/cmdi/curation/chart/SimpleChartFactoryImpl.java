package eu.clarin.cmdi.curation.chart;

import eu.clarin.cmdi.curation.chart.conf.ChartConfig;
import org.springframework.stereotype.Component;

@Component

public class SimpleChartFactoryImpl implements SimpleChartFactory {

    private final ChartConfig conf;

    public SimpleChartFactoryImpl(ChartConfig conf) {

        this.conf = conf;
    }

    @Override
    public StackedAreaChart createStackedAreaChart() {

        return new StackedAreaChart(conf);
    }
}
