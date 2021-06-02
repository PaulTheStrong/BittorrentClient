package by.bsuir.ksis.kursovoi.ui.listeners;

import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import by.bsuir.ksis.kursovoi.ui.TableRecord;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableView;

import java.util.List;

public class TableUpdateOnListChangeListener implements ListChangeListener<TorrentMetaInfo> {

    private static int id = 0;

    private TableView<TableRecord> table;

    public TableUpdateOnListChangeListener(TableView<TableRecord> table) {
        this.table = table;
    }

    @Override
    public void onChanged(Change change) {
        while (change.next()) {
            if (change.wasAdded()) {
                List<TorrentMetaInfo> addedSubList = change.getAddedSubList();
                for (TorrentMetaInfo torrentMetaInfo : addedSubList) {
                    TableRecord tableRecord = new TableRecord();
                    String name = torrentMetaInfo.getName();
                    tableRecord.setName(name/*.substring(name.lastIndexOf("/"))*/);
                    tableRecord.setStatusProperty(torrentMetaInfo.torrentStatusProperty());
                    tableRecord.setTime("0 Seconds");
                    tableRecord.setDownloadSpeed("0 Bytes/s");
                    tableRecord.setDownloaded("0 bytes");
                    setSize(torrentMetaInfo, tableRecord);
                    tableRecord.setId(id++);
                    table.getItems().add(tableRecord);
                }
            }
        }
    }

    private void setSize(TorrentMetaInfo torrentMetaInfo, TableRecord tableRecord) {
        double size = (double) torrentMetaInfo.getTotalSize();
        String measure = "Bytes";
        if (size > 1024) {
            measure = "KB";
            size /= 1024;
        }
        if (size > 1024) {
            measure = "MB";
            size /= 1024;
        }
        if (size > 1024) {
            measure = "GB";
            size /= 1024;
        }
        tableRecord.setSize(String.format("%.2f %s", size, measure));
    }
}
