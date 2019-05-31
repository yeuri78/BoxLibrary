package com.chensi.box.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class BoxCanvas {

    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint = new Paint();
    private int round = 0;

    public BoxCanvas create(int width, int height) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        return this;
    }

    public BoxCanvas setRound(int _round) {
        round = _round;
        return this;
    }

    public BoxCanvas setColor(int size) {
        paint.setColor(size);
        return this;
    }

    public BoxCanvas setTextSize(int size) {
        paint.setTextSize(size);
        return this;
    }

    public BoxCanvas drawColor(int color) {
        canvas.drawColor(color);
        return this;
    }

    public BoxCanvas drawText(int x, int y, String text) {
        canvas.drawText(text, x, y, paint);
        return this;
    }

    public BoxCanvas drawBitmap(Bitmap bitmap, Rect src, Rect dest) {
        canvas.drawBitmap(bitmap, src, dest, paint);
        return this;
    }




    public Bitmap toBitmap() {
        if (round > 0) {
            return BoxImageUtil.getRoundedBitmap(bitmap, round);
        }

        return bitmap;
    }

    public void clear() {
        bitmap = null;
        canvas = null;
    }

}
