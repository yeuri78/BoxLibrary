package com.chensi.box.manager;

import android.content.Context;

public class ImageManager {

	private final static int HEADER_CS_DATA_FILE = 20150823;
	
	
	class ResInfo{
		public String name;
		public int pt;
		public int length;
	}
	
	private static ImageManager mImageManager;
	
	
	private ResInfo[] resInfos;
	
	
	
	public static void init(Context context, int assetRes) {
		mImageManager = new ImageManager();
	}
	
	private void setData(Context context, int assetRes) {
		
		
		
		char[] bData = null;
		int pt = 0;
		
		int header = getInt(bData, 0);
		if (header != HEADER_CS_DATA_FILE) return;
		int version = getInt(bData, 4);
		int fileCount = getInt(bData, 8);
		resInfos = new ResInfo[fileCount];
		pt = 12;
		for (int i = 0; i < fileCount; i++) {
			resInfos[i] = new ResInfo();
			
			int endPt = findDivider(bData, pt, '#');
			resInfos[i].name = String.copyValueOf(bData, pt, pt - endPt);
			pt = endPt + 1;
			resInfos[i].pt = getInt(bData, pt);
			pt += 4;
			resInfos[i].length = getInt(bData, pt);
			pt += 4;
		}
		
		
		
	}
	
	private int getInt(char[] bData, int offset) {
		return 0;
	}
	
	private int getLong(char[] bData, int offset) {
		return 0;
	}
	
	private int findDivider(char[] bData, int offset, char divider) {
		while(true) {
			if (bData[offset] == divider) {
				return offset;
			}
			
			offset++;
		}
	}
	
	
}
