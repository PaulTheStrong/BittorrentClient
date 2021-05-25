package by.bsuir.ksis.kursovoi.ui.listeners;

import by.bsuir.ksis.kursovoi.data.Piece;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;

public class PieChartPieceUpdateListener implements ListChangeListener<Piece> {

    private final PieChart pieChart;

    public PieChartPieceUpdateListener(PieChart pieChart) {
        this.pieChart = pieChart;
    }

    @Override
    public void onChanged(Change<? extends Piece> event) {
        while (event.next()) {
            if (event.wasAdded()) {
                for (Piece piece : event.getAddedSubList()) {
                    Platform.runLater(() -> {
                        int pieceIndex = piece.getIndex();
                        int chartIndex = 0;
                        int chartValue = 0;
                        ObservableList<PieChart.Data> pieChartData = pieChart.getData();
                        while (chartValue <= pieceIndex) {
                            PieChart.Data data = pieChartData.get(chartIndex);
                            chartValue += data.getPieValue();
                            chartIndex++;
                        }
                        chartIndex--;
                        PieChart.Data bigPiece = pieChartData.get(chartIndex);
                        double bigPieceStart = chartValue - bigPiece.getPieValue();
                        double beforeValue = pieceIndex - bigPieceStart;
                        double afterValue = chartValue - pieceIndex - 1;
                        if (beforeValue > 0) {
                            PieChart.Data beforeData = new PieChart.Data("Missing", beforeValue);
                            pieChartData.add(chartIndex + 1, beforeData);
                            beforeData.getNode().setStyle("-fx-pie-color: red; -fx-border-width: 0px;-fx-background-insets: 0;");
                            chartIndex++;
                        }
                        PieChart.Data haveData = new PieChart.Data("Have", 1);
                        pieChartData.add(chartIndex + 1, haveData);
                        haveData.getNode().setStyle("-fx-pie-color: green; -fx-border-width: 0px;-fx-background-insets: 0;");
                        if (afterValue > 0) {
                            PieChart.Data afterData = new PieChart.Data("Missing", afterValue);
                            pieChartData.add(chartIndex + 2, afterData);
                            afterData.getNode().setStyle("-fx-pie-color: red; -fx-border-width: 0px;-fx-background-insets: 0;");
                        }
                        pieChartData.remove(beforeValue > 0 ? chartIndex - 1 : chartIndex);
                        if (chartIndex > 0 && pieChartData.get(chartIndex - 1).getName().equals("Have")) {
                            haveData.setPieValue(haveData.getPieValue() + pieChartData.get(chartIndex - 1).getPieValue());
                            pieChartData.remove(chartIndex - 1);
                            chartIndex--;
                        }

                        if (chartIndex + 1 < pieChartData.size() && pieChartData.get(chartIndex + 1).getName().equals("Have")) {
                            haveData.setPieValue(haveData.getPieValue() + pieChartData.get(chartIndex + 1).getPieValue());
                            pieChartData.remove(chartIndex + 1);
                        }
                    });
                }
            }
        }
    }
}
