package de.smasi.tickmate.widgets;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;

public class SummaryNumber extends View {
	Path path;
	
	public SummaryNumber(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}


	public SummaryNumber(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}


	protected Paint paint;
	private double number;
	private int decimals;
	private String bottomtext;
	

	public SummaryNumber(Context context) {
		super(context);
		init();
	}

	private void init() {
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.number = 23;
		this.decimals = 1;
		this.bottomtext = "example text";
	}

	public void setData(double d, int decimals, String bottomtext) {
		this.number = d;
		this.decimals = decimals;
		this.bottomtext = bottomtext;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
	    paint.setAntiAlias(true);
	    paint.setTextAlign(Align.CENTER);
	
		// normal
		paint.setStrokeWidth(0);

		float height = getHeight() - 24.0f;
		float height0 = height - 26.0f;
		float width = getWidth();

		// vertical lines
		//canvas.drawLine(0, 0, width, height, paint);
		//canvas.drawLine(0, height, width, 0, paint);
		paint.setStrokeWidth(2);
		paint.setStyle(Paint.Style.STROKE);  
		paint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
		paint.setAlpha(64);
		paint.setStyle(Paint.Style.FILL);  

		float bottomtextsize = 16.0f;
		float padding = 3.0f;
		float cx = (float)(width/2.0);
		float cy = (float)((height - bottomtextsize - padding)/2.0);
		canvas.drawCircle(cx, cy, cy, paint);
		paint.setAlpha(32);

		paint.setTextSize((float)(cy));

		paint.setColor(getResources().getColor(android.R.color.white));
		if (number < 0) {			
			canvas.drawText("?", cx, (float)(cy+cy/3.0), paint);
		}
		else {
			canvas.drawText(String.format(String.format("%%.%df", decimals), number), cx, (float)(cy+cy/3.0), paint);
		}
		paint.setTextSize(bottomtextsize);

		canvas.drawText(this.bottomtext, cx, (float)height, paint);
	}
	

	

}
