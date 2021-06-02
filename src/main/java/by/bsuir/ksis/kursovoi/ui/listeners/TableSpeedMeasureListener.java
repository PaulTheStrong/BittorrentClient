package by.bsuir.ksis.kursovoi.ui.listeners;

import by.bsuir.ksis.kursovoi.client.PieceManager;
import by.bsuir.ksis.kursovoi.data.Block;
import by.bsuir.ksis.kursovoi.data.Piece;
import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import by.bsuir.ksis.kursovoi.ui.TableRecord;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.chart.XYChart;
import org.apache.log4j.Logger;

import java.util.stream.IntStream;

public class TableSpeedMeasureListener implements ListChangeListener<Block> {
    private static final Logger LOGGER = Logger.getRootLogger();

    private final int[] values;
    private final long start = System.currentTimeMillis();
    private long lastMeasured = 0;
    private final PieceManager pieceManager;
    private final TableRecord tableRecord;

    public TableSpeedMeasureListener(TableRecord tableRecord, PieceManager pieceManager, int seconds) {
        this.tableRecord = tableRecord;
        this.pieceManager = pieceManager;
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
                    double downloadSpeed = (double) IntStream.of(values).sum() * (1 << 14) / values.length;
                    setDownloadSpeed(downloadSpeed, tableRecord);
                    setDownloaded(tableRecord);
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

    private void setDownloaded(TableRecord tableRecord) {
        double downloaded = (double) pieceManager.getBytesDownloaded();
        String measure = "bytes";
        if (downloaded > 1024) {
            measure = "KB";
            downloaded /= 1024;
        }
        if (downloaded > 1024) {
            measure = "MB";
            downloaded /= 1024;
        }
        if (downloaded > 1024) {
            measure = "GB";
        }
        tableRecord.setDownloaded(String.format("%.2f %s", downloaded, measure));
    }

    private void setDownloadSpeed(double speed, TableRecord tableRecord) {
        String measure = "Bytes";
        long bytesLeft = pieceManager.getTorrent().getTotalSize() - pieceManager.getBytesDownloaded();
        long secondsLeft = speed != 0 ? (long) (bytesLeft / speed) : 0;
        if (speed > 1024) {
            measure = "KB";
            speed /= 1024;
        }
        if (speed > 1024) {
            measure = "MB";
            speed /= 1024;
        }
        if (speed > 1024) {
            measure = "GB";
            speed /= 1024;
        }
        tableRecord.setDownloadSpeed(String.format("%.2f %s / s", speed, measure));
        tableRecord.setTime(String.format("%dч %dмин %dсек", secondsLeft / 3600, (secondsLeft % 3600) / 60, secondsLeft % 3600 % 60));
    }
}
