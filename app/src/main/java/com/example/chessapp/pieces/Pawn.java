package com.example.chessapp.pieces;

import android.util.Log;
import android.widget.ImageView;

import com.example.chessapp.MainActivity;
import com.example.chessapp.R;
import com.example.chessapp.game.Type;
import com.example.chessapp.viewmodels.BoardViewModel;

public class Pawn extends Piece {

    private static final String TAG = "Pawn";
    private int direction;
    private String mEnPassantTarget;

    public Pawn(String id, int row, int col, String color) {
        super(id, row, col, color);
        this.setType(Type.PAWN);
        this.mEnPassantTarget = "";

        if (color.equals("white")) this.direction = 1; // moving up on screen
        else this.direction = -1;
    }

    @Override
    public void setPieceId(ImageView piece) {
        int id = R.id.wp1;
        switch (this.getPieceId()) {
            case "wp2":
                id = R.id.wp2;
                break;
            case "wp3":
                id = R.id.wp3;
                break;
            case "wp4":
                id = R.id.wp4;
                break;
            case "wp5":
                id = R.id.wp5;
                break;
            case "wp6":
                id = R.id.wp6;
                break;
            case "wp7":
                id = R.id.wp7;
                break;
            case "wp8":
                id = R.id.wp8;
                break;
            case "bp1":
                id = R.id.bp1;
                break;
            case "bp2":
                id = R.id.bp2;
                break;
            case "bp3":
                id = R.id.bp3;
                break;
            case "bp4":
                id = R.id.bp4;
                break;
            case "bp5":
                id = R.id.bp5;
                break;
            case "bp6":
                id = R.id.bp6;
                break;
            case "bp7":
                id = R.id.bp7;
                break;
            case "bp8":
                id = R.id.bp8;
                break;
            default:
                break;
        }

        piece.setId(id);
    }

    @Override
    public int setPieceImage(ImageView piece) {
        int drawable;
        if (getColor().equals("white")) {
            drawable = R.drawable.wpawn;
        } else {
            drawable = R.drawable.bpawn;
        }
        piece.setImageResource(drawable);

        return drawable;
    }

    @Override
    public boolean checkValidMove(int startRow, int startCol, int endRow, int endCol) {
        BoardViewModel boardModel = MainActivity.getBoardViewModel();
        boolean isCapturing = (startRow - endRow == this.direction) && Math.abs(startCol - endCol) == 1;
        boolean landingSquareIsOccupied = boardModel.checkForPieceOnLandingSquare(endRow, endCol);

        if (landingSquareIsOccupied) return isCapturing;

        if (isCapturing && !mEnPassantTarget.equals("")) {
            boardModel.enPassant(startRow, endCol);
            this.mEnPassantTarget = "";
            return true;
        }

        if (startCol != endCol) return false;

        if (checkIfMoved()) {
            if (Math.abs(startRow - endRow) > 1) {
                return false;
            }
        }

        if (!checkIfMoved()) {
            if (startRow - endRow == this.direction * 2) {
                boardModel.enableEnPassant(getColor(), endRow, startCol);
                return true;
            }
        }

        if (startRow - endRow == this.direction) {
            return true;
        }

        return false;
    }

    public void enableEnPassant(String targetId) {
        mEnPassantTarget = targetId;
    }
}
