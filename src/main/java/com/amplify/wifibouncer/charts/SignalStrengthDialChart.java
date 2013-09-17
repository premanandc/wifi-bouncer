package com.amplify.wifibouncer.charts;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import com.google.inject.Inject;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DialRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import roboguice.inject.ContextSingleton;

@ContextSingleton
public class SignalStrengthDialChart {

    private CategorySeries category;
    private GraphicalView chartView;

    @Inject
    public SignalStrengthDialChart(Context context) {
        category = new CategorySeries("Current signal strength");
        category.add("Signal Strength", -110);
        final DialRenderer renderer = createRenderer();
        chartView = ChartFactory.getDialChartView(context, category, renderer);
    }

    public View view() {
        return chartView;
    }

    public void update(final int latest) {
        new Thread() {
            @Override
            public void run() {
                category.remove(0);
                category.add("Signal Strength", latest);
                chartView.repaint();
            }
        }.start();
    }

    private DialRenderer createRenderer() {
        final DialRenderer renderer = new DialRenderer();
        renderer.setChartTitle("Currently Connected Network");
        renderer.setPanEnabled(false);
        renderer.setZoomEnabled(false);
        renderer.setShowLegend(false);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        renderer.setMargins(new int[]{0, 0, 0, 0});
        SimpleSeriesRenderer series = new SimpleSeriesRenderer();
        series.setColor(Color.GREEN);
        renderer.addSeriesRenderer(series);
        renderer.setMinValue(-110);
        renderer.setMaxValue(-20);
        return renderer;
    }

}
