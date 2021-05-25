package by.bsuir.ksis.kursovoi.ui;

import by.bsuir.ksis.kursovoi.client.PieceManager;
import by.bsuir.ksis.kursovoi.client.TorrentClient;
import by.bsuir.ksis.kursovoi.data.Block;
import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import by.bsuir.ksis.kursovoi.ui.listeners.PieChartPieceUpdateListener;
import by.bsuir.ksis.kursovoi.ui.listeners.SpeedMeasureChangeBlockListListener;
import by.bsuir.ksis.kursovoi.utils.BencoderParser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;

public class View extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        GridPane root = new GridPane();
        Scene scene = new Scene(root);
        stage.setTitle("Torrent stats");
        stage.setWidth(1200);
        stage.setHeight(800);

        BencoderParser parser = new BencoderParser();
        TorrentMetaInfo torrent = parser.parseInfoDictionary("src/main/resources/top.torrent");
        TorrentClient client = new TorrentClient(torrent);

        PieceManager pieceManager = client.getPieceManager();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        PieChart.Data missing = new PieChart.Data("Missing", torrent.getPieceHashes().length);
        pieChartData.add(missing);

        final PieChart chart = new PieChart(pieChartData);
        pieceManager.addHavePiecesListListener(new PieChartPieceUpdateListener(chart));
        chart.setStyle("-fx-background-insets: 0;" +
                        "-fx-border-width: 0;");

        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setTitle(torrent.getName());

        root.add(chart, 0, 0);
        stage.setScene(scene);
        stage.show();

        for (PieChart.Data data : pieChartData) {
            data.getNode().setStyle("-fx-pie-color: red; -fx-pie-border-width: 0px;-fx-background-insets: 0;");
        }
        chart.setLabelsVisible(false);

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time, seconds");
        yAxis.setLabel("Speed, bytes");
        //creating the chart
        final LineChart<Number,Number> lineChart =
                new LineChart<Number,Number>(xAxis,yAxis);

        lineChart.setTitle("Speed");
        lineChart.setAnimated(true);
        //defining a series
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        pieceManager.addPendingBlockListListener(new SpeedMeasureChangeBlockListListener(series, 20));
        //populating the series with data

        root.add(lineChart, 0, 1);
        lineChart.getData().add(series);

        new Thread(client::start).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}