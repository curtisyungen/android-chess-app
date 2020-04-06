package com.example.chessapp.pieces;

import android.widget.ImageView;

import com.example.chessapp.R;
import com.example.chessapp.game.Type;

public class Queen extends Piece {

    private static final String TAG = "Queen";

    public Queen(String id, int row, int col, String color) {
        super(id, row, col, color);
        this.setType(Type.QUEEN);
    }

    @Override
    public void setPieceId(ImageView piece) {
        int id = R.id.wq1;
        switch (this.getPieceId()) {
            case "wq1": id = R.id.wq1; break;
            case "wq2": id = R.id.wq2; break;
            case "bq1": id = R.id.bq1; break;
            case "bq2": id = R.id.bq2; break;
            default: break;
        }

        piece.setId(id);
    }

    @Override
    public int setPieceImage(ImageView piece) {
        int drawable;
        if (getColor().equals("white")) {
            drawable = R.drawable.wqueen;
        }
        else {
            drawable = R.drawable.bqueen;
        }
        piece.setImageResource(drawable);
        return drawable;
    }

    @Override
    public boolean checkValidMove(int startRow, int startCol, int endRow, int endCol) {
        if (startRow == endRow || startCol == endCol) {
            return true;
        }

        return Math.abs(startRow - endRow) == Math.abs(startCol - endCol);
    }
}
