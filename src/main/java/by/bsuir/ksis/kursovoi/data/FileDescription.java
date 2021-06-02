package by.bsuir.ksis.kursovoi.data;

import java.io.Serializable;

public class FileDescription implements Serializable {

    private String path;
    private long length;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public FileDescription(String path, long length) {
        this.path = path;
        this.length = length;
    }

    @Override
    public String toString() {
        return "FileDescription{" +
                "name='" + path + '\'' +
                ", length=" + length +
                '}';
    }
}
