package me.vlink102.personal.chess.internal;

import com.neovisionaries.i18n.CountryCode;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.ChessMenu;
import me.vlink102.personal.chess.pieces.Piece;
import me.vlink102.personal.chess.pieces.SpecialPiece;
import me.vlink102.personal.chess.ui.history.CaptureGUI;

import java.awt.*;
import java.util.HashMap;

public class OnlineAssets {
    private Image savedBoard;
    private final HashMap<String, Image> savedPieces = new HashMap<>();
    private final HashMap<CaptureGUI.CaptureDisplay, Image> subImages = new HashMap<>();

    public OnlineAssets(BoardGUI boardGUI) {
        loadCachedPieces(boardGUI);
        loadCapturedPieces(boardGUI);
        updateSavedImage(boardGUI);
    }

    public Image getSavedCapturePiece(CaptureGUI.CaptureDisplay display) {
        return subImages.get(display);
    }

    public Image getSavedBoard() {
        return savedBoard;
    }

    public Image getSavedPiece(BoardGUI boardGUI, Piece piece) {
        return savedPieces.get((piece.isWhite() ? "w" : "b") + piece.getAbbr().toLowerCase()).getScaledInstance(boardGUI.getPieceSize(), boardGUI.getPieceSize(), Image.SCALE_SMOOTH);
    }

    public Image getPiece(BoardGUI boardGUI, String abbr) {
        if (abbr.matches("[wb][rnbqkp]")) {
            return Move.getResource("/themes/pieces/" + boardGUI.getPieceTheme().getLinkString() + "/" + abbr + ".png");
        } else {
            return Move.getResource("/special-pieces/" + abbr + ".png");
        }
    }

    public Image getPiece(BoardGUI boardGUI, Piece piece) {
        if (piece instanceof SpecialPiece) {
            return Move.getResource("/special-pieces/" + (piece.isWhite() ? "w" : "b") + piece.getAbbr().toLowerCase() + ".png");
        } else {
            return Move.getResource("/themes/pieces/" + boardGUI.getPieceTheme().getLinkString() + "/" + (piece.isWhite() ? "w" : "b") + piece.getAbbr().toLowerCase() + ".png");
        }
    }

    public Image getBoard(BoardGUI boardGUI) {
        return Move.getResource("/themes/boards/" + boardGUI.getBoardTheme().getLinkString() + ".png");
    }

    public synchronized void updateSavedImage(BoardGUI boardGUI) {
        savedBoard = getBoard(boardGUI).getScaledInstance(boardGUI.getPieceSize() * boardGUI.getBoardSize(), boardGUI.getPieceSize() * 8, Image.SCALE_FAST);
    }


    public void loadCapturedPieces(BoardGUI boardGUI) {
        for (CaptureGUI.CaptureDisplay value : CaptureGUI.CaptureDisplay.values()) {
            subImages.put(value, boardGUI.getCaptureGUI().getSprite(value, boardGUI.getPieceSize() / 4));
        }
    }

    public void loadCachedPieces(BoardGUI boardGUI) {
        loadPieces(boardGUI);
        loadSpecial(boardGUI);
    }

    private void loadPieces(BoardGUI boardGUI) {
        ClassroomAssets.loadDefaultPiecesToMap(savedPieces, getPiece(boardGUI, "wp"), getPiece(boardGUI, "wr"), getPiece(boardGUI, "wn"), getPiece(boardGUI, "wb"), getPiece(boardGUI, "wq"), getPiece(boardGUI, "wk"), getPiece(boardGUI, "bp"), getPiece(boardGUI, "br"), getPiece(boardGUI, "bn"), getPiece(boardGUI, "bb"), getPiece(boardGUI, "bq"), getPiece(boardGUI, "bk"));
    }

    private void loadSpecial(BoardGUI boardGUI) {
        ClassroomAssets.loadSpecialPiecesToMap(savedPieces, getPiece(boardGUI, "ba"), getPiece(boardGUI, "bc"), getPiece(boardGUI, "be"), getPiece(boardGUI, "bem"), getPiece(boardGUI, "bm"), getPiece(boardGUI, "bmi"), getPiece(boardGUI, "bpr"), getPiece(boardGUI, "bdh"), getPiece(boardGUI, "bdk"), getPiece(boardGUI, "wa"), getPiece(boardGUI, "wc"), getPiece(boardGUI, "we"), getPiece(boardGUI, "wem"), getPiece(boardGUI, "wm"), getPiece(boardGUI, "wmi"), getPiece(boardGUI, "wpr"), getPiece(boardGUI, "wdh"), getPiece(boardGUI, "wdk"));
    }

    public synchronized void updatePieceDesigns(BoardGUI boardGUI) {
        loadCachedPieces(boardGUI);
        for (Piece[] pieces : boardGUI.getGamePieces()) {
            for (Piece piece : pieces) {
                if (piece != null) {
                    piece.setIcon(getSavedPiece(boardGUI, piece));
                }
            }
        }
    }
}
