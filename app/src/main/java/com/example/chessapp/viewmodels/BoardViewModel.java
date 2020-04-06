package com.example.chessapp.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chessapp.MainActivity;
import com.example.chessapp.game.Type;
import com.example.chessapp.pieces.Pawn;
import com.example.chessapp.pieces.Piece;
import com.example.chessapp.pieces.Rook;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class BoardViewModel extends ViewModel {
    private static final String TAG = "BoardViewModel";

    private MutableLiveData<String[]> mBoardArray = new MutableLiveData<>();
    private MutableLiveData<Integer> mPlayerTurn = new MutableLiveData<>(1);
    private MutableLiveData<ArrayList<String>> mCapturedWhite = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> mCapturedBlack = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Integer>> mWhiteKingPosition = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Integer>> mBlackKingPosition = new MutableLiveData<>();
    private MutableLiveData<String> mWhiteKingCheck = new MutableLiveData<>();
    private MutableLiveData<String> mBlackKingCheck = new MutableLiveData<>();

    public void resetGame() {
        mBoardArray.setValue(new String[64]);
        mPlayerTurn.setValue(1);
        mCapturedWhite.setValue(new ArrayList<String>());
        mCapturedBlack.setValue(new ArrayList<String>());
        mWhiteKingPosition.setValue(new ArrayList<Integer>());
        mBlackKingPosition.setValue(new ArrayList<Integer>());
        mWhiteKingCheck.setValue("");
        mBlackKingCheck.setValue("");
        initBoardArray();
    }

    public void initBoardArray() {
        Log.d(TAG, "initBoardArray: called.");

        String[] boardArray = new String[]{
                "br1", "bn1", "bb1", "bq1", "bk1", "bb2", "bn2", "br2",
                "bp1", "bp2", "bp3", "bp4", "bp5", "bp6", "bp7", "bp8",
                "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "",
                "wp1", "wp2", "wp3", "wp4", "wp5", "wp6", "wp7", "wp8",
                "wr1", "wn1", "wb1", "wq1", "wk1", "wb2", "wn2", "wr2"
        };

        mBoardArray.setValue(boardArray);
    }

    public LiveData<String[]> getBoardArray() {
        return mBoardArray;
    }

    public LiveData<ArrayList<String>> getCapturedWhite() {
        return mCapturedWhite;
    }

    public LiveData<ArrayList<String>> getCapturedBlack() {
        return mCapturedBlack;
    }

    public LiveData<Integer> getPlayerTurn() {
        return mPlayerTurn;
    }

    public LiveData<ArrayList<Integer>> getWhiteKingPosition() {
        return mWhiteKingPosition;
    }

    public LiveData<ArrayList<Integer>> getBlackKingPosition() {
        return mBlackKingPosition;
    }

    public LiveData<String> getWhiteKingCheck() {
        return mWhiteKingCheck;
    }

    public LiveData<String> getBlackKingCheck() {
        return mBlackKingCheck;
    }

    public void setWhiteKingPosition(int row, int col) {
        ArrayList<Integer> position = new ArrayList<>();
        position.add(row);
        position.add(col);
        mWhiteKingPosition.setValue(position);
    }

    public void setBlackKingPosition(int row, int col) {
        ArrayList<Integer> position = new ArrayList<>();
        position.add(row);
        position.add(col);
        mBlackKingPosition.setValue(position);
    }

    public void setPlayerTurn(Integer player) {
        mPlayerTurn.setValue(player);
    }

    public int getIdxInBoardArray(int row, int col) {
        return (row - 1) * 8 + col - 1;
    }

    private int[] getRowColFromBoardArrayIdx(int idx) {
        float temp = ((float) idx / 64) * 8 + 1;
        int row = (int) Math.floor(temp);
        int col = (int) ((temp - row) * 8) + 1;
        return new int[]{row, col};
    }

    public boolean checkValidSquare(String pieceId, int endRow, int endCol) {

        if (endRow < 1 || endRow > 8 || endCol < 1 || endCol > 8) return false;

        String[] boardArray = mBoardArray.getValue();

        assert boardArray != null;
        String targetPieceId = boardArray[getIdxInBoardArray(endRow, endCol)];

        // Cannot capture piece of same color or a king
        if (!targetPieceId.equals("")) {
            return targetPieceId.charAt(0) != pieceId.charAt(0) && targetPieceId.charAt(1) != 'k';
        }

        return true;
    }

    public boolean checkForPieceOnLandingSquare(int row, int col) {
        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;
        return !boardArray[getIdxInBoardArray(row, col)].equals("");
    }

    /**
     * Verifies there are no other pieces obstructing a row, column, or diagonal move.
     * Calls checkDiagonalMove() and checkRowOrColMove()
     * Returns true if path is valid
     */
    public boolean checkValidPath(Type type, String mColor, int startRow, int startCol, int endRow, int endCol) {
        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;

        if (type == Type.BISHOP) {
            return checkDiagonalMove(startRow, startCol, endRow, endCol);
        }

        if (type == Type.ROOK) {
            return checkRowOrColMove(startRow, startCol, endRow, endCol);
        }

        if (type == Type.QUEEN) {
            if (startRow != endRow && startCol != endCol) {
                return checkDiagonalMove(startRow, startCol, endRow, endCol);
            } else {
                return checkRowOrColMove(startRow, startCol, endRow, endCol);
            }
        }

        if (type == Type.PAWN) {
            return checkRowOrColMove(startRow, startCol, endRow, endCol);
        }

        if (type == Type.KING) {
            return checkForChecks(boardArray, mColor.charAt(0), endRow, endCol).equals("");
        }

        return true;
    }

    /**
     * Verifies that there are no pieces obstructing the path of subject piece
     * Applies to movements along diagonals
     * Returns true if diagonal path is valid
     */
    private boolean checkDiagonalMove(int startRow, int startCol, int endRow, int endCol) {
        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;

        String square;
        int row, col;
        int rowDir = endRow > startRow ? 1 : -1;
        int colDir = endCol > startCol ? 1 : -1;

        for (int i = 1; i < Math.abs(startRow - endRow); i++) {
            row = startRow + i * rowDir;
            col = startCol + i * colDir;

            square = boardArray[getIdxInBoardArray(row, col)];
            if (!square.equals("")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Verifies that there are no pieces obstructing the path of subject piece
     * Applies to movements along rows or columns
     * Returns true if row or col move is valid
     */
    private boolean checkRowOrColMove(int startRow, int startCol, int endRow, int endCol) {
        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;

        String square;
        int row, col;
        int rowDir = endRow > startRow ? 1 : -1;
        int colDir = endCol > startCol ? 1 : -1;

        // Check row
        for (int i = 1; i < Math.abs(startRow - endRow); i++) {
            row = startRow + i * rowDir;

            square = boardArray[getIdxInBoardArray(row, startCol)];
            if (!square.equals("")) {
                return false;
            }
        }

        // Check column
        for (int i = 1; i < Math.abs(startCol - endCol); i++) {
            col = startCol + i * colDir;

            square = boardArray[getIdxInBoardArray(startRow, col)];
            if (!square.equals("")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks for pieces threatening the king
     * Looks down rows, columns, diagonals, and on possible knight and pawn attacking squares
     * Returns true if a check is found
     */
    private String checkForChecks(String[] boardArray, Character kingColor, Integer kRow, Integer kCol) {

        if (kRow < 1 || kRow > 8 || kCol < 1 || kCol > 8) return "range";

        // Check for threats from rooks and queens
        String pieceAt;
        for (int c = 1; c <= 8; c++) {
            // Check right side of king's row
            if (kCol + c <= 8) {
                pieceAt = boardArray[getIdxInBoardArray(kRow, kCol + c)];
                if (!pieceAt.equals("")) {
                    if (pieceAt.charAt(0) != kingColor && (pieceAt.charAt(1) == 'r' || pieceAt.charAt(1) == 'q'))
                        return pieceAt;
                    else break;
                }
            }
        }

        for (int c = 1; c <= 8; c++) {
            // Check left side of king's row
            if (kCol - c >= 1) {
                pieceAt = boardArray[getIdxInBoardArray(kRow, kCol - c)];
                if (!pieceAt.equals("")) {

                    if (pieceAt.charAt(0) != kingColor && (pieceAt.charAt(1) == 'r' || pieceAt.charAt(1) == 'q')){
                        Log.d("CHECKMATE", String.format("kColor %s, pieceAt %s", kingColor, pieceAt));
                        return pieceAt;}
                    else break;
                }
            }
        }


        for (int c = 1; c <= 8; c++) {
            // Check top side of king's column
            if (kRow + c <= 8) {
                pieceAt = boardArray[getIdxInBoardArray(kRow + c, kCol)];
                if (!pieceAt.equals("")) {
                    if (pieceAt.charAt(0) != kingColor && (pieceAt.charAt(1) == 'r' || pieceAt.charAt(1) == 'q'))
                        return pieceAt;
                    else break;
                }
            }
        }

        for (int c = 1; c <= 8; c++) {
            // Check bottom side of king's column
            if (kRow - c >= 1) {
                pieceAt = boardArray[getIdxInBoardArray(kRow - c, kCol)];
                if (!pieceAt.equals("")) {
                    if (pieceAt.charAt(0) != kingColor && (pieceAt.charAt(1) == 'r' || pieceAt.charAt(1) == 'q'))
                        return pieceAt;
                    else break;
                }
            }
        }

        // Check for threats from bishops, queens
        for (int i = 1; i <= 8; i++) {

            // Check right descending diagonal
            if (kRow + i <= 8 && kCol + i <= 8) {
                pieceAt = boardArray[getIdxInBoardArray(kRow + i, kCol + i)];

                if (!pieceAt.equals("")) {
                    if (pieceAt.charAt(0) != kingColor && (pieceAt.charAt(1) == 'b' || pieceAt.charAt(1) == 'q'))
                        return pieceAt;
                    else break;
                }
            }
        }

        for (int i = 1; i <= 8; i++) {
            // Check left ascending diagonal
            if (kRow - i >= 1 && kCol - i >= 1) {
                pieceAt = boardArray[getIdxInBoardArray(kRow - i, kCol - i)];

                if (!pieceAt.equals("")) {
                    if (pieceAt.charAt(0) != kingColor && (pieceAt.charAt(1) == 'b' || pieceAt.charAt(1) == 'q'))
                        return pieceAt;
                    else break;
                }
            }
        }

        for (int i = 1; i <= 8; i++) {
            // Check left descending diagonal
            if (kRow + i <= 8 && kCol - i >= 1) {
                pieceAt = boardArray[getIdxInBoardArray(kRow + i, kCol - i)];

                if (!pieceAt.equals("")) {
                    if (pieceAt.charAt(0) != kingColor && (pieceAt.charAt(1) == 'b' || pieceAt.charAt(1) == 'q'))
                        return pieceAt;
                    else break;
                }
            }
        }

        for (int i = 1; i <= 8; i++) {
            // Check right ascending diagonal
            if (kRow - i >= 1 && kCol + i <= 8) {
                pieceAt = boardArray[getIdxInBoardArray(kRow - i, kCol + i)];

                if (!pieceAt.equals("")) {
                    if (pieceAt.charAt(0) != kingColor && (pieceAt.charAt(1) == 'b' || pieceAt.charAt(1) == 'q'))
                        return pieceAt;
                    else break;
                }
            }
        }

        // Check for threats from knights
        ArrayList<Integer[]> cords = new ArrayList<>();
        cords.add(new Integer[]{kRow + 2, kCol + 1});
        cords.add(new Integer[]{kRow + 2, kCol - 1});
        cords.add(new Integer[]{kRow + 1, kCol + 2});
        cords.add(new Integer[]{kRow + 1, kCol - 2});
        cords.add(new Integer[]{kRow - 2, kCol + 1});
        cords.add(new Integer[]{kRow - 2, kCol - 1});
        cords.add(new Integer[]{kRow - 1, kCol + 2});
        cords.add(new Integer[]{kRow - 1, kCol - 2});

        for (int c = 0; c < cords.size(); c++) {
            if (cords.get(c)[0] >= 1 && cords.get(c)[0] <= 8 && cords.get(c)[1] >= 1 && cords.get(c)[1] <= 8) {
                pieceAt = boardArray[getIdxInBoardArray(cords.get(c)[0], cords.get(c)[1])];
                if (!pieceAt.equals("")) {
                    if (pieceAt.charAt(0) != kingColor && pieceAt.charAt(1) == 'n') return pieceAt;
                }
            }
        }

        // Check for threats from pawns
        if (kingColor == 'b') {
            if (kRow + 1 <= 8) {
                if (kCol + 1 <= 8) {
                    pieceAt = boardArray[getIdxInBoardArray(kRow + 1, kCol + 1)];
                    if (!pieceAt.equals("")) {
                        if (pieceAt.charAt(0) != kingColor && pieceAt.charAt(1) == 'p')
                            return pieceAt;
                    }
                }

                if (kCol - 1 >= 1) {
                    pieceAt = boardArray[getIdxInBoardArray(kRow + 1, kCol - 1)];
                    if (!pieceAt.equals("")) {
                        if (pieceAt.charAt(0) != kingColor && pieceAt.charAt(1) == 'p')
                            return pieceAt;
                    }
                }
            }
        }

        else if (kingColor == 'w') {
            if (kRow - 1 >= 1) {
                if (kCol + 1 <= 8) {
                    pieceAt = boardArray[getIdxInBoardArray(kRow - 1, kCol + 1)];
                    if (!pieceAt.equals("")) {
                        if (pieceAt.charAt(0) != kingColor && pieceAt.charAt(1) == 'p')
                            return pieceAt;
                    }
                }

                if (kCol - 1 >= 1) {
                    pieceAt = boardArray[getIdxInBoardArray(kRow - 1, kCol - 1)];
                    if (!pieceAt.equals("")) {
                        if (pieceAt.charAt(0) != kingColor && pieceAt.charAt(1) == 'p')
                            return pieceAt;
                    }
                }
            }
        }

        return "";
    }

    private boolean checkIfPieceCanCaptureAttacker(Character kingColor, int attackingRow, int attackingCol, PieceViewModel pieceModel) {
        ArrayList<Integer> wKingCords = mWhiteKingPosition.getValue();
        ArrayList<Integer> bKingCords = mBlackKingPosition.getValue();

        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;

        // Can any of king's pieces land on attacking square?
        String pieceAt;
        Type pieceType;

        for (int i = 0; i < boardArray.length; i++) {
            pieceAt = boardArray[i];

            if (!pieceAt.equals("") && pieceAt.charAt(0) == kingColor && pieceAt.charAt(1) != 'k') {

                switch (pieceAt.charAt(1)) {
                    case 'p':
                        pieceType = Type.PAWN;
                        break;
                    case 'r':
                        pieceType = Type.ROOK;
                        break;
                    case 'n':
                        pieceType = Type.KNIGHT;
                        break;
                    case 'q':
                        pieceType = Type.QUEEN;
                        break;
                    case 'b':
                        pieceType = Type.BISHOP;
                        break;
                    default:
                        pieceType = null;
                }

                int startRow = getRowColFromBoardArrayIdx(i)[0];
                int startCol = getRowColFromBoardArrayIdx(i)[1];

                String kingColorStr = kingColor == 'w' ? "white" : "black";

                boolean validPath = checkValidPath(pieceType, kingColorStr, startRow, startCol, attackingRow, attackingCol);

                Piece defendingPiece = pieceModel.getPieceById(pieceAt);
                boolean validMove = defendingPiece.checkValidMove(startRow, startCol, attackingRow, attackingCol);

                if (validPath && validMove) {
                    Log.d("CHECKMATE", String.format("%s can capture.", pieceAt));
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkIfPieceCanBlockAttack(Character kingColor, int kRow, int kCol, Piece attackingPiece, int attackingRow, int attackingCol, PieceViewModel pieceModel) {
        if (attackingPiece.getType() == Type.KNIGHT || attackingPiece.getType() == Type.PAWN)
            return false;

        // Find path between king and attacking piece
        int rowDiff = Math.abs(kRow - attackingRow);
        int colDiff = Math.abs(kCol - attackingCol);

        if (Math.abs(rowDiff) == 1 && Math.abs(colDiff) == 1) return false;

        int tRow = 0;
        int tCol = 0;

        int rDirection = 1; // down
        if (rowDiff != 0) {
            if (kRow > attackingRow) rDirection = -1;
        }

        int cDirection = 1; // right
        if (colDiff != 0) {
            if (kCol > attackingCol) cDirection = -1;
        }

        ArrayList<int[]> path = new ArrayList<>();

        if (attackingPiece.getType() == Type.QUEEN || attackingPiece.getType() == Type.ROOK) {
            if (rowDiff == 0) {
                for (int i = 1; i < colDiff; i++) {
                    tCol = kCol + (i * cDirection);
                    path.add(new int[]{kRow, tCol});
                }
            } else if (colDiff == 0) {
                for (int i = 1; i < rowDiff; i++) {
                    tRow = kRow + (i * rDirection);
                    path.add(new int[]{tRow, kCol});
                }
            }
        }

        if (attackingPiece.getType() == Type.BISHOP || (attackingPiece.getType() == Type.QUEEN && (rowDiff != 0 && colDiff != 0))) {
            // calculate diagonal path
            for (int i=1; i<rowDiff; i++) {
                tRow = kRow + (i * rDirection);
                tCol = kCol + (i * cDirection);
                path.add(new int[]{tRow, tCol});
            }
        }

        // Loop through each square in path to see if piece can move there
        for (int i = 0; i < path.size(); i++) {
            Log.d("CHECKMATE", String.format("path row %s, path col %s", path.get(i)[0], path.get(i)[1]));
            if (checkIfPieceCanCaptureAttacker(kingColor, path.get(i)[0], path.get(i)[1], pieceModel)) return true;
        }

        return false;
    }

    public boolean checkForCheckmate(Character kingColor, Piece attackingPiece, PieceViewModel pieceModel) {
        ArrayList<Integer> wKingCords = mWhiteKingPosition.getValue();
        ArrayList<Integer> bKingCords = mBlackKingPosition.getValue();

        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;

        Integer kRow, kCol;

        if (kingColor == 'w') {
            kRow = wKingCords.get(0);
            kCol = wKingCords.get(1);
        } else {
            kRow = bKingCords.get(0);
            kCol = bKingCords.get(1);
        }

        ArrayList<Integer[]> possMoves = new ArrayList<Integer[]>();
        possMoves.add(new Integer[]{kRow + 1, kCol});
        possMoves.add(new Integer[]{kRow + 1, kCol + 1});
        possMoves.add(new Integer[]{kRow + 1, kCol - 1});
        possMoves.add(new Integer[]{kRow, kCol + 1});
        possMoves.add(new Integer[]{kRow, kCol - 1});
        possMoves.add(new Integer[]{kRow - 1, kCol});
        possMoves.add(new Integer[]{kRow - 1, kCol + 1});
        possMoves.add(new Integer[]{kRow - 1, kCol - 1});

        boolean kingCanMove = false;
        boolean validKingMove;

        int square = 0;

        Integer targetRow;
        Integer targetCol;

        while (!kingCanMove && square < possMoves.size()) {
            validKingMove = false;

            targetRow = possMoves.get(square)[0];
            targetCol = possMoves.get(square)[1];

            validKingMove = checkValidSquare(String.format("%sk1", kingColor), targetRow, targetCol);

            if (validKingMove) {
                kingCanMove = ensureOwnKingNotExposed(String.format("%sk1", kingColor), kRow, kCol, targetRow, targetCol);
            }

            square += 1;
        }

        if (kingCanMove) return false;

        int attackingRow = 0, attackingCol = 0;
        for (int i = 0; i < boardArray.length; i++) {
            if (boardArray[i].equals(attackingPiece.getPieceId())) {
                attackingRow = getRowColFromBoardArrayIdx(i)[0];
                attackingCol = getRowColFromBoardArrayIdx(i)[1];
            }
        }

        if (checkIfPieceCanCaptureAttacker(kingColor, attackingRow, attackingCol, pieceModel))
            return false;
        return !checkIfPieceCanBlockAttack(kingColor, kRow, kCol, attackingPiece, attackingRow, attackingCol, pieceModel);
    }

    /**
     * Tests proposed move to make sure player's own king is not exposed to a check
     * Returns true if no check found; false if check found
     */
    public boolean ensureOwnKingNotExposed(String pieceId, int startRow, int startCol, int endRow, int endCol) {
        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;

        int startIdx = getIdxInBoardArray(startRow, startCol);
        int endIdx = getIdxInBoardArray(endRow, endCol);

        String origPiece = boardArray[endIdx];

        Array.set(boardArray, startIdx, "");
        Array.set(boardArray, endIdx, pieceId);

        String kingId = String.format("%sk1", pieceId.charAt(0));
        int[] kCords = new int[2];
        for (int i=0; i<boardArray.length; i++) {
            if (boardArray[i].equals(kingId)) {
                kCords = getRowColFromBoardArrayIdx(i);
            }
        }

        boolean kingNotExposed = checkForChecks(boardArray, pieceId.charAt(0), kCords[0], kCords[1]).equals("");

        Array.set(boardArray, startIdx, pieceId);
        Array.set(boardArray, endIdx, origPiece);

        return kingNotExposed;
    }

    /**
     * If all move validation tests are passed, move piece
     * Updates boardArray and player turn
     */
    public void movePiece(String pieceId, int startRow, int startCol, int endRow, int endCol) {
        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;

        int startIdx = getIdxInBoardArray(startRow, startCol);
        int endIdx = getIdxInBoardArray(endRow, endCol);

        if (checkForPieceOnLandingSquare(endRow, endCol)) {
            capturePiece(endRow, endCol);
        }

        Array.set(boardArray, startIdx, "");
        Array.set(boardArray, endIdx, pieceId);

        Integer playerTurn = mPlayerTurn.getValue();
        if (playerTurn == null) playerTurn = 1;

        mPlayerTurn.setValue(playerTurn + 1);
        mBoardArray.setValue(boardArray);

        ArrayList<Integer> wKingCords = mWhiteKingPosition.getValue();
        ArrayList<Integer> bKingCords = mBlackKingPosition.getValue();

        mWhiteKingCheck.setValue(checkForChecks(boardArray, 'w', wKingCords.get(0), wKingCords.get(1)));
        mBlackKingCheck.setValue(checkForChecks(boardArray, 'b', bKingCords.get(0), bKingCords.get(1)));

        disableEnPassant(pieceId.charAt(0));
    }

    private void disableEnPassant(Character color) {
        PieceViewModel pieceModel = MainActivity.getPieceViewModel();
        for (int i=1; i<=8; i++) {
            Pawn pawn = (Pawn) pieceModel.getPieceById(String.format("%sp%s", color, i));
            pawn.enableEnPassant("");
        }
    }

    private void capturePiece(int row, int col) {
        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;

        String capturedPieceId = boardArray[getIdxInBoardArray(row, col)];

        if (capturedPieceId.charAt(0) == 'w') {
            captureWhitePiece(capturedPieceId);
        } else if (capturedPieceId.charAt(0) == 'b') {
            captureBlackPiece(capturedPieceId);
        }
    }

    private void captureWhitePiece(String pieceId) {
        ArrayList<String> capturedWhite = mCapturedWhite.getValue();

        if (capturedWhite == null) {
            capturedWhite = new ArrayList<>();
        }

        capturedWhite.add(pieceId);

        mCapturedWhite.setValue(capturedWhite);
    }

    private void captureBlackPiece(String pieceId) {
        ArrayList<String> capturedBlack = mCapturedBlack.getValue();

        if (capturedBlack == null) {
            capturedBlack = new ArrayList<>();
        }

        capturedBlack.add(pieceId);

        mCapturedBlack.setValue(capturedBlack);
    }

    public boolean checkIfKingCanCastle(String kingId, int startCol, int endCol) {
        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;

        char kColor = kingId.charAt(0);
        int startRow = kColor == 'w' ? 8 : 1;
        int direction = startCol < endCol ? 1 : -1; // -1 is toward queen side; 1 is king side
        int colsToMove = direction == -1 ? 3 : -2; // 3 is queen side castle; -2 is king side castle

        // Check that there are no pieces or checks on castling path
        for (int i=1; i<=Math.abs(colsToMove); i++)  {
            String pieceAt = boardArray[getIdxInBoardArray(startRow, startCol +  i * direction)];
            if (!pieceAt.equals("")) return false;
            if (!ensureOwnKingNotExposed(kingId, startRow, 5, startRow, startCol + i * direction)) return false;
        }

        // Check that assisting rook hasn't been moved
        int rookNum = direction == -1 ? 1 : 2;
        String tRookId = String.format("%sr%s", kColor, rookNum);
        PieceViewModel pieceModel = MainActivity.getPieceViewModel();
        Rook tRook = (Rook) pieceModel.getPieceById(tRookId);

        if (tRook.checkIfMoved()) return false;

        // Move assisting rook
        tRook.assistInKingCastle(colsToMove);

        return true;
    }

    public void enableEnPassant(String color, int row, int col) {
        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;

        PieceViewModel pieceModel = MainActivity.getPieceViewModel();
        String pieceAt;
        Pawn tPawn;
        if (col < 8) {
            pieceAt = boardArray[getIdxInBoardArray(row, col + 1)];
            if (!pieceAt.equals("") && pieceAt.charAt(0) != color.charAt(0) && pieceAt.charAt(1) == 'p') {
                tPawn = (Pawn) pieceModel.getPieceById(pieceAt);
                tPawn.enableEnPassant(pieceAt);
            }
        }

        if (col > 1) {
            pieceAt = boardArray[getIdxInBoardArray(row, col - 1)];
            if (!pieceAt.equals("") && pieceAt.charAt(0) != color.charAt(0) && pieceAt.charAt(1) == 'p') {
                tPawn = (Pawn) pieceModel.getPieceById(pieceAt);
                tPawn.enableEnPassant(pieceAt);
            }
        }
    }

    public void enPassant(int row, int col) {
        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;

        capturePiece(row, col);

        boardArray[getIdxInBoardArray(row, col)] = "";
        mBoardArray.setValue(boardArray);
    }

    public void promoteToQueen(String pieceId, int startRow, int startCol, int endRow, int endCol) {
        String[] boardArray = mBoardArray.getValue();
        assert boardArray != null;

        char colorChar = 'w';
        if (endRow == 8) { colorChar = 'b'; }

        boardArray[getIdxInBoardArray(startRow, startCol)] = "";
        boardArray[getIdxInBoardArray(endRow, endCol)] = String.format("%sq2", colorChar);
        mBoardArray.setValue(boardArray);

        Log.d("PROMOTE", String.format("boardArray start %s", boardArray[getIdxInBoardArray(startRow, startCol)]));

        PieceViewModel pieceModel = MainActivity.getPieceViewModel();
        pieceModel.promotePawnToQueen(pieceId, endRow, endCol);

        setPlayerTurn(getPlayerTurn().getValue() + 1);
    }
}