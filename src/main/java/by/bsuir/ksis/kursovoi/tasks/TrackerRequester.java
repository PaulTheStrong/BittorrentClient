package by.bsuir.ksis.kursovoi.tasks;

import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;

public class TrackerRequester implements Runnable {

    private final TorrentMetaInfo torrentMetaInfo;

    @Override
    public void run() {

    }

    public TrackerRequester(TorrentMetaInfo torrentMetaInfo) {
        this.torrentMetaInfo = torrentMetaInfo;
    }

}
