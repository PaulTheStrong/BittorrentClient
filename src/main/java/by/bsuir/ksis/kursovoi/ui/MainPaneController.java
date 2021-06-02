package by.bsuir.ksis.kursovoi.ui;

import by.bsuir.ksis.kursovoi.client.PieceManager;
import by.bsuir.ksis.kursovoi.client.TorrentClient;
import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import by.bsuir.ksis.kursovoi.data.TorrentStatus;
import by.bsuir.ksis.kursovoi.ui.listeners.TableSpeedMeasureListener;
import by.bsuir.ksis.kursovoi.ui.listeners.TableUpdateOnListChangeListener;
import by.bsuir.ksis.kursovoi.utils.BencoderParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class MainPaneController {

    private static final Logger LOGGER = Logger.getRootLogger();

    @FXML
    public Button continueButton;
    @FXML
    public Button addButton;
    @FXML
    public Button stopButton;
    @FXML
    public TextField searchField;
    @FXML
    public Button searchButton;
    @FXML
    public TableView<TableRecord> torrentsTable;
    @FXML
    public VBox parentVBox;
    @FXML
    public Button removeButton;

    private Stage primaryStage;
    private static final BencoderParser parser = new BencoderParser();

    private int currentRowIndex;

    private ObservableList<TorrentMetaInfo> torrents = FXCollections.observableArrayList();
    private List<TorrentClient> torrentClients = new ArrayList<>();

    private static final String savedTorrentsPath = "savedTorrent";

    public void initController(Stage stage) {
        this.primaryStage = stage;
        torrents.addListener(new TableUpdateOnListChangeListener(torrentsTable));
        loadTorrents(savedTorrentsPath);
    }

    public void loadTorrents(String path) {
        try {
            if (Files.exists(Path.of(path))) {
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(path));
                TorrentMetaInfo torrentMetaInfo;
                while ((torrentMetaInfo = (TorrentMetaInfo) inputStream.readObject()) != null) {
                    torrents.add(torrentMetaInfo);
                    addNewClient(torrentMetaInfo, torrentsTable.getItems().get(torrents.size() - 1));
                }
                inputStream.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn("Something went wrong during loading torrent statuses. " + e.getMessage());
        }
    }

    public void addTorrent() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                TorrentMetaInfo torrentMetaInfo = parser.parseInfoDictionary(file.getAbsolutePath());
                torrents.add(torrentMetaInfo);
                addNewClient(torrentMetaInfo, torrentsTable.getItems().get(torrents.size() - 1));
                new Thread(torrentClients.get(torrentClients.size() - 1)::start).start();
            } catch (Exception e) {
                LOGGER.warn("Something went wrong during parsing:" + e.getMessage());
            }
        }
    }


    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void close() {
        LOGGER.info("Saving torrent list");
        try (ObjectOutputStream torrentOutputStream = new ObjectOutputStream(new FileOutputStream(savedTorrentsPath))) {
            for (TorrentMetaInfo torrentMetaInfo : torrents) {
                torrentOutputStream.writeObject(torrentMetaInfo);
            }
            torrentOutputStream.writeObject(null);
        }catch (IOException e) {
            LOGGER.warn("Error closing torrent object output stream." + e.getMessage());
        }
        try {
            for (TorrentClient client : torrentClients) {
                client.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addNewClient(TorrentMetaInfo torrentMetaInfo, TableRecord tableRecord) {
        TorrentClient torrentClient = new TorrentClient(torrentMetaInfo);
        if (!torrentMetaInfo.getTorrentStatus().equals(TorrentStatus.FINISHED)) {
            PieceManager pieceManager = torrentClient.getPieceManager();
            pieceManager.addPendingBlockListListener(new TableSpeedMeasureListener(tableRecord, pieceManager, 20));
        }
        torrentClients.add(torrentClient);
    }

    public void startClients() {
        for (TorrentClient client : torrentClients) {
            new Thread(client::start).start();
        }
    }

    public void stopClient() throws Exception {
        TorrentClient client = torrentClients.get(currentRowIndex);
        client.stop();
        continueButton.setDisable(false);
        stopButton.setDisable(true);
    }

    public void startClient() {
        TorrentClient client = torrentClients.get(currentRowIndex);
        int indexOfOldClient = torrentClients.indexOf(client);
        TorrentClient newClient = new TorrentClient(torrents.get(currentRowIndex));
        new Thread(newClient::start).start();
        torrentClients.set(indexOfOldClient, newClient);
        continueButton.setDisable(true);
        stopButton.setDisable(false);

        PieceManager pieceManager = newClient.getPieceManager();
        pieceManager.addPendingBlockListListener(new TableSpeedMeasureListener(torrentsTable.getItems().get(currentRowIndex), pieceManager, 20));
    }

    public void removeClient() throws Exception {
        TorrentClient client = torrentClients.get(currentRowIndex);
        client.stop();
        torrentClients.remove(currentRowIndex);
        torrents.remove(currentRowIndex);
        torrentsTable.getItems().remove(currentRowIndex);
        continueButton.setDisable(true);
        stopButton.setDisable(true);

    }

    public void updateIndex(MouseEvent mouseEvent) {
        int selectedIndex = torrentsTable.getSelectionModel().getSelectedIndex();
        currentRowIndex = selectedIndex;
        LOGGER.info("clicked " + selectedIndex);
        TorrentMetaInfo torrentMetaInfo = torrents.get(currentRowIndex);
        if (torrentMetaInfo.getTorrentStatus().equals(TorrentStatus.STOPPED)) {
            continueButton.setDisable(false);
            stopButton.setDisable(true);
        }
        if (torrentMetaInfo.getTorrentStatus().equals(TorrentStatus.FINISHED)) {
            continueButton.setDisable(true);
            stopButton.setDisable(true);
        }
        if (torrentMetaInfo.getTorrentStatus().equals(TorrentStatus.DOWNLOADING)) {
            continueButton.setDisable(true);
            stopButton.setDisable(false);
        }

    }
}
