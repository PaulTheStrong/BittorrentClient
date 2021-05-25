package by.bsuir.ksis.kursovoi.ui;

import by.bsuir.ksis.kursovoi.client.PieceManager;
import by.bsuir.ksis.kursovoi.data.Piece;
import by.bsuir.ksis.kursovoi.ui.listeners.BlockListChangeListener;
import by.bsuir.ksis.kursovoi.ui.listeners.PieceListChangeListener;

public class EventConsumer {

    private final PieceManager pieceManager;

    public EventConsumer(PieceManager pieceManager) {
        this.pieceManager = pieceManager;
        pieceManager.addHavePiecesListListener(new PieceListChangeListener("Have"));
        //pieceManager.addPendingBlockListListener(new BlockListChangeListener());
        pieceManager.addMissingPiecesListListener(new PieceListChangeListener("Missing"));
    }

}
