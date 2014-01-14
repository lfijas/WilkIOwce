package com.example.wilkiowce;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class BoardSquareView extends ImageView {
	//private Pawn mPawn;
	public BoardSquareView(Context context) {
		super(context);
		setClickable(true);
	}
	
	public BoardSquareView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(true);
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
	
	protected void onDraw(Canvas canvas) {
		Log.i("lukasz", "lukasz");
		super.onDraw(canvas);
		int width = getWidth();
		int height = getHeight();
		/*if (((View) getParent()).getId() == R.id.h8) {
			mPawn.draw(canvas, width, height);
		}*/
	}
}
