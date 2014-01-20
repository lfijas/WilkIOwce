package com.example.wilkiowce;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class Board extends Activity {
	
	public static final int EMPTY = 0;
	public static final int WOLF = 1;
	public static final int SHEEP = 2;
	public static final int WHITE = 3;
	
	public Context mContext;
	private BluetoothService mBluetoothService;
	
	
	private Pawn wolf;
	
	private int player;
	
	GameState gameState;
	boolean gameInProgress;
	int currentPlayer;
	int selectedRow;
	int selectedCol;
	PawnMove[] legalMoves;
	BoardSquareView[][] squares = new BoardSquareView[8][8];
 	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.board);
		mBluetoothService = BluetoothService.getInstance(Board.this, mHandler);
		mBluetoothService.setBoard(this);
		mContext = Board.this;
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		player = extras.getInt("player");
		
		RelativeLayout boardLayout = (RelativeLayout) findViewById(R.id.board_layout);
		for (int row = 0; row < 8; row++){
			for(int col = 0; col < 8; col++) {
				squares[row][col] = new BoardSquareView(Board.this, row, col);
				squares[row][col].setBackgroundResource(R.drawable.black_square);
				if (col % 2 == row % 2) {
					squares[row][col].setBackgroundResource(R.drawable.white_square);
				}
				squares[row][col].setId(100 * row + 10 * col + 1);
				boardLayout.addView(squares[row][col]);
				if (col > 0) {
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) squares[row][col].getLayoutParams();
					params.addRule(RelativeLayout.RIGHT_OF, 100 * row + 10 * (col - 1) + 1);
					squares[row][col].setLayoutParams(params);
				}
				if (row > 0) {
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) squares[row][col].getLayoutParams();
					params.addRule(RelativeLayout.BELOW, 100 * (row - 1) + 10 * col + 1);
					squares[row][col].setLayoutParams(params);
				}
			}
		}
		
		/*wolf = new Pawn(Board.this);
		wolf.setId(1);
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.h8);
		relativeLayout.addView(wolf);
		//wolf.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) wolf.getLayoutParams();
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		wolf.setLayoutParams(layoutParams);
		
		
		wolf.invalidate();*/
		
		/*
		 * Kod z Javy TODO sprawdizc jak to sie ma do tego co było napisane wcześniej
		 */
		gameState = new GameState();
		doNewGame();
	}
	
	public final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MainActivity.MESSAGE_READ:
				byte [] readBuf = (byte[]) msg.obj;
				String readMessage = new String(readBuf, 0, msg.arg1);
				Log.i("luke", readMessage);
				Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_SHORT).show();
				String[] splitedStrings = readMessage.split(";");
				PawnMove opponentMove = new PawnMove(Integer.parseInt(splitedStrings[0]), Integer.parseInt(splitedStrings[1]), Integer.parseInt(splitedStrings[2]), Integer.parseInt(splitedStrings[3]));
				doMakeMove(opponentMove, true);
				break;
			case MainActivity.MESSAGE_WRITE:
				
				break;
				}
		}
	};
	
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_NEUTRAL:
				finish();
				break;
			}
			
		}
	};
	
	private void sendMessage(PawnMove move) {
		String message = move.fromRow + ";" + move.fromCol + ";" + move.toRow + ";" + move.toCol;
		byte[] send = message.getBytes();
		mBluetoothService.write(send);
	}
	
	void drawBoard() {
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (row % 2 == col % 2) {
					squares[row][col].myDraw(Board.WHITE, false);
				}
				else {
					if (gameState.board[row][col] == GameState.WOLF) {
						Log.i("rysuj_wilka", "row: " + row + ", col: " + col);
						if (row == selectedRow && col == selectedCol) {
							squares[row][col].myDraw(Board.WOLF, true);
						}
						else {
							squares[row][col].myDraw(Board.WOLF, false);
						}
					}
					else if (gameState.board[row][col] == Board.SHEEP) {
						Log.i("rysuj_owce", "row: " + row + ", col: " + col);
						if (row == selectedRow && col == selectedCol) {
							squares[row][col].myDraw(Board.SHEEP, true);
						}
						else {
							squares[row][col].myDraw(Board.SHEEP, false);
						}
					}
					else {
						Log.i("rysuj_czarne", "row: " + row + ", col: " + col);
						squares[row][col].myDraw(Board.EMPTY, false);
					}
				}
			}
		}
	}
	
	void doNewGame() {
		gameState.setUpGame();
		currentPlayer = GameState.WOLF;
		legalMoves = gameState.getLegalMoves(GameState.WOLF);
		selectedRow = -1;
		gameInProgress = true;
		/*
		 * TODO Odrysuj
		 */
		drawBoard();
	}
	
	void doClickSquare(int row, int col) {
		if (currentPlayer != player) {
			Toast.makeText(Board.this, "Teraz ruch przeciwnika", Toast.LENGTH_LONG).show();
			return;
		}
		for (int i = 0; i < legalMoves.length; i ++) {
			if (legalMoves[i].fromRow == row && legalMoves[i].fromCol == col) {
				selectedRow = row;
				selectedCol = col;
				drawBoard();
				return;
			}
		}
		
		if (selectedRow < 0) {
			return;
		}
		
		for (int i = 0; i < legalMoves.length; i++) 
			if (legalMoves[i].fromRow == selectedRow && legalMoves[i].fromCol == selectedCol 
			&& legalMoves[i].toRow == row && legalMoves[i].toCol == col) {
				doMakeMove(legalMoves[i], false);
				return;
			}
	}
	
	void doMakeMove(PawnMove move, boolean opponentMove) {
		boolean wolfWin = gameState.makeMove(move);
		if (wolfWin) {
			Log.i("toRow", "koniec");
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage("Koniec gry. Wygrał wilk.").setNeutralButton("OK", dialogClickListener).show();
			//Toast.makeText(Board.this, "Koniec gry wygrał wilk", Toast.LENGTH_LONG).show();
		}
		if (currentPlayer == GameState.SHEEP) {
			ImageView token = (ImageView) findViewById(R.id.player_token);
			token.setBackgroundResource(R.drawable.wolf_token);
			token.invalidate();
			currentPlayer = GameState.WOLF;
		}
		else if (currentPlayer == GameState.WOLF) {
			ImageView token = (ImageView) findViewById(R.id.player_token);
			token.setBackgroundResource(R.drawable.sheep_token);
			token.invalidate();
			currentPlayer = GameState.SHEEP;
		}
		
		legalMoves = gameState.getLegalMoves(currentPlayer);
		if (legalMoves == null) {
			if (currentPlayer == GameState.WOLF) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage("Koniec gry. Wygrał owce.").setNeutralButton("OK", dialogClickListener).show();
				//Toast.makeText(Board.this, "Koniec gry wygrały owce", Toast.LENGTH_LONG).show();
			}
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage("Koniec gry. Wygrał wilk.").setNeutralButton("OK", dialogClickListener).show();
				//Toast.makeText(Board.this, "Koniec gry wygrał wilk", Toast.LENGTH_LONG).show();
			}
		}
		selectedRow = -1;
		if (!opponentMove) {
			sendMessage(move);
		}
		drawBoard();
	}
	
	private static class PawnMove {
		int fromRow, fromCol;
		int toRow, toCol;
		public PawnMove(int r1, int c1, int r2, int c2) {
			fromRow = r1;
			fromCol = c1;
			toRow = r2;
			toCol = c2;
		}	
	}
	
	private static class GameState {
		static final int EMPTY = 0;
		static final int WOLF = 1;
		static final int SHEEP = 2;
		
		int[][] board;
		public GameState() {
			board = new int[8][8];
			setUpGame();
		}
		void setUpGame() {
			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					if (row == 7 && col == 0)
						board[row][col] = WOLF;
					else if (row == 0 && (col % 2 == 1))
						board[row][col] = SHEEP;
					else
						board[row][col] = EMPTY;
				}
			}
		}
		
		int pawnAt(int row, int col) {
			return board[row][col];
		}
		
		boolean makeMove(PawnMove pawnMove) {
			return makeMove(pawnMove.fromRow, pawnMove.fromCol, pawnMove.toRow, pawnMove.toCol);
		}
		
		boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
			Log.i("ruchy", "toRow: " + toRow + ", " + board[toRow][toCol]);
			board[toRow][toCol] = board[fromRow][fromCol];
			board[fromRow][fromCol] = EMPTY;
			if (toRow == 0 && board[toRow][toCol] == WOLF) {
				/*
				 * TODO Koniec gry wilk wygrał
				 */
				return true;
			}
			return false;
		}
		
		PawnMove[] getLegalMoves(int player) {
			if (player != SHEEP && player != WOLF) {
				return null;
			}
			ArrayList<PawnMove> moves = new ArrayList<PawnMove>();
			
			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					if (board[row][col] == player) {
						if (canMove(player, row, col, row + 1, col + 1))
							moves.add(new PawnMove(row, col, row + 1, col + 1));
						if (canMove(player, row, col, row + 1, col - 1))
							moves.add(new PawnMove(row, col, row + 1, col - 1));
						if (canMove(player, row, col, row - 1, col + 1))
							moves.add(new PawnMove(row, col, row - 1, col + 1));
						if (canMove(player, row, col, row - 1, col - 1))
							moves.add(new PawnMove(row, col, row - 1, col - 1));
					}
				}
			}
			
			if (moves.size() == 0)
				return null;
			else {
				PawnMove[] moveArray = new PawnMove[moves.size()];
				for (int i = 0; i < moves.size(); i++)
					moveArray[i] = moves.get(i);
				return moveArray;
			}
		}
		
		private boolean canMove(int player, int r1, int c1, int r2, int c2) {
			if (r2 < 0 || r2 > 7 || c2 < 0 || c2 > 7)
				return false;
			if (board[r2][c2] != EMPTY)
				return false;
			if (player == SHEEP) {
				if (r2 < r1)
					return false;
				return true;
			}
			else {
				return true;
			}
		}
	}
}
