package com.chensi.box.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.chensi.box.R;

public class BoxImageUtil {


    public static Bitmap getRoundedBitmap(Bitmap src, float edgeSize) {
        final Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.GRAY;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, src.getWidth(), src.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
//	    canvas.drawOval(rectF, paint);
        canvas.drawRoundRect(rectF, edgeSize, edgeSize, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, rect, rect, paint);

        src.recycle();

        return output;
    }

    public static Bitmap getRoundedBitmap(int color, float edgeSize, int width, int height, boolean reduced) {
        float widthReal = width;//BoxUtil.getRealSizeX(width);
        float heightReal = height;//BoxUtil.getRealSizeY(height);

        // Reduce size
        float rate = 1;
        if (reduced) {
            if (widthReal > 500) {
                rate = 500 / widthReal;
            }
            if (heightReal > 500) {
                float rateHeight = 500 / heightReal;
                if (rateHeight > rate) {
                    rate = rateHeight;
                }
            }
        }

        widthReal *= rate;
        heightReal *= rate;

        // Create bitmap and draw color
        Bitmap bmp = Bitmap.createBitmap((int)widthReal, (int)heightReal, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(color);

        return getRoundedBitmap(bmp, edgeSize);
    }

}
