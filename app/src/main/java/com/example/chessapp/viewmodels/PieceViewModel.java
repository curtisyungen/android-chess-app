package com.example.chessapp.viewmodels;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chessapp.game.Type;
import com.example.chessapp.pieces.Bishop;
import com.example.chessapp.pieces.King;
import com.example.chessapp.pieces.Knight;
import com.example.chessapp.pieces.Pawn;
import com.example.chessapp.pieces.Piece;
import com.example.chessapp.pieces.Queen;
import com.example.chessapp.pieces.Rook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class PieceViewModel extends ViewModel {
    private static final String TAG = "PieceViewModel";

    private MutableLiveData<ArrayList<Piece>> mPieceArray = new MutableLiveData<>();

    public LiveData<ArrayList<Piece>> getPieceArray() {
        return mPieceArray;
    }

    public Piece getPieceById(String pieceId) {
        ArrayList<Piece> pieceArray = mPieceArray.getValue();
        assert pieceArray != null;

        for (int i = 0; i < pieceArray.size(); i++) {
            if (pieceArray.get(i).getPieceId().equals(pieceId)) {
                return pieceArray.get(i);
            }
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getPositionFromJSONFile(Context context, String title) {
        Log.i(TAG, "getPositionFromJSONFile called.");

        String json;
        try {
            // Get data from file
            InputStream input = context.getAssets().open("positions.json");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // Store data in JSON Array
            json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);
            JSONArray arr = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                if (title.equals(obj.getString("title"))) {
                    arr = new JSONArray(obj.getString("position"));
                    break;
                }
            }

            String[] position = new String[64];
            for (int i = 0; i < arr.length(); i++) {
                position[i] = arr.get(i).toString();
            }

            //setPosition(position);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void resetGame() {
        mPieceArray.setValue(new ArrayList<Piece>());
        setPosition();
    }

    public void movePieceToPosition(String pieceId, int endRow, int endCol) {
        ArrayList<Piece> pieces = mPieceArray.getValue();
        Piece piece = getPieceById(pieceId);

        Type pieceType = piece.getType();
        String color = piece.getColor();

        pieces.remove(piece);

        Piece newPiece;

        if (pieceType == Type.ROOK) {
            newPiece = new Rook(pieceId, endRow, endCol, color);
        }
        else if (pieceType == Type.KNIGHT) {
            newPiece = new Knight(pieceId, endRow, endCol, color);
        }
        else if (pieceType == Type.BISHOP) {
            newPiece = new Bishop(pieceId, endRow, endCol, color);
        }
        else {
            newPiece = new Queen(pieceId, endRow, endCol, color);
        }
    }

    public void setPosition() {
        Log.d(TAG, "set position called.");
        Dictionary<String, Piece> position = new Hashtable<>();

        position.put("br1", new Rook("br1", 1, 1, "black"));
        position.put("bn1", new Knight("bn1", 1, 2, "black"));
        position.put("bb1", new Bishop("bb1", 1, 3, "black"));
        position.put("bq1", new Queen("bq1", 1, 4, "black"));
        position.put("bk1", new King("bk1", 1, 5, "black"));
        position.put("bb2", new Bishop("bb2", 1, 6, "black"));
        position.put("bn2", new Knight("bn2", 1, 7, "black"));
        position.put("br2", new Rook("br2", 1, 8, "black"));

        position.put("bp1", new Pawn("bp1", 2, 1, "black"));
        position.put("bp2", new Pawn("bp2", 2, 2, "black"));
        position.put("bp3", new Pawn("bp3", 2, 3, "black"));
        position.put("bp4", new Pawn("bp4", 2, 4, "black"));
        position.put("bp5", new Pawn("bp5", 2, 5, "black"));
        position.put("bp6", new Pawn("bp6", 2, 6, "black"));
        position.put("bp7", new Pawn("bp7", 2, 7, "black"));
        position.put("bp8", new Pawn("bp8", 2, 8, "black"));

        position.put("wr1", new Rook("wr1", 8, 1, "white"));
        position.put("wn1", new Knight("wn1", 8, 2, "white"));
        position.put("wb1", new Bishop("wb1", 8, 3, "white"));
        position.put("wq1", new Queen("wq1", 8, 4, "white"));
        position.put("wk1", new King("wk1", 8, 5, "white"));
        position.put("wb2", new Bishop("wb2", 8, 6, "white"));
        position.put("wn2", new Knight("wn2", 8, 7, "white"));
        position.put("wr2", new Rook("wr2", 8, 8, "white"));

        position.put("wp1", new Pawn("wp1", 7, 1, "white"));
        position.put("wp2", new Pawn("wp2", 7, 2, "white"));
        position.put("wp3", new Pawn("wp3", 7, 3, "white"));
        position.put("wp4", new Pawn("wp4", 7, 4, "white"));
        position.put("wp5", new Pawn("wp5", 7, 5, "white"));
        position.put("wp6", new Pawn("wp6", 7, 6, "white"));
        position.put("wp7", new Pawn("wp7", 7, 7, "white"));
        position.put("wp8", new Pawn("wp8", 7, 8, "white"));

        initPieceArray(position);
    }

    public void initPieceArray(Dictionary<String, Piece> position) {
        Log.d(TAG, "initPieceArray: called.");

        ArrayList<Piece> pieces = new ArrayList<>();

        for (Enumeration i = position.elements(); i.hasMoreElements(); ) {
            pieces.add((Piece) i.nextElement());
        }

        mPieceArray.setValue(pieces);
    }
}
