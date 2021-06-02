package by.bsuir.ksis.kursovoi.ui;

import by.bsuir.ksis.kursovoi.data.TorrentStatus;
import javafx.beans.property.*;

public class TableRecord {

    private IntegerProperty id = new SimpleIntegerProperty();
    private SimpleStringProperty name = new SimpleStringProperty();
    private SimpleStringProperty size = new SimpleStringProperty();
    private ObjectProperty<TorrentStatus> status = new SimpleObjectProperty<>();
    private SimpleStringProperty downloadSpeed = new SimpleStringProperty();
    private SimpleStringProperty time = new SimpleStringProperty();
    private SimpleStringProperty downloaded = new SimpleStringProperty();

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getSize() {
        return size.get();
    }

    public StringProperty sizeProperty() {
        return size;
    }

    public void setSize(String size) {
        this.size.set(size);
    }

    public TorrentStatus getStatus() {
        return status.get();
    }

    public ObjectProperty<TorrentStatus> statusProperty() {
        return status;
    }

    public void setStatus(TorrentStatus status) {
        this.status.set(status);
    }

    public void setStatusProperty(ObjectProperty<TorrentStatus> statusProperty) {
        this.status = statusProperty;
    }

    public String getDownloadSpeed() {
        return downloadSpeed.get();
    }

    public StringProperty downloadSpeedProperty() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(String downloadSpeed) {
        this.downloadSpeed.set(downloadSpeed);
    }

    public String getTime() {
        return time.get();
    }

    public StringProperty timeProperty() {
        return time;
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public String getDownloaded() {
        return downloaded.get();
    }

    public SimpleStringProperty downloadedProperty() {
        return downloaded;
    }

    public void setDownloaded(String downloaded) {
        this.downloaded.set(downloaded);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }
}
