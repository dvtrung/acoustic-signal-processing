package com.dvtrung.sound.gui.charts.controllers;

import com.dvtrung.sound.lib.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import org.apache.commons.math3.complex.Complex;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dvtrung.sound.lib.Utils.CEPSTRUM_LIFTER;
import static com.dvtrung.sound.lib.Utils.getFrameWaveform;
import static com.dvtrung.sound.lib.Utils.getFrameWaveform;

public class CepstrumChart extends SoundChart {
    private LineChart cepstrumChart;
    NumberAxis xAxis, yAxis;

    ObservableList<XYChart.Data<Number, Number>> data_cepstrum;
    ObservableList<XYChart.Data<Number, Number>> data_mfcc_cepstrum;
    ObservableList<XYChart.Data<Number, Number>> data_spectrum;

    @Override
    public void init() {
        super.init();

        xAxis = new NumberAxis();
        xAxis.setLabel("Time (s)");
        yAxis = new NumberAxis();
        yAxis.setLabel("Amplitude");
        cepstrumChart = new LineChart<Number, Number>(xAxis, yAxis);
        cepstrumChart.setTitle(getTitle());
        cepstrumChart.setCreateSymbols(false);
        VBox.setVgrow(cepstrumChart, Priority.ALWAYS);
        cepstrumChart.setAnimated(false);
    }

    @Override
    public void plot(double[] waveform) {
        double wf[] = getFrameWaveform(waveform);

        final int fftSize = 1 << Le4MusicUtils.nextPow2(wf.length);
        final int fftSize2 = (fftSize >> 1) + 1;

        final double[] src = Arrays.stream(Arrays.copyOf(wf, fftSize))
                .map(w -> w / wf.length)
                .toArray();

        final Complex[] spectrum = Le4MusicUtils.rfft(src);

        double[] specLog = Arrays.stream(spectrum)
                .mapToDouble(c -> Math.log10(c.abs()))
                .toArray();

        data_spectrum = IntStream.range(0, fftSize2)
                .mapToObj(i -> new XYChart.Data<Number, Number>(i * options.sampleRate / fftSize, specLog[i]))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final XYChart.Series<Number, Number> series_spectrum = new XYChart.Series<>("Spectrum", data_spectrum);

        Complex[] cepstrum = Le4MusicUtils.rfft(Arrays.copyOf(specLog, specLog.length - 1));
        for (int i = Utils.CEPSTRUM_LIFTER; i < cepstrum.length; i++) cepstrum[i] = new Complex(0, 0);
        double[] c1 = Le4MusicUtils.irfft(cepstrum);

        data_cepstrum = IntStream.range(0, c1.length)
                .mapToObj(i -> new XYChart.Data<Number, Number>(i * options.sampleRate / fftSize, c1[i]))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final XYChart.Series<Number, Number> series_cepstrum = new XYChart.Series<>("Cepstrum", data_cepstrum);

        cepstrumChart.getData().clear();
        cepstrumChart.getData().add(series_spectrum);
        cepstrumChart.getData().add(series_cepstrum);
    }

    @Override
    public void onRecordingChanged(boolean recording) {
        getCheckBox().setSelected(true);
    }

    public Chart getChart() {
        return cepstrumChart;
    }
    public String getTitle() { return "Cepstrum (lifter " + CEPSTRUM_LIFTER + ")"; }
}
