package by.bsuir.ksis.kursovoi.client;

import by.bsuir.ksis.kursovoi.data.FileDescription;
import by.bsuir.ksis.kursovoi.data.TorrentMetaInfo;
import by.bsuir.ksis.kursovoi.utils.BencoderParser;
import lombok.SneakyThrows;

import java.io.*;
import java.util.List;

public class FileSplitter {

    private final TorrentMetaInfo metaInfo;
    private final String fileToSplit;

    public FileSplitter(TorrentMetaInfo metaInfo, String fileToSplit) {
        this.metaInfo = metaInfo;
        this.fileToSplit = fileToSplit;
    }

    public void splitIntoFiles() throws IOException {
        List<FileDescription> files = metaInfo.getFiles();
        try (FileInputStream is = new FileInputStream("H:/downloads/" + fileToSplit)) {
            if (files.size() == 1) {
                return;
            }
            int bufferSize = 1 << 20; // 64KB
            byte[] buffer = new byte[bufferSize];
            for (FileDescription description : files) {
                File writeFile = new File("H:/downloads/files/" + metaInfo.getName() + description.getPath());
                File parent = writeFile.getParentFile();
                boolean mkdirs = parent.mkdirs();
                try (FileOutputStream outputStream = new FileOutputStream(writeFile)) {
                    long length = description.getLength();
                    long totalRead = 0;
                    for (long index = 0; index < length; index += bufferSize) {
                        int bytesToRead = (int) Math.min(length - totalRead, bufferSize);
                        if (is.read(buffer, 0, bytesToRead) != bytesToRead) {
                            throw new IOException("Not enough bytes read");
                        }
                        totalRead += bytesToRead;
                        outputStream.write(buffer, 0, bytesToRead);
                    }
                }
            }
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        BencoderParser parser = new BencoderParser();
        TorrentMetaInfo metaInfo = parser.parseInfoDictionary("src/main/resources/torrent.torrent");
        FileSplitter fileSplitter = new FileSplitter(metaInfo, "Twenty One Pilots - Level of Concern (Single) 2020");
        fileSplitter.splitIntoFiles();
    }
}


