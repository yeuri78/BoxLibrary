package com.chensi.box.util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.chensi.box.widget.Box;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.util.Log;

public class BoxUtil {

	public static final int MAP_WIDTH = 1000;
	public static final int MAP_HEIGHT = 1000;
	
	private static int touchedColor = Color.argb(100, 0, 0, 255);
	private static boolean testMode = false;
	private static int screenWidth = 0, screenHeight = 0;
	private static float screenDensity;
	
	// ************ TextBox ************ 
	
	// divide text and return start position.
	public static int[] getDivideLoc(String text, Paint paint, int width) {
		float[] arrayWidth = new float[text.length()];
		int[] arrayIndexOfStart = new int[text.length()];
		int[] rst;
		int rstPos = 0;
		float totalWidth = 0;
		paint.getTextWidths(text, arrayWidth);
		
		// caculate line
		for (int index = 0; index < arrayWidth.length; index++) {
			totalWidth += arrayWidth[index];
			if ("\n".equals(text.substring(index, index + 1))) {
				arrayIndexOfStart[rstPos] = index + 1;
				totalWidth = 0;
				rstPos++;
			} else if (totalWidth >= (float)width) {
				arrayIndexOfStart[rstPos] = index;
				totalWidth = arrayWidth[index];
				rstPos++;
			}
		}
		
		// create result
		rst = new int[rstPos + 1];
		rst[0] = 0;
		for (int index = 0; index < rstPos; index++) {
			rst[index + 1] = arrayIndexOfStart[index];
		}
		
		return rst;
	}
	
	public static float getTextWidth(String text, Paint paint) {
		float[] arrayWidth = new float[text.length()];
		float totalWidth = 0;
		
		paint.getTextWidths(text, arrayWidth);
		for (float charWidth : arrayWidth) {
			totalWidth += charWidth;
		}
		return totalWidth; 
	}
	
	// Draw Text
	public static void drawText(Canvas canvas, Paint paint, Rect rect, String text, int align, int topMargin) {
		if (text.length() <= 0) {
			return;
		}

		// if (needUpdate) {
		int[] indexOfStart = BoxUtil.getDivideLoc(text, paint, rect.width());
		
		switch(align) {
		case Box.ALIGN_CENTER_VERTICAL:
		case Box.ALIGN_CENTER:
			topMargin += (int) ((rect.height() - indexOfStart.length * paint.getTextSize()) / 2 - paint.getTextSize() / 8);
			if (topMargin < 0) {
				topMargin = 0;
			}
			break;
		default:
		}

		for (int index = 0; index < indexOfStart.length; index++) {
			if ((index + 1) * paint.getTextSize() + topMargin > rect.height())
				break;

			int start = indexOfStart[index];
			int end, left, width;
			// remove /\
			if (text.length() > start + 1 && "\n".equals(text.substring(start, start + 1))) {
				start++;
			}
			if (index + 1 < indexOfStart.length) {
				end = indexOfStart[index + 1];
			} else {
				end = text.length();
			}
			
			if (start < end) { // Avoid same position.
				// align
				width = (int) paint.measureText(text.substring(start, end));
				switch (align) {
				case Box.ALIGN_RIGHT:
					left = rect.right - width;
					break;
				case Box.ALIGN_CENTER:
				case Box.ALIGN_CENTER_HORIZON:
					left = rect.left + (rect.width() - width) / 2;
					break;
				case Box.ALIGN_CUSTOM:
				default:
					left = rect.left;
					break;
				}
				
				canvas.drawText(text.substring(start, end), left, rect.top + (index + 1) * paint.getTextSize() + topMargin, paint);
			}
		}
	}
	
	
	// ************ ListBox ************
	
	public static Bitmap getPressedImage(Bitmap bOri) {
		Bitmap bRst = Bitmap.createBitmap(bOri);
		Canvas canvas = new Canvas(bRst);
		canvas.drawColor(touchedColor);
		
		return bRst;
	}
	
	public static Bitmap getPressedImageSmall(Bitmap bOri) {
		Rect rectDest = new Rect(bOri.getWidth() / 10, bOri.getHeight() / 10, bOri.getWidth() * 9 / 10, bOri.getHeight() * 9 / 10);
		Bitmap bRst = Bitmap.createBitmap(bOri.getWidth(), bOri.getHeight(), Config.ARGB_4444);
		Canvas canvas = new Canvas(bRst);
		canvas.drawARGB(0, 255, 255, 255);
		canvas.drawBitmap(bOri, new Rect(0, 0, bOri.getWidth(), bOri.getHeight()), rectDest, new Paint());
		
		return bRst;
	}
	
