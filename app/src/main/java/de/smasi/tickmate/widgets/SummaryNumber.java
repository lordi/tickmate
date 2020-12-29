package de.smasi.tickmate.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;

import androidx.core.content.ContextCompat;

import de.smasi.tickmate.R;

public class SummaryNumber extends View {
	Path path;
    private int mColor;
	private int mNumberColor;
	private int mTextColor;

    public SummaryNumber(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}


	public SummaryNumber(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}


	protected Paint paint;
	private double number;
	private int decimals;
	private String bottomtext;
	

	public SummaryNumber(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.number = 23;
		this.decimals = 1;
		this.bottomtext = "";
        this.mColor = ContextCompat.getColor(context, android.R.color.holo_blue_light);
		this.mNumberColor = ContextCompat.getColor(context, android.R.color.white);
		this.mTextColor = ContextCompat.getColor(context, android.R.color.secondary_text_dark);
    }

	/**
	 * Set Data
	 *
	 * @param d if decimals >= 0: number to be displayed, else: slope of arrow in degrees (+90° = upright),
	 *             will be capped to -90° ... +90°
	 * @param decimals if >= 0: number of decimals for d, else: flag that d should be treated as angle
	 * @param bottomtext caption to be shown below circle
	 */
    public void setData(double d, int decimals, String bottomtext) {
		this.number = d;
		this.decimals = decimals;
		this.bottomtext = bottomtext;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
	    paint.setAntiAlias(true);
	    paint.setTextAlign(Align.CENTER);
	
		int bottomtextsize = getResources().getDimensionPixelSize(R.dimen.fontsize_small);
		paint.setTextSize(bottomtextsize);
		Paint.FontMetricsInt captionFontMetricsInt = paint.getFontMetricsInt();
		final int captionFontTop = -captionFontMetricsInt.top; // distances above baseline are negative
		final int captionFontBottom = captionFontMetricsInt.bottom;

		float cx = getWidth() / 2f;
		float cy = (getHeight() - (captionFontBottom + captionFontTop)) / 2f;

		paint.setColor(mTextColor);
		canvas.drawText(this.bottomtext, cx, 2 * cy + captionFontTop, paint);

		paint.setStrokeWidth(2);
		paint.setColor(mColor);
		paint.setAlpha(128);
		paint.setStyle(Paint.Style.FILL);  

		canvas.drawCircle(cx, cy, cy, paint);
		paint.setAlpha(32);

		paint.setColor(mNumberColor);
		paint.setTextSize(cy);
		Rect textBounds = new Rect();
		String text;
		if (this.decimals >= 0){
			text = number < 0 ? "?" : String.format(String.format("%%.%df", decimals), number);
			paint.getTextBounds("1", 0, 1, textBounds); // vertically align any value, with and without decimal separator
			canvas.drawText(text, cx, cy + textBounds.height() / 2f, paint);
		} else {
			if (this.number > -90 && this.number < 90) {
				text = "\u279E";  // Heavy Triangle-headed Rightwards Arrow
			} else {
				this.number = this.number > 0 ? 90.0 : -90.0;  // cap angle at 90° ...
				text = "\u279F";  // ... and use Dashed Triangle-headed Rightwards Arrow to visualize the capping
			}
			canvas.save();
			canvas.rotate((float)-this.number, cx, cy);
			paint.getTextBounds(text, 0, text.length(), textBounds);
			canvas.drawText(text, cx, cy - (textBounds.bottom + textBounds.top)/ 2f, paint);
			canvas.restore();
		}
	}


    public void setColor(int color) {
        this.mColor = color;
    }
}
