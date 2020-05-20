package circleapp.circlepackage.circle;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class PercentDrawable extends Drawable {

    private final int percent;
    private final Paint paint;

    public PercentDrawable(int percent) {
        super();

        this.percent = percent;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paint.setColor(Color.parseColor("#D8E9FF"));
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRect(0, 0, percent * canvas.getWidth() / 100, canvas.getHeight(), paint);
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @SuppressLint("WrongConstant")
    @Override
    public int getOpacity() {
        return 0;
    }
}