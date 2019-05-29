package com.chensi.box.manager;

import java.io.IOException;
import java.util.Hashtable;

import com.chensi.box.util.BoxUtil;
import com.chensi.box.widget.Box;
import com.chensi.box.widget.MapBox.CMapData;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.AssetManager.AssetInputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BoxResourceManager {

	private interface IInputFunc {
		int readInt(AssetInputStream inp);
		byte[] readByteArray(AssetInputStream inp);
		String readString(AssetInputStream inp);
	}
	
	private static final int TYPE_IMAGE = 1;
	private static final int TYPE_TEXT = 2;
	private static final int TYPE_SPRITE = 11;
	private static final int TYPE_MAP = 12;
	
	
	
	
	private static BoxResourceManager mResourceManager;
	
	public static BoxResourceManager getInstance() {
		if (mResourceManager == null) {
			mResourceManager = new BoxResourceManager();
		}
		
		return mResourceManager;
	}
	
	
	
	private IInputFunc mInputFunc = new IInputFunc() {
		
		@Override
		public String readString(AssetInputStream inp) {
			byte[] bData = readByteArray(inp);
			String name = new String(bData);

			return name;
		}
		
		@Override
		public int readInt(AssetInputStream inp) {
			byte[] bData = new byte[4];
			try {
				inp.read(bData);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return BoxUtil.parceInt(bData);
		}
		
		@Override
		public byte[] readByteArray(AssetInputStream inp) {
			int size = readInt(inp);
			byte[] bData = new byte[size];
			try {
				inp.read(bData);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return bData;
		}
	};
	
	private IInputFunc mInputSkip = new IInputFunc() {
		
		@Override
		public String readString(AssetInputStream inp) {
			readByteArray(inp);

			return "";
		}
		
		@Override
		public int readInt(AssetInputStream inp) {
			byte[] bData = new byte[4];
			try {
				inp.read(bData);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return BoxUtil.parceInt(bData);
		}
		
		@Override
		public byte[] readByteArray(AssetInputStream inp) {
			int size = readInt(inp);
			try {
				inp.skip(size);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}
	};
	
	
	
	private String assetName = null;
	
	private BoxResourceManager() {
		
	}
	
	public void setAssetName (String name) {
		assetName = name;
	}
	
	
	
	private AssetInputStream getInput(Context context, String itemName, int assetType) {
		AssetInputStream input;
		try {
			input = (AssetInputStream) context.getAssets().open(assetName, AssetManager.ACCESS_RANDOM);
			while(input.available() > 0) {
				int type = mInputFunc.readInt(input);
				String name = mInputFunc.readString(input);
				if (type == assetType && name.equals(itemName)) {
					return input;
				}
				switch (type) {
					case TYPE_IMAGE:
						getBitmapFromAsset(input, mInputSkip);
						break;
					case TYPE_TEXT:
						getTextFromAsset(input, mInputSkip);
						break;
					case TYPE_MAP:
						getMapFromAsset(input, mInputSkip);
						break;
					default:
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Asset is not found.");
		}
		
		return null;
	}
	
	private Bitmap getBitmapFromAsset(AssetInputStream input, IInputFunc func) {
//		int size = func.readInt(input);
//Log.i("ttttt", "size " + size);
		byte[] bImage = func.readByteArray(input);
		if (bImage == null) {
			return null;
		}
		
		return BitmapFactory.decodeByteArray(bImage, 0, bImage.length);
	}
	
	private String[] getTextFromAsset(AssetInputStream input, IInputFunc func) {
		int size = func.readInt(input);
		String[] texts = new String[size];
		for (int i = 0; i < size; i++) {
			texts[i] = func.readString(input);
		}
		
		return texts;
	}
	
	private CMapData getMapFromAsset(AssetInputStream input, IInputFunc func) {
		CMapData mapData = new CMapData();
		
		return mapData;
	}
	

	
	
	public Bitmap getBitmap(Context context, String name) {
		AssetInputStream input = getInput(context, name, TYPE_IMAGE);
		if (input == null) return null;

		Bitmap bmp = getBitmapFromAsset(input, mInputFunc);
		try {
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bmp;
	}
	
	public String[] getTest(Context context, String name) {
		AssetInputStream input = getInput(context, name, TYPE_TEXT);
		if (input == null) return null;

		String[] text = getTextFromAsset(input, mInputFunc);
		try {
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text;
	}
	
	
	public void release() {
		
	}
	
	
	
	
	class ResData{
		public int bodyStart;
		public int bodyLength;
		
	}
	
	
}
