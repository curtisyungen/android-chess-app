package com.example.chessapp.pieces;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.chessapp.MainActivity;
import com.example.chessapp.game.Type;
import com.example.chessapp.viewmodels.BoardViewModel;

public abstract class Piece extends Fragment {

    private static final String TAG = "Piece";

    private ImageView piece;
    private int mPieceViewId;
    private int mPieceDrawable;

    private String mId;
    private int mRow;
    private int mCol;
    private String mColor;
    private Type mType;
    private boolean mCaptured;
    private boolean mHasMoved;

    public Piece(String id, int row, int col, String color) {
        this.mId = id;
        this.mRow = row;
        this.mCol = col;
        this.mCaptured = false;
        this.mHasMoved = false;
        this.mColor = color;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called.");

        piece = new ImageView(getContext());

        mPieceDrawable = setPieceImage(piece);
        setPieceId(piece);

        int width = MainActivity.screenwidth / 8;
        int height = MainActivity.screenwidth / 8;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        layoutParams.leftMargin = (getCol() - 1) * width;
        layoutParams.topMargin = (getRow() - 1) * height;
        piece.setLayoutParams(layoutParams);

        this.mPieceViewId = piece.getId();

        setOnTouchListener(piece);

        return piece;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchListener(final ImageView piece) {
        piece.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE && !mCaptured) {
                    int x_cord = (int) motionEvent.getRawX();
                    int y_cord = (int) motionEvent.getRawY();

                    onDrag(piece, x_cord, y_cord);
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_UP && !mCaptured) {
                    onDrop(piece);
                }

                return !mCaptured;
            }
        });
    }

    private void onDrag(ImageView piece, int x_cord, int y_cord) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) piece.getLayoutParams();

        if (x_cord > MainActivity.screenwidth) {
            x_cord = MainActivity.screenwidth;
        }

        if (y_cord < 680) {
            y_cord = 680;
        }

        if (y_cord > 1620) {
            y_cord = 1620;
        }

        layoutParams.leftMargin = x_cord - 60;
        layoutParams.topMargin = y_cord - 680;

        piece.setLayoutParams(layoutParams);
    }

    private void onDrop(ImageView piece) {
        BoardViewModel boardModel = MainActivity.getBoardViewModel();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) piece.getLayoutParams();

        int x = layoutParams.leftMargin;
        int y = layoutParams.topMargin;

        int endRow = (int)((y + 67.5) / 135) + 1;
        int endCol = (int)((x + 67.5) / 135) + 1;

        boolean isPlayerTurn = MainActivity.getPlayerTurn().equals(mColor);
        boolean validMove = checkValidMove(mRow, mCol, endRow, endCol);
        boolean validSquare = boardModel.checkValidSquare(mId, endRow, endCol);
        boolean validPath = boardModel.checkValidPath(mType, mColor, mRow, mCol, endRow, endCol);
        boolean ownKingNotExposed = boardModel.ensureOwnKingNotExposed(mId, mRow, mCol, endRow, endCol);

        if (!isPlayerTurn) Log.d("VALIDATION", "Not player's turn.");
        if (!validMove) Log.d("VALIDATION", "Invalid move.");
        if (!validSquare) Log.d("VALIDATION", "Not valid square.");
        if (!validPath) Log.d("VALIDATION", "Not valid path.");
        if (!ownKingNotExposed) Log.d("VALIDATION", "Own king exposed.");

        if (isPlayerTurn && validMove && validSquare && validPath && ownKingNotExposed) {
            // Snap to target square
            layoutParams.leftMargin = (int)((x + 67.5) / 135) * 135;
            layoutParams.topMargin = (int)((y + 67.5) / 135) * 135;

            if (getPieceId().equals("wk1")) boardModel.setWhiteKingPosition(endRow, endCol);
            if (getPieceId().equals("bk1")) boardModel.setBlackKingPosition(endRow, endCol);

            boardModel.movePiece(mId, mRow, mCol, endRow, endCol);

            // Update piece location
            mRow = endRow;
            mCol = endCol;

            mHasMoved = true;
        }
        else {
            // Snap back to starting square
            layoutParams.leftMargin = (mCol - 1) * 135;
            layoutParams.topMargin = (mRow - 1) * 135;
        }

        piece.setLayoutParams(layoutParams);
    }

    public abstract boolean checkValidMove(int startRow, int startCol, int endRow, int endCol);

    public boolean checkIfMoved() {
        return mHasMoved;
    }

    public String getPieceId() {
        return mId;
    }

    public int getPieceViewId() { return mPieceViewId; }

    public abstract void setPieceId(ImageView piece);

    public int getPieceImage() { return mPieceDrawable; }

    public abstract int setPieceImage(ImageView piece);

    public void setPieceAsCaptured() {
        this.mCaptured = true;
    }

    public int getRow() {
        return mRow;
    }

    public int getCol() {
        return mCol;
    }

    public Type getType() {
        return mType;
    }

    public String getColor() {
        return mColor;
    }

    public void setCol(int col) {
        this.mCol = col;
    }

    public void setType(Type type) {
        this.mType = type;
    }
}
