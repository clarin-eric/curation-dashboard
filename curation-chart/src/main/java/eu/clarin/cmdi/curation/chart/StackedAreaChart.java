package eu.clarin.cmdi.curation.chart;

import eu.clarin.cmdi.curation.chart.conf.ChartConfig;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeTableXYDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * The type Stacked area chart.
 */
@Slf4j
public class StackedAreaChart {

    private final ChartConfig conf;

    private final Map<String, Color> colors;

    private final TimeTableXYDataset dataset;

    /**
     * Instantiates a new Stacked area chart.
     *
     * @param conf the conf
     */
    public StackedAreaChart(ChartConfig conf) {

        this.conf = conf;

        this.colors = new TreeMap<>();
        conf.getColors().forEach((key, value) -> {

            this.colors.put(key, Color.decode(value));
        });

        dataset = new TimeTableXYDataset();

    }

    /**
     * Add value.
     *
     * @param seriesName the series name
     * @param date       the date
     * @param value      the value
     */
    public void addValue(String seriesName, Date date, double value) {

        dataset.add(new Day(date), value, seriesName);
    }

    /**
     * Add values.
     *
     * @param date   the date
     * @param values the values
     */
    public void addValues(Date date, Map<String, Double> values) {

        values.forEach((key, value) -> addValue(key, date, value));
    }

    /**
     * Save.
     *
     * @param outputDir the output dir
     * @param fileName  the file name
     * @throws IOException the io exception
     */
    public void save(Path outputDir, String fileName) throws IOException {

        JFreeChart chart = ChartFactory.createTimeSeriesChart(this.conf.getTitle(), this.conf.getXAxisLabel(), this.conf.getYAxisLabel(), dataset);
        // rendering time series as stacked area chart instead of default line chart
        chart.getXYPlot().setRenderer(new StackedXYAreaRenderer2());

        // setting standard colors for categories
        IntStream.range(0, this.dataset.getSeriesCount()).forEach(index ->
                chart.getXYPlot().getRenderer().setSeriesPaint(index, this.colors.get(dataset.getSeriesKey(index))));

        ChartUtils.saveChartAsPNG(new File(outputDir.toFile(), fileName + ".png"), chart, this.conf.getWidth(), this.conf.getHeight());
    }
}
