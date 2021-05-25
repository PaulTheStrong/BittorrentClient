package by.bsuir.ksis.kursovoi.client.callbacks;

import by.bsuir.ksis.kursovoi.client.TorrentClient;

import java.io.IOException;

public class OnBlockRetrievedCallBackFunction implements ClientCallBackFunction {

    private final TorrentClient client;

    @Override
    public void callBack(Object... params) throws IOException {
        client.onBlockRetrieved((String) params[0], (Integer) params[1], (Integer) params[2], (byte[]) params[3]);
    }

    public OnBlockRetrievedCallBackFunction(TorrentClient torrentClient) {
        this.client = torrentClient;
    }
}
