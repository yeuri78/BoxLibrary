package com.chensi.box.manager;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

public class MusicManager {

	private static Context mContext;
	private static MediaPlayer music = null;
	
	private static ArrayList<Integer> aMusicResource = new ArrayList<Integer>();
	private static int nowMusic = -1;
	private static boolean modeRandom = false;
	
	private static Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			nextTrack();
			
			music.release();
			music = null;
			play(mContext);
		};	
	};
	
	public static void init() {
	}
	
	public static void addMusicResource(int res) {
		aMusicResource.add(res);
	}
	
	public static void play(Context context) {
		if (context != null) {
			mContext = context;
		}
		if (mContext == null){
			Log.e("BoxLibrary", "MusicManager can't play music bacause context is null.");
			return;
		}
		
		nextTrack();
		if (music == null) {
			music = MediaPlayer.create(context, aMusicResource.get(nowMusic));
			music.setLooping(false);
		}
		music.start();
		
		int delayMili = music.getDuration() - music.getCurrentPosition();
		mHandler.sendEmptyMessageDelayed(0, delayMili);
	}
		
	public static void pause() {
		if (music != null) {
			music.pause();
		}
		mHandler.removeMessages(0);
		mContext = null;
	}
	
	public static void setRandomPlay(boolean isRandom) {
		modeRandom = isRandom;
	}
	
	private static void nextTrack() {
		if (modeRandom) {
			Random rand = new Random();
			nowMusic = rand.nextInt(aMusicResource.size());
		} else {
			nowMusic++;
			if (nowMusic >= aMusicResource.size()) {
				nowMusic = 0;
			}
		}
	}
	
	
}
