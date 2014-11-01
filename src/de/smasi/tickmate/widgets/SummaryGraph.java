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

public class SummaryGraph extends View {
	Path path;
	
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
	private boolean cyclic;
	
	public boolean isCyclic() {
		return cyclic;
	}


	public void setCyclic(boolean cyclic) {
		this.cyclic = cyclic;
	}


	public SummaryGraph(Context context) {
		super(context);
		init();
	}

	private void init() {
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		path = new Path();
		this.data = new LinkedList<Integer>();
		this.keys = new LinkedList<String>();
		this.maximum = 7;
		this.cyclic = false;
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
		//canvas.drawRect(0, 0, width, height, paint);
		canvas.drawLine(0, height, width, height, paint);
		paint.setStyle(Paint.Style.FILL);  
		paint.setTextSize(18.0f);
		int len = this.data.size();
		
		if (len == 0)
			return;
		
		// this part needs some refactoring
		path.reset();
		path.moveTo((float) ((-0.5)*width/len), height);
		float oldH = height;
		if (this.cyclic) {
			float h = (height0-this.data.get(len - 1)/(1.0f*this.maximum)*height0) + 26.0f;
			path.lineTo((float) ((-0.5)*width/len), h);
			oldH = h;
		}
		
		for (int i=0; i < len; i++) {
			int val = this.data.get(i);
			float h = (height0-val/(1.0f*this.maximum)*height0) + 26.0f;
			float x0 = (i)*width/len;
			float x = (i+0.5f)*width/len;
			path.cubicTo(x0, oldH, x0, h, x, h);
			oldH = h;
		}

		if (this.cyclic) {
			float h = (height0-this.data.get(0)/(1.0f*this.maximum)*height0) + 26.0f;
			float x = (float) ((len+0.5f)*width/len);
			path.cubicTo(width, oldH, width, h, x, h);
			path.lineTo((float) ((len+0.5f)*width/len), height);
		}
		else 
			path.cubicTo((float) ((len+0.5f)*width/len), oldH, width, height, width, height);
	
		paint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
		paint.setStrokeWidth(2.2f);
		paint.setStyle(Style.STROKE);
		canvas.drawPath(path, paint);
		paint.setStyle(Style.FILL);
		paint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
		paint.setAlpha(64);
		canvas.drawPath(path, paint);
		paint.setStyle(Style.FILL);
		paint.setAlpha(255);
			
		for (int i=0; i < len; i++) {
			int val = this.data.get(i);
			float h = (height0-val/(1.0f*this.maximum)*height0) + 26.0f;
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
