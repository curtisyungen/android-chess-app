package com.example.chessapp.pieces;

import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.chessapp.MainActivity;
import com.example.chessapp.R;
import com.example.chessapp.game.Type;
import com.example.chessapp.viewmodels.BoardViewModel;
import com.example.chessapp.viewmodels.PieceViewModel;

public class Rook extends Piece {

    private static final String TAG = "Rook";

    public Rook(String id, int row, int col, String color) {
        super(id, row, col, color);
        this.setType(Type.ROOK);
    }

    @Override
    public int setPieceImage(ImageView piece) {
        int drawable;
        if (getColor().equals("white")) {
            drawable = R.drawable.wrook;
        }
        else {
            drawable = R.drawable.brook;
        }

        piece.setImageResource(drawable);
        return drawable;
    }

    @Override
    public void setPieceId(ImageView piece) {
        int id = R.id.wr1;
        switch (this.getPieceId()) {
            case "wr1": id = R.id.wr1; break;
            case "wr2": id = R.id.wr2; break;
            case "br1": id = R.id.br1; break;
            case "br2": id = R.id.br2; break;
            default: break;
        }

        piece.setId(id);
    }

    @Override
    public boolean checkValidMove(int startRow, int startCol, int endRow, int endCol) {
        return startRow == endRow || startCol == endCol;
    }

    public void assistInKingCastle(int colsToMove) {
        BoardViewModel boardModel = MainActivity.getBoardViewModel();
        ImageView piece = (ImageView) getView().findViewById(getPieceViewId());

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) piece.getLayoutParams();
        layoutParams.leftMargin += colsToMove * 135;

        int endCol = this.getCol() + colsToMove;

        boardModel.movePiece(this.getPieceId(), this.getRow(), this.getCol(), this.getRow(), endCol);
        boardModel.setPlayerTurn(boardModel.getPlayerTurn().getValue() + 1);

        this.setCol(endCol);

        piece.setLayoutParams(layoutParams);
    }
}
