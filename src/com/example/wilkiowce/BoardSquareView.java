package com.example.wilkiowce;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class BoardSquareView extends ImageView {
	private Pawn mPawn;
	private int mPawnType;
	private boolean mSelected;
	
	private Paint mPaint;
	
	private Context mContext;
	private int mRow;
	private int mCol;
	
	public BoardSquareView(Context context, int row, int col) {
		super(context);
		setClickable(true);
		mContext = context;
		mRow = row;
		mCol = col;
		mPawn = new Pawn(context);
		setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("klik", (Integer.toString(mPawnType)));
				((Board) mContext).doClickSquare(mRow, mCol);
			}
		});
		}
	
	public BoardSquareView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(true);
		//mPawn = new Pawn(context, attrs);
		//RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.board_layout);
		
		//mPawn = new Pawn(context, attrs);
		//mPawn.setId(1);
		//mPawn.setLayoutParams(new L)
		
		/*setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i("tescik", "tescik");
				return false;
			}
		});*/
	}
	public void myDraw(int pawnType, boolean selected) {
		mPawnType = pawnType;
		mSelected = selected; 
		invalidate();
	}
	
	protected void onDraw(Canvas canvas) {
		Log.i("lukasz", "lukasz");
		super.onDraw(canvas);
		
		int width = getWidth();
		int height = getHeight();
		//if (((View) getParent()).getId() == R.id.h8) {
			mPawn.draw(canvas, width, height, mPawnType, mSelected);
		//}*/
	}
}
