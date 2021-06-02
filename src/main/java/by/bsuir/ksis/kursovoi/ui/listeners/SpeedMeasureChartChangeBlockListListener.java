package by.bsuir.ksis.kursovoi.ui.listeners;

import by.bsuir.ksis.kursovoi.data.Block;
import by.bsuir.ksis.kursovoi.data.Piece;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.stream.IntStream;

public class SpeedMeasureChartChangeBlockListListener implements ListChangeListener<Block> {

    private static final Logger LOGGER = Logger.getRootLogger();

    private final XYChart.Series<Number, Number> series;
    private final int[] values;
    private final long start = System.currentTimeMillis();
    private long lastMeasured = 0;

    public SpeedMeasureChartChangeBlockListListener(XYChart.Series<Number, Number> series, int seconds) {
        this.series = series;
        this.values = new int[seconds];
    }

    @Override
    public void onChanged(Change<? extends Block> event) {
        while (event.next()) {
            if (event.wasRemoved()) {
                long now = System.currentTimeMillis();
                values[0] += event.getRemovedSize();
                if (now - lastMeasured > 1000) {
                    int secondsPassed = (int) ((now - lastMeasured) / 1000);
                    secondsPassed = Math.min(secondsPassed, values.length);
                    lastMeasured = now;
                    Platform.runLater(() -> series.getData().add(new XYChart.Data<>((now - start) / 1000, (double) IntStream.of(values).sum() * (1 << 14)/values.length)));
                    for (int i = values.length - 1; i >= secondsPassed; i--) {
                        values[i] = values[i - secondsPassed];
                    }
                    for (int i = 0; i < secondsPassed; i++) {
                        values[i] = 0;
                    }
                }
            }
        }
    }
}