package com.kaliturin.scicharttest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;

import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.modifiers.PinchZoomModifier;
import com.scichart.charting.modifiers.ZoomExtentsModifier;
import com.scichart.charting.modifiers.ZoomPanModifier;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.AxisAlignment;
import com.scichart.charting.visuals.axes.DateAxis;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.axes.NumericAxis;
import com.scichart.charting.visuals.renderableSeries.FastLineRenderableSeries;
import com.scichart.charting.visuals.renderableSeries.FastMountainRenderableSeries;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.data.model.DateRange;
import com.scichart.data.model.DoubleRange;
import com.scichart.drawing.common.BrushStyle;
import com.scichart.drawing.common.PenStyle;
import com.scichart.drawing.common.SolidBrushStyle;
import com.scichart.drawing.utility.ColorUtil;

import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    // Using service factory for getting particular rates service
    private final Class<? extends RateService> serviceClass = new RateServiceFactory().getService();

    private final static long TIME_INTERVAL = 1000;
    private final static int CHART_CAPACITY = 60;

    private InsertPointRunnable insertPointRunnable = new InsertPointRunnable();
    private XyDataSeries<Long, Double> chartDataSeries = new XyDataSeries<>(Long.class, Double.class);
    private XyDataSeries<Long, Double> lineDataSeries = new XyDataSeries<>(Long.class, Double.class);
    private DateRange xVisibleRange = null;
    private DoubleRange yVisibleRange = null;
    private SciChartSurface chartSurface = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initChart();
        startService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopService();
    }

    private void initChart() {
        // visible ranges
        long time = System.currentTimeMillis();
        xVisibleRange = new DateRange(new Date(time), new Date(time + TIME_INTERVAL * CHART_CAPACITY));
        yVisibleRange = new DoubleRange();

        // chart surface
        chartSurface = (SciChartSurface) findViewById(R.id.chartView);
        chartSurface.zoomExtents();

        // X axis
        IAxis xAxis = new DateAxis(this);
        xAxis.setTextFormatting("mm:ss");
        xAxis.setAxisTitle("Time");
        xAxis.setVisibleRange(xVisibleRange);
        Collections.addAll(chartSurface.getXAxes(), xAxis);

        // Y axis
        IAxis yAxis = new NumericAxis(this);
        yAxis.setAxisTitle("Rate");
        yAxis.setAxisAlignment(AxisAlignment.Right);
        yAxis.setVisibleRange(yVisibleRange);
        Collections.addAll(chartSurface.getYAxes(), yAxis);

        // chart series
        FastMountainRenderableSeries chartSeries = new FastMountainRenderableSeries();
        Collections.addAll(chartSurface.getRenderableSeries(), chartSeries);

        // create a pen for drawing and a brush for fill chart
        PenStyle penStyle = new PenStyle(ColorUtil.Blue, true, 2f);
        chartSeries.setStrokeStyle(penStyle);
        BrushStyle fillStyle = new SolidBrushStyle(ColorUtil.argb(0x80, 0, 0, 0x80));
        chartSeries.setAreaStyle(fillStyle);

        // enable zooming
        Collections.addAll(chartSurface.getChartModifiers(), new PinchZoomModifier());
        Collections.addAll(chartSurface.getChartModifiers(), new ZoomPanModifier());
        Collections.addAll(chartSurface.getChartModifiers(), new ZoomExtentsModifier());

        // link chart series to data
        chartSeries.setDataSeries(chartDataSeries);
        chartDataSeries.setFifoCapacity(CHART_CAPACITY);

        // line series
        FastLineRenderableSeries lineSeries = new FastLineRenderableSeries();
        Collections.addAll(chartSurface.getRenderableSeries(), lineSeries);

        // create a pen
        penStyle = new PenStyle(ColorUtil.Grey, true, 2f);
        lineSeries.setStrokeStyle(penStyle);

        // link line series to data
        lineSeries.setDataSeries(lineDataSeries);
        lineDataSeries.setFifoCapacity(2);
    }

    // Starts the rates service
    private void startService() {
        if (serviceClass != null) {
            Intent intent = new Intent(this, serviceClass);
            intent.putExtra(RateService.RECEIVER, resultReceiver);
            intent.putExtra(RateService.TIME_INTERVAL, TIME_INTERVAL);
            startService(intent);
        }
    }

    // Stops the rates service
    private void stopService() {
        if (serviceClass != null) {
            Intent intent = new Intent(this, serviceClass);
            stopService(intent);
        }
    }

    // Results receiver from the rates service
    private ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // rate point
            long date = resultData.getLong(RateService.DATE);
            double rate = resultData.getDouble(RateService.RATE);
            insertPointRunnable.setPoint(new Point(date, rate));
            // update chart
            UpdateSuspender.using(chartSurface, insertPointRunnable);
        }
    };

    // Inserts rate point into chart's series
    private class InsertPointRunnable implements Runnable {
        Point point = null;

        void setPoint(Point point) {
            this.point = point;
        }

        @Override
        public void run() {
            chartDataSeries.append(point.date, point.rate);

            if (CHART_CAPACITY - chartDataSeries.getCount() == 0) {
                // shift x range
                Long xMin = chartDataSeries.getXMin() + TIME_INTERVAL;
                Long xMax = chartDataSeries.getXMax() + TIME_INTERVAL;
                xVisibleRange.setMinMax(new Date(xMin), new Date(xMax));
            }

            // shift y range
            Double yMin = chartDataSeries.getYMin();
            Double yMax = chartDataSeries.getYMax();
            Double yMargin = (yMax - yMin) / 20;
            yVisibleRange.setMinMax(yMin - yMargin, yMax + yMargin);

            // update line data
            lineDataSeries.append(chartDataSeries.getXMin(), point.rate);
            lineDataSeries.append(Long.MAX_VALUE, point.rate);
        }
    }

}
