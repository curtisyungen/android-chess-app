package com.example.chessapp.pieces;

import android.util.Log;
import android.widget.ImageView;

import com.example.chessapp.MainActivity;
import com.example.chessapp.R;
import com.example.chessapp.game.Type;
import com.example.chessapp.viewmodels.BoardViewModel;
import com.example.chessapp.viewmodels.PieceViewModel;

public class King extends Piece {

    private static final String TAG = "King";
    private boolean mIsInCheck;

    public King(String id, int row, int col, String color) {
        super(id, row, col, color);
        this.setType(Type.KING);
        this.mIsInCheck = false;
    }

    @Override
    public void setPieceId(ImageView piece) {
        int id = R.id.wk1;
        if (this.getPieceId().equals("bk1")) {
            id = R.id.bk1;
        }

        piece.setId(id);
    }

    @Override
    public int setPieceImage(ImageView piece) {
        int drawable;
        if (getColor().equals("white")) {
            drawable = R.drawable.wking;
        }
        else {
            drawable = R.drawable.bking;
        }
        piece.setImageResource(drawable);
        return drawable;
    }

    @Override
    public boolean checkValidMove(int startRow, int startCol, int endRow, int endCol) {
        if ((startRow - endRow) == 0 && Math.abs(startCol - endCol) == 2) {
            return !mIsInCheck && !checkIfMoved() && checkIfCanCastle(startCol, endCol);
        }
        return Math.abs(startRow - endRow) <= 1 && Math.abs(startCol - endCol) <= 1;
    }

    public void setIsInCheck(boolean check) {
        mIsInCheck = check;
    }

    private boolean checkIfCanCastle(int startCol, int endCol) {
        BoardViewModel boardModel = MainActivity.getBoardViewModel();
        return boardModel.checkIfKingCanCastle(this.getPieceId(), startCol, endCol);
    }
}
