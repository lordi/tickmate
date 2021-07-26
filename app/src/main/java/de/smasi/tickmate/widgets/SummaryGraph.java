package de.smasi.tickmate.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;

import androidx.core.content.ContextCompat;

import java.util.LinkedList;
import java.util.List;

import de.smasi.tickmate.R;

public class SummaryGraph extends View {
	Path path;

	public SummaryGraph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SummaryGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	protected Paint paint;
	private List<Integer> data;
	private List<String> keys;
	private float maximum;
	private float minimum;
	private boolean cyclic;
	private int mColor;
	private int mTextColor;
	private int mMarkerColor;
	final static float MARKER_RADIUS = 6f;

	public boolean isCyclic() {
		return cyclic;
	}

	public void setCyclic(boolean cyclic) {
		this.cyclic = cyclic;
	}

	public void setColor(int color) { this.mColor = color; }

	public SummaryGraph(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		path = new Path();

		this.data = new LinkedList<>();
		this.keys = new LinkedList<>();
		this.cyclic = false;
		this.mColor = ContextCompat.getColor(context, android.R.color.holo_blue_light);
		this.mTextColor = ContextCompat.getColor(context, android.R.color.secondary_text_dark);
		this.mMarkerColor = ContextCompat.getColor(context, android.R.color.white);
	}

	public void setData(List<Integer> data, List<String> keys, Integer maximum) {
		setData( data, keys, maximum, 0 );
	}

	public void setData(List<Integer> data, List<String> keys, Integer maximum, Integer minimum) {
		this.data = data;
		this.keys = keys;
		this.maximum = maximum;
		this.minimum = minimum;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		int len = this.data.size();

		if (len == 0)
			return;

		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);

		// normal
		paint.setStrokeWidth(0);
		paint.setAlpha(255);

		int textSize = getResources().getDimensionPixelSize(R.dimen.fontsize_small);
		paint.setTextSize(textSize);

		Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
		final int fontTop = -fontMetricsInt.top; // distances above baseline are negative
		final int fontBottom = fontMetricsInt.bottom;

		float marginTop = MARKER_RADIUS + fontBottom + fontTop;  // for point/axis labels below/above chart area
		float marginBottom = marginTop;
		if (this.cyclic) {
			marginTop = 2 * marginBottom;
		}
		if (this.maximum <= 0)
			this.maximum = 1f;
		float height0 = this.maximum / (this.maximum + this.minimum) * (getHeight() - (marginTop + marginBottom));
		// height of positive section of chart
		float height = height0 + marginTop; // distance from top to abscissa
		float width = getWidth();

		float deltaX = width/len;
		float x = -0.5f * deltaX;
		float x0 = -deltaX;
		float oldH = height;
		float h;
		path.reset();
		path.moveTo(x, height);
		if (this.cyclic) {
			h = marginTop + height0 - this.data.get(len - 1) / this.maximum * height0;
			path.lineTo(x, h);
			oldH = h;
		}

		for (int i=0; i < len; i++) {
			h = marginTop + height0 - this.data.get(i) / this.maximum * height0;
			x0 += deltaX;
			x += deltaX;
			path.cubicTo(x0, oldH, x0, h, x, h);
			oldH = h;
		}

		x += deltaX;
		if (this.cyclic) {
			h = marginTop + height0 - this.data.get(0) / this.maximum * height0;
			path.cubicTo(width, oldH, width, h, x, h);
			path.lineTo(x, height);
		}
		else
			path.cubicTo(x, oldH, width, height, width, height);

		int dpSize = 2;
		DisplayMetrics dm = getResources().getDisplayMetrics() ;
		float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);

		paint.setStyle(Style.FILL);
		paint.setColor(mColor);
		paint.setAlpha(128);
		canvas.drawPath(path, paint);
		paint.setAlpha(255);
		paint.setStrokeWidth(strokeWidth);
		paint.setStyle(Style.STROKE);
		canvas.drawPath(path, paint);

		canvas.drawLine(0, height, width, height, paint);

		paint.setStyle(Style.FILL);

		x = -0.5f * deltaX;
		double sum = 0.0;
		for (int i = 0; i < len; i++) {
			int val = this.data.get(i);
			sum += val;
		}
		for (int i=0; i < len; i++) {
			int val = this.data.get(i);
			h = marginTop + height0 - val / this.maximum * height0;
			x += deltaX;
			paint.setStrokeWidth(1);
			paint.setColor(mColor);
			if (val != 0) {
				paint.setColor(mTextColor);
				if (val > 0 ) {
					if (this.cyclic) {
						canvas.drawText(" " + Math.round((val / sum) * 100) + "%", x, h - MARKER_RADIUS - fontBottom - (marginTop / 2), paint);
						canvas.drawText(Integer.toString(val), x, h - MARKER_RADIUS - fontBottom, paint);
					} else {
						canvas.drawText(Integer.toString(val), x, h - MARKER_RADIUS - fontBottom, paint);
					}
				} else {
					canvas.drawText(Integer.toString(-val), x, h + MARKER_RADIUS + fontTop, paint);
				}
				paint.setColor(mMarkerColor);
				canvas.drawCircle(x, h, MARKER_RADIUS, paint);
				paint.setColor(mColor);
				canvas.drawCircle(x, h, MARKER_RADIUS / 2, paint);
			}
			paint.setColor(mTextColor);
			canvas.drawText(keys.get(i), x, height + MARKER_RADIUS + fontTop, paint);
		}
	}
}
