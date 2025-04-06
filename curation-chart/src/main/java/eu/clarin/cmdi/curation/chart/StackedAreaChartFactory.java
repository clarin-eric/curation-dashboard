package eu.clarin.cmdi.curation.chart;

import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeTableXYDataset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StackedAreaChartFactory {

    final HashMap<String, TimeTableXYDataset> datasets;

    private String chartTitle;
    private String xAxisLabel;
    private String yAxisLabel;

    private int width;
    private int height;

    private StackedAreaChartFactory() {

        this.datasets = new HashMap<>();
    }

    public static StackedAreaChartFactory newInstance() {

        return new StackedAreaChartFactory();
    }

    public void setLabels(String chartTitle, String xAxisLabel, String yAxisLabel) {
        this.chartTitle = chartTitle;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
    }

    public void setDimensions(int width, int height) {

        this.width = width;
        this.height = height;
    }

    public void addXY(String datasetName, String seriesName, Date date, Double value){

        this.datasets.computeIfAbsent(datasetName, k -> new TimeTableXYDataset()).add(new Day(date), value, seriesName);
    }

    public void save(Path outputDir, String datasetName) throws IOException {

        if(datasets.containsKey(datasetName)){

            JFreeChart chart = ChartFactory.createTimeSeriesChart(this.chartTitle, this.xAxisLabel, this.yAxisLabel, datasets.get(datasetName));
            chart.getXYPlot().setRenderer(new StackedXYAreaRenderer2());

            ChartUtils.saveChartAsPNG(new File(outputDir.toFile(), datasetName + ".png"), chart, this.width, this.height);
        }
        else{

            log.error("Dataset '{}' does not exist", datasetName);
        }
 }

    public void saveAll(Path outputDir) throws IOException {

        for (Map.Entry<String, TimeTableXYDataset> entry : this.datasets.entrySet()) {

            JFreeChart chart = ChartFactory.createTimeSeriesChart(this.chartTitle, this.xAxisLabel, this.yAxisLabel, entry.getValue());
            chart.getXYPlot().setRenderer(new StackedXYAreaRenderer2());

            ChartUtils.saveChartAsPNG(new File(outputDir.toFile(), entry.getKey() + ".png"), chart, this.width, this.height);
        }
    }
}
