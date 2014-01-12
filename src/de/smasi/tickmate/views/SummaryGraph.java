package de.smasi.tickmate.views;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
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
		paint = new Paint();
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
		
		for (int i=0; i < len; i++) {
			int val = this.data.get(i);
			float h = ((height-24.f)-val/(1.0f*this.maximum)*(height-24.f)) + 24.0f;
			paint.setStrokeWidth(1);
			paint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
			canvas.drawRect(i*width/len, h, (i+1)*width/len, height, paint);
			if (val > 0) {
				paint.setColor(getResources().getColor(android.R.color.secondary_text_dark));
				canvas.drawText(Integer.toString(val), (i+0.5f)*width/len, h-5.0f,paint);
			}
			paint.setColor(getResources().getColor(android.R.color.secondary_text_dark));
			canvas.drawText(keys.get(i), (i+0.5f)*width/len, height+20.0f,paint);
		}
	}
	

	

}
