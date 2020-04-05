package com.example.chessapp.pieces;

import android.widget.ImageView;

import com.example.chessapp.R;
import com.example.chessapp.game.Type;

public class Bishop extends Piece {

    private static final String TAG = "Bishop";

    public Bishop(String id, int row, int col, String color) {
        super(id, row, col, color);
        this.setType(Type.BISHOP);
    }

    @Override
    public void setPieceId(ImageView piece) {
        int id = R.id.wb1;
        switch (this.getPieceId()) {
            case "wb2": id = R.id.wb2; break;
            case "bb1": id = R.id.bb1; break;
            case "bb2": id = R.id.bb2; break;
            default: break;
        }

        piece.setId(id);
    }

    @Override
    public int setPieceImage(ImageView piece) {
        int drawable;
        if (getColor() == "white") {
            drawable = R.drawable.wbishop;
        }
        else {
            drawable = R.drawable.bbishop;
        }
        piece.setImageResource(drawable);
        return drawable;
    }

    @Override
    public boolean checkValidMove(int startRow, int startCol, int endRow, int endCol) {
        return Math.abs(endRow - startRow) == Math.abs(endCol - startCol);
    }
}