	// ************ ImageBox ************
	
	public static Bitmap getImageFromResource(Context context, String name, int id, Rect rect){
		String fName = name + "_" + rect.width() + "_" + rect.height() + ".png";
		File file = new File(context.getFilesDir(), fName);
		
		if (file.exists()) {
			return BitmapFactory.decodeFile(context.getFilesDir() + "/" + fName);
		} else {
			Bitmap bSrc = BitmapFactory.decodeResource(context.getResources(), id);
//			Bitmap bDest = Bitmap.createScaledBitmap(bSrc, rect.width(), rect.height(), true);
//			bSrc.recycle();
			
			Bitmap bDest = Bitmap.createBitmap(rect.width(), rect.height(), Config.ARGB_8888);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setFilterBitmap(true);
			Canvas canvas = new Canvas(bDest);
			canvas.drawBitmap(bSrc, new Rect(0, 0, bSrc.getWidth(), bSrc.getHeight()), 
					new Rect(0, 0, rect.width(), rect.height()), paint);
			bSrc.recycle();
	
			// Write file
			try {
				FileOutputStream fos = new FileOutputStream(file);
				bDest.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return bDest;
		}
	}
	
	public static void loadImageFromResource(Context context, String name, int id, Rect rect){
		String fName = name + "_" + rect.width() + "_" + rect.height() + ".png";
		File file = new File(context.getFilesDir(), fName);
		
		if (!file.exists()) {
			Bitmap bSrc = BitmapFactory.decodeResource(context.getResources(), id);
			
			Bitmap bDest = Bitmap.createBitmap(rect.width(), rect.height(), Config.ARGB_8888);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setFilterBitmap(true);
			Canvas canvas = new Canvas(bDest);
			canvas.drawBitmap(bSrc, new Rect(0, 0, bSrc.getWidth(), bSrc.getHeight()), 
					new Rect(0, 0, rect.width(), rect.height()), paint);
			bSrc.recycle();
	
			// Write file
			try {
				FileOutputStream fos = new FileOutputStream(file);
				bDest.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			bDest.recycle();
		}
	}
	
	// ************ Common ************
	
	public static void removeAllLocalFiles(Context context) {
		for (String path : context.getFilesDir().list()) {
			File f = new File(context.getFilesDir(), path);
			f.delete();
		}
	}
	
	public static void testModeOn() {
		testMode = true;
	}
	
	public static void log(String log) {
		if (testMode) {
			Log.i("testMode", log);
		}
	}
	
	
	
	public static void setScreenSize(int width, int height, float density) {
		screenWidth = width;
		screenHeight = height;
		screenDensity = density;
	}
	
	public static int getScreenWidth() {
		return screenWidth;
	}
	
	public static int getScreenHeight() {
		return screenHeight;
	}

	public static int getMapSizeY(int size) {
		return size * MAP_HEIGHT / screenHeight;
	}
	
	public static float getMapSizeY(float size) {
		return size * (float)MAP_HEIGHT / (float)screenHeight;
	}
	
	public static int getMapSizeX(int size) {
		return size * MAP_WIDTH / screenWidth;
	}
	
	public static float getMapSizeX(float size) {
		return size * (float)MAP_WIDTH / (float)screenWidth;
	}
	
	public static float getMapSizeYByDP(int size) {
		return size * MAP_HEIGHT / screenHeight * screenDensity;
	}
	
	public static float getMapSizeXByDP(int size) {
		return size * MAP_WIDTH / screenWidth * screenDensity;
	}

	public static int getRealSizeY(int size) {
		return size * screenHeight / MAP_HEIGHT;
	}
	
	public static float getRealSizeY(float size) {
		return size * (float)screenHeight / (float)MAP_HEIGHT;
	}

	public static int getRealSizeX(int size) {
		return size * screenWidth / MAP_WIDTH;
	}
	
	public static float getRealSizeX(float size) {
		return size * (float)screenWidth / (float)MAP_WIDTH;
	}
	
	
	public static int parceInt(byte[] bData) {
		int s1 = bData[0] & 0xFF;
        int s2 = bData[1] & 0xFF;
        int s3 = bData[2] & 0xFF;
        int s4 = bData[3] & 0xFF;

        return (s4 << 24) + (s3 << 16) + (s2 << 8) + s1;
	}
	
	
}
