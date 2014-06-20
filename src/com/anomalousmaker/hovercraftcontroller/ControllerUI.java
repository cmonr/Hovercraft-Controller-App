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

	public Hovercraft robot;
	
	// Painting Styles
	private static Paint lines;	// Lines Style
	private static Paint fill;	// Fill Style
	private static Paint text;	// Text Style

	// UI Elements
	private static Rect joystick_bounds;
	private static Rect hover_bounds;
	private static Rect hover_fill;
	
	// Invisible UI Contact Areas
	private static Rect joystick_contact_area;
	private static Rect hover_contact_area;

	// Text Indicators
	public static String status = "Disconnected";
	private static String power_left_percentage = "-.--";
	private static String thrust_percentage = "---";
	private static String hover_percentage = "---";
	private static String servo_percentage = "---";


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
		joystick_bounds = new Rect();
		hover_bounds = new Rect();
		hover_fill = new Rect();
		
		// UI Contact Areas 
		joystick_contact_area = new Rect();
		hover_contact_area = new Rect();


		// Background Color
		setBackgroundColor(Color.BLACK);
	}
	
	public void setStatus(String str)
	{
		status = str;
		
		invalidate();
	}

	public void enableUI(boolean connected)
	{
		btConnected = connected;
		
		if (connected){
			thrust_percentage = "0";
			servo_percentage = "0";
			hover_percentage = "0";
			
			robot.enableAll();
		}else{
			power_left_percentage = "-.--";
			thrust_percentage = "---";
			servo_percentage = "---";
			hover_percentage = "---";
			
			// TODO: Set fills to 0%
		}
		
		invalidate();
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
		drawRect(joystick_bounds, width*0.25f, (height-20)/2.0f, (height-20)*0.75f, (height-20)*0.75f);
		drawRect(hover_bounds, width*0.75f, (height-20)/2.0f, (height-20)*0.25f, (height-20)*0.75f);
		
		//  Redraw contact area here since _width_ and _height_ are determined on screen redraw 
		drawRect(joystick_contact_area, width*0.25f, (height-20)/2.0f, (height-20)*(0.75f + 0.3f), (height-20)*(0.75f + 0.3f));
		drawRect(hover_contact_area, width*0.75f, (height-20)/2.0f, (height-20)*(0.25f + 0.3f), (height-20)*(0.75f+ + 0.3f));

		canvas.drawRect(joystick_bounds, lines);
		canvas.drawRect(hover_bounds, lines);
		canvas.drawRect(hover_fill, fill);


		// Update indicators
		//  Connection
		text.setTextSize(height * 0.05f);
		text.setTextAlign(Align.LEFT);
		canvas.drawText(status, 10, height-10, text);

		//  Power Left
		text.setTextAlign(Align.RIGHT);
		canvas.drawText(power_left_percentage + "%", width-10, height-10, text);

		//  Throttle Values
		text.setTextAlign(Align.LEFT);
		canvas.drawText(thrust_percentage + "%", joystick_bounds.left+8, joystick_bounds.top-12, text);
		text.setTextAlign(Align.RIGHT);
		canvas.drawText(servo_percentage + "%", joystick_bounds.right-8, joystick_bounds.top-12, text);
		text.setTextAlign(Align.CENTER);
		canvas.drawText(hover_percentage + "%", hover_bounds.centerX()+8, hover_bounds.top-12, text);		
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
				boolean joystick_contact_area_touched = false;
				boolean hover_contact_area_touched = false;
				
				// Cycle through all active points since onTouchEvent only generates an ACTION_MOVE for the first point...
				for(int i=0; i<event.getPointerCount(); i++){

					int x = (int) MotionEventCompat.getX(event, i);
					int y = (int) MotionEventCompat.getY(event, i);

					
					// Thrust & Servo Control
					if (joystick_contact_area.contains(x, y) && joystick_contact_area_touched == false){
						// Inside of joystick hit area
						if (joystick_bounds.contains(x, y)){
							// Inside of visible bounds
							thrust_percentage = String.valueOf((int) (100 * (joystick_bounds.height() - (y - joystick_bounds.top))) / joystick_bounds.height());
							servo_percentage = String.valueOf((int) (100 * (joystick_bounds.width() - (joystick_bounds.right - x))) / joystick_bounds.width());
						}else{
							// Being set to either 0% or 100%
							if (x < joystick_bounds.left){
								servo_percentage = "0";
							}else if (x > joystick_bounds.right){
								servo_percentage = "100";
							}else{
								servo_percentage = String.valueOf((int) (100 * (joystick_bounds.width() - (joystick_bounds.right - x))) / joystick_bounds.width());
							}

							if (y < joystick_bounds.top){
								thrust_percentage= "100";
							}else if (y > joystick_bounds.bottom){
								thrust_percentage= "0";
							}else{
								thrust_percentage = String.valueOf((int) (100 * (joystick_bounds.height() - (y - joystick_bounds.top))) / joystick_bounds.height());
							}
						}
						
						joystick_contact_area_touched = true;
					}
					
					
					// Hover Control 
					if (hover_contact_area.contains(x, y) && hover_contact_area_touched == false){
						// Inside of hover hit area
						if (hover_bounds.contains(x,y)){
							// Inside of visible bounds
							hover_fill.set(hover_bounds.left, y, hover_bounds.right, hover_bounds.bottom);
							hover_percentage = String.valueOf((int) (100 * (hover_bounds.height() - (y - hover_bounds.top))) / hover_bounds.height());
						}else{
							if (y < hover_bounds.top){
								hover_fill.set(hover_bounds.left, hover_bounds.top, hover_bounds.right, hover_bounds.bottom);
								hover_percentage= "100";
							}else if (y > hover_bounds.bottom){
								hover_fill.set(hover_bounds.left, hover_bounds.bottom, hover_bounds.right, hover_bounds.bottom);
								hover_percentage= "0";
							}else{
								hover_fill.set(hover_bounds.left, y, hover_bounds.right, hover_bounds.bottom);
								hover_percentage = String.valueOf((int) (100 * (joystick_bounds.height() - (y - joystick_bounds.top))) / joystick_bounds.height());
							}
						}
						
						hover_contact_area_touched = true;
					}
				}
				
				// Reset values if joystick area not touched
				if (joystick_contact_area_touched == false)
				{
					thrust_percentage = "0";
					servo_percentage = "50";
				}
				
				// Update robot
				robot.setThrust(Integer.parseInt(thrust_percentage)/100.0f);
				robot.setServo(Integer.parseInt(servo_percentage)/100.0f);
				robot.setHover(Integer.parseInt(hover_percentage)/100.0f);
			}

			// Request to redraw screen
			invalidate();
		}

		return true;
	} 


}
