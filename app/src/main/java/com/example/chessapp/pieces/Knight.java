package com.example.chessapp.pieces;

import android.widget.ImageView;

import com.example.chessapp.R;
import com.example.chessapp.game.Type;

public class Knight extends Piece {

    private static final String TAG = "Knight";

    public Knight(String id, int row, int col, String color) {
        super(id, row, col, color);
        this.setType(Type.KNIGHT);
    }

    @Override
    public void setPieceId(ImageView piece) {
        int id = R.id.wn1;
        switch (this.getPieceId()) {
            case "wn2": id = R.id.wn2; break;
            case "bn1": id = R.id.bn1; break;
            case "bn2": id = R.id.bn2; break;
            default: break;
        }

        piece.setId(id);
    }

    @Override
    public int setPieceImage(ImageView piece) {
        int drawable;
        if (getColor() == "white") {
            drawable = R.drawable.wknight;
        } else {
            drawable = R.drawable.bknight;
        }
        piece.setImageResource(drawable);
        return drawable;
    }

    @Override
    public boolean checkValidMove(int startRow, int startCol, int endRow, int endCol) {

        if (Math.abs(endRow - startRow) == 2 && Math.abs(endCol - startCol) == 1) {
            return true;
        }

        return Math.abs(endCol - startCol) == 2 && Math.abs(endRow - startRow) == 1;
    }
}
