package com.example.chessapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.chessapp.fragments.BoardFragment;
import com.example.chessapp.pieces.King;
import com.example.chessapp.pieces.Piece;
import com.example.chessapp.viewmodels.BoardViewModel;
import com.example.chessapp.viewmodels.PieceViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static int screenwidth;
    public static int screenheight;

    private BoardFragment board;
    private static String mPlayerTurn;

    private static PieceViewModel pieceModel;
    private static BoardViewModel boardModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNewGame();
    }

    public void initNewGame() {
        Log.d(TAG, "initNewGame: called.");
        setScreenDimensions();
        initBoard();
        initHeader();
        initFooter();
        initCapturedPieceLayouts();
        initGameControlBtns();
        initPieceViewModel();
        initBoardViewModel();
    }

    public void setScreenDimensions() {
        screenwidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenheight = Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public void initBoard() {
        board = new BoardFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.board_container, board)
                .commit();
    }

    public void initHeader() {
        FrameLayout headerLayout = (FrameLayout) findViewById(R.id.header_layout);
        getLayoutInflater().inflate(R.layout.layout_header, headerLayout);

        setPlayerTurnMessage();
    }

    public void initFooter() {
        FrameLayout footerLayout = (FrameLayout) findViewById(R.id.footer_layout);
        getLayoutInflater().inflate(R.layout.layout_footer, footerLayout);
    }

    private void initCapturedPieceLayouts() {
        FrameLayout capturedBlack = (FrameLayout) findViewById(R.id.captured_black_container);
        getLayoutInflater().inflate(R.layout.layout_captured, capturedBlack);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) capturedBlack.getLayoutParams();
        params.height = screenwidth / 8;
        params.width = screenwidth;

        capturedBlack.setLayoutParams(params);

        FrameLayout capturedWhite = (FrameLayout) findViewById(R.id.captured_white_container);
        getLayoutInflater().inflate(R.layout.layout_captured, capturedWhite);

        capturedWhite.setLayoutParams(params);
    }

    public void initPieceViewModel() {
        pieceModel = ViewModelProviders.of(this).get(PieceViewModel.class);

        ArrayList<Observer> observers = new ArrayList<Observer>();

        Observer<ArrayList<Piece>> observer = new Observer<ArrayList<Piece>>() {
            @Override
            public void onChanged(ArrayList<Piece> pieces) {
                Log.d(TAG, "onChanged: piece view model changed.");

                if (pieces == null) return;

                for (int p = 0; p < pieces.size(); p++) {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.board_container, pieces.get(p))
                            .commit();

                    if (pieces.get(p).getPieceId().equals("wk1")) {
                        boardModel.setWhiteKingPosition(pieces.get(p).getRow(), pieces.get(p).getCol());
                    } else if (pieces.get(p).getPieceId().equals("bk1")) {
                        boardModel.setBlackKingPosition(pieces.get(p).getRow(), pieces.get(p).getCol());
                    }
                }
            }
        };

        pieceModel.getPieceArray().observe(this, observer);
        pieceModel.setPosition();

        pieceModel.getPromotedPawn().observe(this, new Observer<Piece>() {
            @Override
            public void onChanged(Piece piece) {
                View hidePiece = findViewById(piece.getPieceViewId());
                hidePiece.setVisibility(View.GONE);
            }
        });

        pieceModel.getNewQueen().observe(this, new Observer<Piece>() {
            @Override
            public void onChanged(Piece piece) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.board_container, piece)
                        .commit();
            }
        });
    }

    public void initBoardViewModel() {
        boardModel = ViewModelProviders.of(this).get(BoardViewModel.class);

        boardModel.getPlayerTurn().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer % 2 == 0) mPlayerTurn = "black";
                else mPlayerTurn = "white";
                setPlayerTurnMessage();
            }
        });

        boardModel.getCapturedWhite().observe(this, new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> capturedWhite) {

                if (capturedWhite == null || capturedWhite.size() == 0) return;

                Log.d(TAG, "onChanged: white piece captured.");
                String capturedPiece = capturedWhite.get(capturedWhite.size() - 1);
                Piece piece = pieceModel.getPieceById(capturedPiece);
                piece.setPieceAsCaptured();
                showPieceAsCaptured(piece, capturedWhite.size());
            }
        });

        boardModel.getCapturedBlack().observe(this, new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> capturedBlack) {

                if (capturedBlack == null || capturedBlack.size() == 0) return;

                Log.d(TAG, "onChanged: black piece captured.");
                String capturedPiece = capturedBlack.get(capturedBlack.size() - 1);
                Piece piece = pieceModel.getPieceById(capturedPiece);
                piece.setPieceAsCaptured();
                showPieceAsCaptured(piece, capturedBlack.size());
            }
        });

        boardModel.getWhiteKingCheck().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String checkingPieceId) {
                King whiteKing = (King) pieceModel.getPieceById("wk1");
                whiteKing.setIsInCheck(!checkingPieceId.equals(""));

                boolean checkmate = false;
                if (!checkingPieceId.equals("")) {
                    Piece checkingPiece = pieceModel.getPieceById(checkingPieceId);
                    checkmate = boardModel.checkForCheckmate('w', checkingPiece, pieceModel);

                    if (checkmate) {
                        setCheckMate('b');
                        getAnimation(0, 0, true);
                    }
                    else getAnimation(whiteKing.getRow(), whiteKing.getCol(), false);
                }

            }
        });

        boardModel.getBlackKingCheck().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String checkingPieceId) {
                King blackKing = (King) pieceModel.getPieceById("bk1");
                blackKing.setIsInCheck(!checkingPieceId.equals(""));

                boolean checkmate = false;
                if (!checkingPieceId.equals("")) {
                    Piece checkingPiece = pieceModel.getPieceById(checkingPieceId);
                    checkmate = boardModel.checkForCheckmate('b', checkingPiece, pieceModel);

                    if (checkmate) {
                        setCheckMate('w');
                        getAnimation(0, 0, true);
                    }
                    else getAnimation(blackKing.getRow(), blackKing.getCol(), false);
                }
            }
        });

        boardModel.initBoardArray();
    }

    private void setCheckMate(Character winningColor) {
        if (winningColor == 'w') {
            mPlayerTurn = "wcm";
        } else {
            mPlayerTurn = "bcm";
        }

        setPlayerTurnMessage();
    }

    private void showPieceAsCaptured(Piece piece, int position) {
        View hidePiece = findViewById(piece.getPieceViewId());
        hidePiece.setVisibility(View.GONE);

        int pieceHeight = screenheight / 16;
        int pieceWidth = screenwidth / 16;

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pieceWidth, pieceHeight);
        layoutParams.leftMargin = (pieceWidth * (position - 1));

        int pieceImage = piece.getPieceImage();
        String pieceColor = piece.getColor();

        ImageView dummyPiece = new ImageView(this);
        dummyPiece.setImageResource(pieceImage);
        dummyPiece.setLayoutParams(layoutParams);

        FrameLayout capturedZone = new FrameLayout(this);
        if (pieceColor.equals("black")) {
            capturedZone = (FrameLayout) findViewById(R.id.captured_white_container);
        } else if (pieceColor.equals("white")) {
            capturedZone = (FrameLayout) findViewById(R.id.captured_black_container);
        }
        capturedZone.addView(dummyPiece);
    }

    public void initGameControlBtns() {
        Button resetBtn = (Button) findViewById(R.id.reset_button);
        resetBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGame();
            }
        });
    }

    public void resetGame() {
        FrameLayout whiteCapturedZone = (FrameLayout) findViewById(R.id.captured_white_container);
        FrameLayout blackCapturedZone = (FrameLayout) findViewById(R.id.captured_black_container);

        whiteCapturedZone.removeAllViews();
        blackCapturedZone.removeAllViews();

        List<Fragment> frags = getSupportFragmentManager().getFragments();

        for (int i = 1; i < frags.size(); i++) {
            getSupportFragmentManager().beginTransaction()
                    .remove(frags.get(i))
                    .commit();
        }

        boardModel.resetGame();
        pieceModel.resetGame();

        resetAnimation();
    }

    public static String getPlayerTurn() {
        return mPlayerTurn;
    }

    public static BoardViewModel getBoardViewModel() {
        return boardModel;
    }

    public static PieceViewModel getPieceViewModel() {
        return pieceModel;
    }

    public void setPlayerTurnMessage() {
        TextView moveMessage = (TextView) findViewById(R.id.moveMessage);

        if (moveMessage == null || mPlayerTurn == null) return;

        if (mPlayerTurn.equals("white")) {
            moveMessage.setText(R.string.whiteToMove);
        } else if (mPlayerTurn.equals("black")) {
            moveMessage.setText(R.string.blackToMove);
        } else if (mPlayerTurn.equals("wcm")) {
            moveMessage.setText(R.string.checkmate_white);
        } else if (mPlayerTurn.equals("bcm")) {
            moveMessage.setText(R.string.checkmate_black);
        }
    }


    public void getAnimation(int row, int col, boolean isCheckmate) {
        String[] colors = new String[]{"#FF0000", "#FF3232", "#FF4C4C", "#FF6666", "#FF7F7F", "#FF9999", "#FFB2B2", "#FFCCCC", "#FFE5E5"};
        int time = 250;

        if (isCheckmate) {
            for (int i = 0; i <= 8; i++) {
                for (int j = 0; j <= 8; j++) {
                    animate(i, j, "#FF3232", 1000, true);
                }
            }
        } else {
            animate(row, col, colors[0], time, false);

            for (int i = 1; i <= 8; i++) {
                animate(row + i, col, colors[i], time + (i * 100), false);
                animate(row - i, col, colors[i], time + (i * 100), false);
                animate(row, col + i, colors[i], time + (i * 100), false);
                animate(row, col - i, colors[i], time + (i * 100), false);
            }

            for (int i = 1; i <= 8; i++) {
                animate(row + i, col + i, colors[i], time + (i * 100), false);
                animate(row - i, col - i, colors[i], time + (i * 100), false);
                animate(row + i, col - i, colors[i], time + (i * 100), false);
                animate(row - i, col + i, colors[i], time + (i * 100), false);
            }
        }
    }

    public void animate(final int row, final int col, String color, final int ms, final boolean isCheckmate) {
        if (row < 1 || row > 8 || col < 1 || col > 8) return;

        final TextView square = (TextView) findViewById(row * 10 + col);

        ColorDrawable background = (ColorDrawable) square.getBackground();
        final int colorFrom = background.getColor();
        int colorTo = Color.parseColor(color);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(ms);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                square.setBackgroundColor((int) animator.getAnimatedValue());
                unanimate(row, col, colorFrom, ms, isCheckmate);
            }
        });

        colorAnimation.start();
    }

    public void unanimate(int row, int col, int colorTo, int ms, boolean isCheckmate) {
        if (isCheckmate) return;
        if (row < 1 || row > 8 || col < 1 || col > 8) return;

        final TextView square = (TextView) findViewById(row * 10 + col);

        ColorDrawable background = (ColorDrawable) square.getBackground();
        int colorFrom = background.getColor();

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

        colorAnimation.setDuration(ms);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                square.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });

        colorAnimation.start();
    }

    public void resetAnimation()  {
        int color;
        for (int i=0; i<=8; i++) {
            for (int j=0; j<=8; j++) {
                if ((i + j) % 2 == 0) color = Color.parseColor("#f2f2f2");
                else color = Color.parseColor("#cccccc");
                unanimate(i, j, color, 0, false);
            }
        }
    }
}
