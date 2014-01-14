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
import android.view.View.OnClickListener;

public class Pawn extends View{
	
	private Paint mPaint;
	private int mSquareWidth, mSquareHeight;
	
	private boolean mDragInProgress;
	private boolean mHovering;
	private boolean mAcceptsDrag;

	public Pawn(Context context) {
		super(context);
		
		setFocusable(true);
		setClickable(true);
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(6);
		mPaint.setColor(Color.MAGENTA);
		
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
	
	/*public void draw(Canvas canvas, int squareWidth, int squareHeight) {
		mSquareWidth = squareWidth;
		mSquareHeight = squareHeight;
		measure(mSquareWidth, mSquareHeight);
		onDraw(canvas);
	}*/
	
	@Override
	protected void onDraw(Canvas canvas) {
		//float rad = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());//10;getWidth();
		
		float rad = 30;//mSquareHeight/2.2f;
		final float cx = 30;//mSquareWidth/2;
		final float cy = 30;//mSquareHeight/2;
		
		canvas.drawCircle(cx, cy, rad, mPaint);	
	}
	
	@Override
	protected void  onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}
	
	
	
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
