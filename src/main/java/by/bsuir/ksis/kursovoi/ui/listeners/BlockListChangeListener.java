package by.bsuir.ksis.kursovoi.ui.listeners;

import by.bsuir.ksis.kursovoi.data.Block;
import javafx.collections.ListChangeListener;
import org.apache.log4j.Logger;

public class BlockListChangeListener implements ListChangeListener<Block> {

    private static final Logger LOGGER = Logger.getRootLogger();

    @Override
    public void onChanged(Change<? extends Block> change) {
        LOGGER.info(change);
    }
}
