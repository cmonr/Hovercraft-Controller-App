package com.anomalousmaker.hovercraftcontroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class ControllerUI extends View {	
	private static final int holo = Color.parseColor("#FF33B5E5");
	public static boolean btConnected = false;

	// Painting Styles
	private static Paint lines;	// Lines Style
	private static Paint fill;	// Fill Style
	private static Paint text;	// Text Style

	// UI Elements
	private static Rect left_rect_bounds;
	private static Rect center_rect_bounds;
	private static Rect right_rect_bounds;

	private static Rect left_rect_fill;
	private static Rect center_rect_fill;
	private static Rect right_rect_fill;

	// Text Indicators
	public static String connection_status = "Disconnected";
	private static String power_left_percentage = "-.--";
	private static String left_throttle_percentage = "---";
	private static String center_throttle_percentage = "---";
	private static String right_throttle_percentage = "---";


	public ControllerUI(Context context) {
		super(context);

		initUI();
	}

	public ControllerUI(Context context, AttributeSet attrs) {
		super(context, attrs);

		initUI();
	}

	public ControllerUI(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		initUI();
	}


	private void initUI() {
		// Init Styles
		lines = new Paint();
		lines.setColor(holo);
		lines.setStrokeWidth(3.0f);
		lines.setStyle(Style.STROKE);

		fill = new Paint();
		fill.setColor(holo);
		fill.setAlpha((int) (255*0.3f));
		fill.setStyle(Style.FILL);

		text = new Paint();
		text.setColor(Color.WHITE);


		// UI Objects
		left_rect_bounds = new Rect();
		center_rect_bounds = new Rect();
		right_rect_bounds = new Rect();

		left_rect_fill = new Rect();
		center_rect_fill = new Rect();
		right_rect_fill = new Rect();


		// Background Color
		setBackgroundColor(Color.BLACK);
	}

	public void enableUI(boolean connected)
	{
		btConnected = connected;
		
		if (connected){
			connection_status = "Connected";
			left_throttle_percentage = "0";
			center_throttle_percentage = "0";
			right_throttle_percentage = "0";
			
		}else{
			connection_status = "Disconnected";
			power_left_percentage = "-.--";
			left_throttle_percentage = "---";
			center_throttle_percentage = "---";
			right_throttle_percentage = "---";
			
			// TODO: Set fills to 0%
		}
		
		invalidate();
		requestLayout();
	}
	
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// Swap variables if needed
		if (width < height)
		{
			int tmp = width;
			width = height;
			height = tmp;
		}


		// Update Interactive UI
		drawRect(left_rect_bounds, width * (0.3f/2 + 0.075f), (height-20)/2, width*0.3f, width*0.3f);
		drawRect(center_rect_bounds, width/2.0f, (height-20)/2, width*0.1f, width*0.3f);
		drawRect(right_rect_bounds, width * (1 - (0.3f/2 + 0.075f)), (height-20)/2, width*0.3f, width*0.3f);

		canvas.drawRect(left_rect_bounds, lines);
		canvas.drawRect(center_rect_bounds, lines);
		canvas.drawRect(right_rect_bounds, lines);

		canvas.drawRect(left_rect_fill, fill);
		canvas.drawRect(center_rect_fill, fill);
		canvas.drawRect(right_rect_fill, fill);


		// Update indicators
		//  Connection
		text.setTextSize(height * 0.05f);
		text.setTextAlign(Align.LEFT);
		canvas.drawText(connection_status, 10, height-10, text);

		//  Power Left
		text.setTextAlign(Align.RIGHT);
		canvas.drawText(power_left_percentage + "%", width-10, height-10, text);

		//  Throttle Values
		text.setTextAlign(Align.CENTER);
		canvas.drawText(left_throttle_percentage + "%", left_rect_bounds.centerX()+8, left_rect_bounds.top-12, text);
		canvas.drawText(center_throttle_percentage + "%", center_rect_bounds.centerX()+8, center_rect_bounds.top-12, text);
		canvas.drawText(right_throttle_percentage + "%", right_rect_bounds.centerX()+8, right_rect_bounds.top-12, text);
	}

	private static void drawRect(Rect rect, float x, float y, float w, float h)
	{
		rect.set((int) (x-(w/2)), (int) (y-(h/2)), (int) (x+(w/2)), (int) (y+(h/2)));
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(btConnected)
		{
			if (event.getActionMasked() ==  MotionEvent.ACTION_MOVE)
			{
				// Cycle through all active points since onTouchEvent only generates an ACTION_MOVE for the first point...
				for(int i=0; i<event.getPointerCount(); i++){

					int x = (int) MotionEventCompat.getX(event, i);
					int y = (int) MotionEventCompat.getY(event, i);


					// Left Throttle 
					if (left_rect_bounds.contains(x, y)){
						// Inside of box
						left_rect_fill.set(left_rect_bounds.left, y, left_rect_bounds.right, left_rect_bounds.bottom);
						left_throttle_percentage = String.valueOf((int) (100 * (left_rect_bounds.height() - (y - left_rect_bounds.top))) / left_rect_bounds.height());
					} else if (x >= left_rect_bounds.left && x <= left_rect_bounds.right){
						if (y >= left_rect_bounds.bottom) {
							// Below Box
							left_rect_fill.set(left_rect_bounds.left, left_rect_bounds.bottom, left_rect_bounds.right, left_rect_bounds.bottom);
							left_throttle_percentage = "0";
						} else {
							// Above Box
							left_rect_fill.set(left_rect_bounds.left, left_rect_bounds.top, left_rect_bounds.right, left_rect_bounds.bottom);
							left_throttle_percentage = "100";
						}
					}

					// Center Throttle 
					if (center_rect_bounds.contains(x, y)){
						// Inside of box
						center_rect_fill.set(center_rect_bounds.left, y, center_rect_bounds.right, center_rect_bounds.bottom);

						center_throttle_percentage = String.valueOf((int) (100 * (center_rect_bounds.height() - (y - center_rect_bounds.top))) / center_rect_bounds.height());
					} else if (x >= center_rect_bounds.left && x <= center_rect_bounds.right){
						if (y >= center_rect_bounds.bottom) {
							// Below Box
							center_rect_fill.set(center_rect_bounds.left, center_rect_bounds.bottom, center_rect_bounds.right, center_rect_bounds.bottom);
							center_throttle_percentage = "0";
						} else {
							// Above Box
							center_rect_fill.set(center_rect_bounds.left, center_rect_bounds.top, center_rect_bounds.right, center_rect_bounds.bottom);
							center_throttle_percentage = "100";
						}
					}

					// Right Throttle 
					if (right_rect_bounds.contains(x, y)){
						// Inside of box
						right_rect_fill.set(right_rect_bounds.left, y, right_rect_bounds.right, right_rect_bounds.bottom);

						right_throttle_percentage = String.valueOf((int) (100 * (right_rect_bounds.height() - (y - right_rect_bounds.top))) / right_rect_bounds.height());
					} else if (x >= right_rect_bounds.left && x <= right_rect_bounds.right){
						if (y >= right_rect_bounds.bottom) {
							// Below Box
							right_rect_fill.set(right_rect_bounds.left, right_rect_bounds.bottom, right_rect_bounds.right, right_rect_bounds.bottom);
							right_throttle_percentage = "0";
						} else {
							// Above Box
							right_rect_fill.set(right_rect_bounds.left, right_rect_bounds.top, right_rect_bounds.right, right_rect_bounds.bottom);
							right_throttle_percentage = "100";
						}
					}
				}
			}

			// Request to redraw screen
			invalidate();
		}

		return true;
	} 


}
