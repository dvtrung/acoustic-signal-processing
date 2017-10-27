package com.dvtrung.sound.chart;

import com.dvtrung.sound.train.StatisticalModel;
import com.dvtrung.sound.train.TrainingHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jp.ac.kyoto_u.kuis.le4music.LineChartWithMarkers;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RecognitionResultChart extends SoundChart {
    private LineChartWithMarkers chart;
    private double maxVal, minVal;
    NumberAxis xAxis, yAxis;

    ObservableList<XYChart.Data<Number, Number>> data;

    XYChart.Data<Number, Number> currentPosMarker;

    @Override
    public void init() {
        super.init();

        xAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        yAxis = new NumberAxis();
        yAxis.setLabel("Amplitude");
        chart = new LineChartWithMarkers<Number, Number>(xAxis, yAxis);
        chart.setTitle(getTitle());
        VBox.setVgrow(chart, Priority.ALWAYS);
        chart.setAnimated(false);
        VBox.setVgrow(chart, Priority.ALWAYS);

        currentPosMarker = new XYChart.Data<Number, Number>(0, 0);
        //chart.addVerticalValueMarker(currentPosMarker);
    }

    @Override
    public void plot(double[] waveform) {
        if (data != null) return;

        int frameSize = StatisticalModel.FRAME_LENGTH_IN_MILIS * (int)options.sampleRate / 1000;
        int frameCount = waveform.length / frameSize;
        double[] res = new double[frameCount];
        for (int i = 0; i < frameCount; i++) {
            res[i] = TrainingHelper.getInstance().getLabelIndex(waveform, i * frameSize);
        }
        data = IntStream.range(0, frameCount * 2)
                        .mapToObj(i -> new XYChart.Data<Number, Number>((i / 2 + i % 2) * frameSize / options.sampleRate, res[i / 2]))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Result");
        series.setData(data);

        chart.setCreateSymbols(false);
        chart.getData().clear();
        chart.getData().add(series);

        xAxis.setAutoRanging(false);
        xAxis.setUpperBound((waveform.length - 1) / options.sampleRate);
    }

    public Chart getChart() {
        return chart;
    }
    public String getTitle() { return "Recognition Result"; }
}
