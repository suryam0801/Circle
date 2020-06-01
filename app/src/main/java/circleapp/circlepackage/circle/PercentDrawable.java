package circleapp.circlepackage.circle;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

public class PercentDrawable extends Drawable {

    private final int percent;
    private final Paint paint;
    private final String colorCode;

    public PercentDrawable(int percent, String colorCode) {
        super();

        this.percent = percent;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.colorCode = colorCode;
        this.paint.setColor(Color.parseColor(colorCode));
    }

    @Override
    public void draw(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(0, 0, (percent * canvas.getWidth()) / 100, canvas.getHeight(),15, 15, paint);
        }
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