package com.chensi.box.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.chensi.box.manager.BoxResourceManager;

public class BgManager {
    private static final int TYPE_NONE = 0;
    private static final int TYPE_RES = 1;
    private static final int TYPE_IMAGE = 2;
    private static final int TYPE_COLOR = 3;

    private int type;
    private int res;
    private String name;
    private int color;
    private float round;


    @Deprecated
    public void setRes(int _res) {
        res = _res;
        type = TYPE_RES;
    }

    public void setImage(String _name) {
        name = _name;
        type = TYPE_IMAGE;
    }

    public void setColor(int _color) {
        color = _color;
        type = TYPE_COLOR;
    }

    public void setRound(float _round) {
        round = _round;
    }

    public void clear() {
        type = TYPE_NONE;
    }

    public Bitmap getImage(Context context, int width, int height) {
        Bitmap bmp;

        switch (type) {
            case TYPE_RES:
                bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);
                Drawable drawable = context.getDrawable(res);
                drawable.setBounds(0, 0, width, height);
                drawable.draw(canvas);
                break;
            case TYPE_IMAGE:
                bmp = BoxResourceManager.getInstance().getBitmap(context, name);
                break;
            case TYPE_COLOR:
                bmp = BoxImageUtil.getRoundedBitmap(color, 0, width, height, false);
                break;
            default:
                return null;
        }

        if (round > 0) {
            bmp = BoxImageUtil.getRoundedBitmap(bmp, round);
        }

        return bmp;
    }
}
