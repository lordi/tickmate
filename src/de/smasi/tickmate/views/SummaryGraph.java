package de.smasi.tickmate.views;

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

public class SummaryGraph extends View {
	public SummaryGraph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}


	public SummaryGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}


	protected Paint paint;
	private List<Integer> data;
	private List<String> keys;
	private Integer maximum;
	
	public SummaryGraph(Context context) {
		super(context);
		init();
	}

	private void init() {
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG );
		this.data = new LinkedList<Integer>();
		this.data.add(1);
		this.data.add(2);
		this.data.add(3);
		this.data.add(4);
		this.keys = new LinkedList<String>();
		this.keys.add("CO");
		this.keys.add("MING");
		this.keys.add("SO");
		this.keys.add("ON");
		this.maximum = 7;
		
	}


	public void setData(List<Integer> data, List<String> keys, Integer maximum) {
		this.data = data;
		this.keys = keys;
		this.maximum = maximum;
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
		paint.setColor(getResources().getColor(android.R.color.holo_blue_light));
		canvas.drawRect(0, 0, width, height, paint);
		paint.setStyle(Paint.Style.FILL);  
		paint.setTextSize(18.0f);
		int len = this.data.size();
		
		Path p = new Path();
		p.moveTo(0, height);
		float oldH = height;
		
		for (int i=0; i < len; i++) {
			int val = this.data.get(i);
			float h = (height0-val/(1.0f*this.maximum)*height0) + 26.0f;
			float x0 = (i)*width/len;
			float x = (i+0.5f)*width/len;
			p.cubicTo(x0, oldH, x0, h, x, h);
			oldH = h;
		}
		p.cubicTo(width, oldH, width, height, width, height);
		
		paint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
		paint.setStrokeWidth(2.2f);
		paint.setStyle(Style.STROKE);
		canvas.drawPath(p, paint);
		paint.setStyle(Style.FILL);
		paint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
		paint.setAlpha(64);
		canvas.drawPath(p, paint);
		paint.setStyle(Style.FILL);
		paint.setAlpha(255);
			
		for (int i=0; i < len; i++) {
			int val = this.data.get(i);
			float h = (height0-val/(1.0f*this.maximum)*height0) + 26.0f;
			float x0 = (i)*width/len;
			float x = (i+0.5f)*width/len;
			paint.setStrokeWidth(1);
			paint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
			//canvas.drawRect(i*width/len, h, (i+1)*width/len, height, paint);
			if (val > 0) {
				paint.setColor(getResources().getColor(android.R.color.secondary_text_dark));
				canvas.drawText(Integer.toString(val), x, h-9.5f,paint);
				paint.setColor(getResources().getColor(android.R.color.white));
				canvas.drawCircle((i+0.5f)*width/len, h, 6.0f, paint);
				paint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
				canvas.drawCircle((i+0.5f)*width/len, h, 3.0f, paint);
			}
			paint.setColor(getResources().getColor(android.R.color.secondary_text_dark));
			canvas.drawText(keys.get(i), (i+0.5f)*width/len, height+20.0f,paint);
			
		}
	}
	

	

}
