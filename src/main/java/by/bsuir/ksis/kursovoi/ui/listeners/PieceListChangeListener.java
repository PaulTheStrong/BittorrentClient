package by.bsuir.ksis.kursovoi.ui.listeners;

import by.bsuir.ksis.kursovoi.data.Piece;
import javafx.collections.ListChangeListener;
import org.apache.log4j.Logger;

public class PieceListChangeListener implements ListChangeListener<Piece> {

    private static final Logger LOGGER = Logger.getRootLogger();
    private final String listName;

    public PieceListChangeListener(String listName) {
        this.listName = listName;
    }

    @Override
    public void onChanged(Change<? extends Piece> change) {
        LOGGER.info(listName + " : " + change);
    }

}
