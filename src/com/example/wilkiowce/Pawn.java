package com.example.wilkiowce;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;

public class Pawn extends View {
	
	private Paint mPaint;
	private Paint mGlow;
	private int mSquareWidth, mSquareHeight;
	
	private boolean mDragInProgress;
	private boolean mHovering;
	private boolean mAcceptsDrag;
	private boolean mSelected;
	private Context mContext; 
	
	private int mPawnType;
	
	private static final int NUM_GLOW_STEPS = 4;
	private static final int GREEN_STEP = 0x0000FF00 / NUM_GLOW_STEPS;
	private static final int WHITE_STEP = 0x00FFFFFF / NUM_GLOW_STEPS;
	private static final int ALPHA_STEP = 0xFF000000 / NUM_GLOW_STEPS;

	public Pawn(Context context) {
		super(context);
		mContext = context;
		
		setFocusable(true);
		setClickable(true);
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(6);
		//mPaint.setColor(Color.MAGENTA);
		
		mGlow = new Paint();
		mGlow.setAntiAlias(true);
		mGlow.setStrokeWidth(1);
		mGlow.setStyle(Paint.Style.STROKE);
		
		setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				Log.i("lukasz", "long click");
				ClipData data = ClipData.newPlainText("", "");
				v.startDrag(data, new DragShadowBuilder(v), v, 0);
				return true;
			}
		});
	}
	
	public void draw(Canvas canvas, int squareWidth, int squareHeight, int pawnType, boolean selected) {
		mSquareWidth = squareWidth;
		mSquareHeight = squareHeight;
		mPawnType = pawnType;
		mSelected = selected;
		//measure(mSquareWidth, mSquareHeight);
		onDraw(canvas);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		//float rad = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());//10;getWidth();
		
		float rad = mSquareHeight/2.2f;
		final float cx = mSquareWidth/2;
		final float cy = mSquareHeight/2;
		if (mPawnType == Board.WOLF) {
			mPaint.setColor(Color.BLACK);
		}
		else if (mPawnType == Board.SHEEP) {
			mPaint.setColor(Color.RED);
		}
		else if (mPawnType == Board.WHITE){
			mPaint.setColor(mContext.getResources().getColor(R.color.light_square));
		}
		else {
			Log.i("rysuj", "czrane");
			mPaint.setColor(mContext.getResources().getColor(R.color.dark_square));
		}
		
		canvas.drawCircle(cx, cy, rad, mPaint);
		
		if (mSelected) {
			for (int i = NUM_GLOW_STEPS; i > 0; i--) {
				int color = mHovering ? WHITE_STEP : GREEN_STEP;
				color = i*(color | ALPHA_STEP);
				mGlow.setColor(color);
				canvas.drawCircle(cx, cy, rad, mGlow);
				rad -= 0.5f;
				canvas.drawCircle(cx, cy, rad, mGlow);
				rad -= 0.5f;
			}
		}
	}
	
	/*@Override
	protected void  onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}*/
	
	
	
	public boolean onDragEvent(DragEvent event) {
		boolean result = false;
		switch(event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			mDragInProgress = true;
			mAcceptsDrag = result = true;
			
			if (mAcceptsDrag) {
				invalidate();
			}
			break;
		case DragEvent.ACTION_DRAG_ENDED:
			if (mAcceptsDrag) {
				invalidate();
			}
			mDragInProgress = false;
			mHovering = false;
			break;
		case DragEvent.ACTION_DRAG_LOCATION:
			result = mAcceptsDrag;
			break;
		case DragEvent.ACTION_DROP:
			processDrop(event);
			result = true;
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			mHovering = true;
			invalidate();
			break;
		case DragEvent.ACTION_DRAG_EXITED:
			mHovering = false;
			invalidate();
			break;
		default:
			result = mAcceptsDrag;
			break;
		}
		return result;
	}
	
	private void processDrop(DragEvent event) {
		final ClipData data = event.getClipData();
		final int N = data.getItemCount();
	}

}
