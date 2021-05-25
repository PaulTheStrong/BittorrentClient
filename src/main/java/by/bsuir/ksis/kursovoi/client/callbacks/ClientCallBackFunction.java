package by.bsuir.ksis.kursovoi.client.callbacks;

import by.bsuir.ksis.kursovoi.client.TorrentClient;

import java.io.IOException;

@FunctionalInterface
public interface ClientCallBackFunction {
    void callBack(Object... params) throws IOException;
}
