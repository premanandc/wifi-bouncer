package com.amplify.wifibouncer.charts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.view.View;
import com.google.inject.Inject;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import roboguice.inject.ContextSingleton;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@ContextSingleton
public class SignalStrengthBarChart {

    private GraphicalView chartView;
    private final XYMultipleSeriesDataset dataSet;
    private final XYMultipleSeriesRenderer seriesRenderer;

    @Inject
    public SignalStrengthBarChart(Context context) {
        seriesRenderer = createRenderer();
        dataSet = createDataSet();
        chartView = ChartFactory.getBarChartView(context,
                dataSet, seriesRenderer, BarChart.Type.DEFAULT);
    }

    private XYMultipleSeriesDataset createDataSet() {
        final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        XYSeries series = new XYSeries("All Nearby Networks");
        dataset.addSeries(series);
        return dataset;
    }

    public View view() {
        return chartView;
    }

    public void update(final List<ScanResult> scanResults) {
        new Thread() {
            @Override
            public void run() {
                List<ScanResult> results = filterAndSort(scanResults);

                XYSeries series = new XYSeries("");
                for (int i = 0; i < results.size(); i++) {
                    ScanResult result = scanResults.get(i);
                    series.add(i + 2, result.level);
                    seriesRenderer.addXTextLabel(i + 2, result.SSID);

                }
                dataSet.removeSeries(0);
                dataSet.addSeries(series);
                chartView.repaint();
            }
        }.start();
    }

    private List<ScanResult> filterAndSort(List<ScanResult> scanResults) {
        List<ScanResult> results = scanResults.subList(0, Math.min(scanResults.size(), 10));
        Collections.sort(results, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return rhs.level - lhs.level;
            }
        });
        return results;
    }

    private XYMultipleSeriesRenderer createRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxisTitleTextSize(15);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(12);
        renderer.setPointSize(10f);
        renderer.setXLabelsAngle(270);
        renderer.setXLabelsAlign(Paint.Align.LEFT);
        renderer.setMargins(new int[]{60, 30, 30, 60});
        renderer.setBackgroundColor(Color.BLUE);
        renderer.setYAxisMax(-110);
        renderer.setYAxisMin(-20);
        renderer.setXAxisMin(1);
        renderer.setXAxisMax(12);
        renderer.setShowAxes(true);
        renderer.setShowLegend(false);
        renderer.setShowGridX(true);
        renderer.setXLabels(1);
        renderer.setClickEnabled(false);
        renderer.setExternalZoomEnabled(false);
        renderer.setPanEnabled(true, false);
        renderer.setZoomEnabled(false, false);
        renderer.setAxesColor(Color.WHITE);
        renderer.setShowGrid(true);
        renderer.setBarWidth(20);
        renderer.setBarSpacing(0.25);
        renderer.setChartTitle("All Nearby Networks");
        renderer.setXTitle("Network");
        renderer.setYTitle("Strength");
        renderer.setYLabels(11);
        renderer.setXLabelsPadding(0);
        renderer.setYLabelsPadding(20);

        SimpleSeriesRenderer series = new XYSeriesRenderer();
        series.setDisplayChartValues(true);
        series.setChartValuesTextAlign(Paint.Align.RIGHT);
        series.setChartValuesTextSize(10);
        series.setChartValuesSpacing(-15);
        series.setColor(Color.BLUE);
        renderer.addSeriesRenderer(series);

        return renderer;
    }

}
