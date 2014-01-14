package com.example.wilkiowce;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class Board extends Activity {
	private Pawn wolf;
	
	GameState gameState;
	boolean gameInProgress;
	int currentPlayer;
	int selectedRow;
	int selectedCol;
	PawnMove[] legalMoves;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.board);
		
		wolf = new Pawn(Board.this);
		wolf.setId(1);
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.h8);
		relativeLayout.addView(wolf);
		//wolf.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) wolf.getLayoutParams();
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		wolf.setLayoutParams(layoutParams);
		
		
		wolf.invalidate();
		
		/*
		 * Kod z Javy TODO sprawdizc jak to sie ma do tego co było napisane wcześniej
		 */
		gameState = new GameState();
		doNewGame();
		
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
					if (row == 7 && col == 4)
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
		
		void makeMove(PawnMove pawnMove) {
			makeMove(pawnMove.fromRow, pawnMove.fromCol, pawnMove.toRow, pawnMove.toCol);
		}
		
		void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
			board[toRow][toCol] = board[fromRow][fromCol];
			board[fromRow][toCol] = EMPTY;
			if (toRow == 0 && board[toRow][toCol] == WOLF) {
				/*
				 * TODO Koniec gry wilk wygrał
				 */
			}	
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
						if (canMove(player, row, col, row - 1, col + 1))
							moves.add(new PawnMove(row, col, row + 1, col - 1));
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
