package com.chensi.box.widget;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.customview.BoxPage.BoxRequest;
import com.chensi.box.manager.TouchEventManager;
import com.chensi.box.manager.TouchEventManager.OnClickEvent;
import com.chensi.box.manager.TouchEventManager.OnLongClickEvent;
import com.chensi.box.manager.TouchEventManager.OnTouchEvent;
import com.chensi.box.util.BoxUtil;
import com.chensi.box.manager.AnimationManager.AnimationInfo;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLUtils;

public abstract class Box {
	
	public static final int TOUCH_STATE_IDLE = 0;
	public static final int TOUCH_STATE_PRESSED = 1;
	
	public static final int DRAW_NO = 0;
	public static final int DRAW_NEED = 1;
	public static final int DRAW_ANIMATION = 2;
	
	public static final int ALIGN_CUSTOM = 0;
	public static final int ALIGN_LEFT = 1;
	public static final int ALIGN_RIGHT = 2;
	public static final int ALIGN_CENTER_HORIZON = 4;
	public static final int ALIGN_TOP = 8;
	public static final int ALIGN_BOTTOM = 16;
	public static final int ALIGN_CENTER_VERTICAL = 32;
	public static final int ALIGN_CENTER = 36;
	
	
	
	private BoxBinderActivity mBoxBinderActivity;
	
	protected Rect rectLocation;
	private Rect rectReserved;
	private BoxRequest mRequest;
	
	protected OnClickEvent mOnClickEvent;
	protected OnLongClickEvent mOnLongClickEvent;
	protected OnTouchEvent mOnTouchEvent;

	protected AnimationInfo aniInfo;
	
	protected boolean isInit = false;
	protected boolean visible = true;
	protected boolean ignoreTouch = false;
	protected int state;
	private boolean ignoreScaledDraw = false;

	private int drawingTextureNumber = 0;

	protected boolean needInitTexture = true;
	
	protected FloatBuffer vertexBuffer;
	protected ByteBuffer indexBuffer;
	protected FloatBuffer[] textureBuffer;

    protected int[] textureName;

    private byte[] index = {
            0, 1, 2,
            0, 2, 3
    };
    
    
	
	public Box(BoxBinderActivity boxBinderActivity, Rect location) {
		mBoxBinderActivity = boxBinderActivity;
		rectLocation = location;
		rectReserved = new Rect(location);
	}
	
	protected FloatBuffer getVectexSize(Rect rect) {
		float width = (float)rect.width() * 2 / 1000;
		float height = (float)rect.height() * 2 / 1000;

	    float[] vertices = {
	            width - 1f, height - 1f, 0f,
	            -1f, height - 1f, 0f,
	            -1f, -1f, 0f,
	            width - 1f, -1f, 0f
	    };
		return getFloatBufferFromFloatArray(vertices);
	}
	

	protected void initTexture(GL10 gl10, int n) {
		initTexture(gl10, n, rectLocation);
	}
	
	protected void initTexture(GL10 gl10, int n, Rect rect) {
		if (isInit) return;
		
		if (textureName != null) {
			gl10.glDeleteTextures(textureName.length, textureName, 0);
		}
		
		isInit = true;
		indexBuffer = getByteBufferFromByteArray(index);
		vertexBuffer = getVectexSize(rect);
		textureBuffer = new FloatBuffer[n];
		textureName = new int[n];
		gl10.glGenTextures(textureName.length, textureName, 0);
		
		// �̰� setTexture�� �Űܾ� �Ѵ�
		float[] texture = {
	            1.0f, 0.0f,
	            0.0f, 0.0f,
	            0.0f, 1.0f,
	            1.0f, 1.0f,
	    }; 
		for (int i = 0; i < n; i++) {
			textureBuffer[i] = getFloatBufferFromTextureArray(texture);
		}
	}
	
	protected void setTexture(GL10 gl10, int pos, Bitmap bmp) {
//		gl10.glDeleteTextures(pos, textureName, 0);
		
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, textureName[pos]);
//        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
//        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
	}
	
	protected void setDrawingTexture(int pos) {
		drawingTextureNumber = pos;
	}
	
	protected int getWidthPixel() {
		return (int) (BoxUtil.getScreenWidth() * rectLocation.width() / BoxUtil.MAP_WIDTH);
	}
	
	protected int getHeightPixel() {
		return (int) (BoxUtil.getScreenHeight() * rectLocation.height() / BoxUtil.MAP_HEIGHT);
	}
	
	protected void requestDraw() {
		needInitTexture = true;
		isInit = false;
	}
	
	protected BoxRequest getBoxRequest() {
		return mRequest;
	}
	
	protected void deleteTexture(int num, int[] textureName) {
		mRequest.requestDeleteTexture(num + 1, textureName);
	}
	
	protected void setScissor(GL10 gl10, int left, int top, int right, int bottom) {
		mRequest.setScissor(gl10, left, top, right, bottom);
	}
	
	// draw to canvas
	public void draw(GL10 gl10, Paint paint) {
		onDraw(gl10, paint, new Rect(rectLocation), drawingTextureNumber);
	}

	// Draw with offset
	public void draw(GL10 gl10, Paint paint, int left, int top) {
		rectLocation.offset(left, top);
		onDraw(gl10, paint, rectLocation, drawingTextureNumber);
		rectLocation.offset(-left, -top);
	}
	
	// draw ignore left and top
	public void drawNoOffset(GL10 gl10, Paint paint) {
		Rect rect = new Rect(0, 0, rectLocation.width(), rectLocation.height());
		onDraw(gl10, paint, rect, drawingTextureNumber);
	}
	
	protected void onDraw(GL10 gl10, Paint paint, Rect rect, int textureNumber) {
		onDraw(gl10, paint, rect, 1f, 1f, 0, textureNumber);
	}
	
	protected void onDraw(GL10 gl10, Paint paint, Rect rect, float scaleX, float scaleY, float angle, int textureNumber) {
		if (needInitTexture) {
			needInitTexture = false;
			prepareTexture(gl10, paint);
//			return;
		}

		if (!isInit) return;
		
		FloatBuffer FBSize = getFBSize(textureNumber);
		// init
//    	gl10.glMatrixMode(GL10.GL_MODELVIEW);
//        gl10.glLoadIdentity();
        
		gl10.glFrontFace(GL10.GL_CW);
        gl10.glVertexPointer(3, GL10.GL_FLOAT, 0, FBSize);

        gl10.glEnable(GL10.GL_BLEND);
        gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, textureName[textureNumber]);
        gl10.glEnable(GL10.GL_ALPHA_TEST);
        gl10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer[textureNumber]);
        gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        
        float left = (float)rect.left * 2f / 1000f;
        float top = -(float)rect.bottom * 2f / 1000f + 2f;

        gl10.glPushMatrix();
        gl10.glTranslatef(left, top, 0f);
        gl10.glScalef(scaleX, scaleY, 1f);

        gl10.glDrawElements(GL10.GL_TRIANGLES, index.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);

        gl10.glPopMatrix();
//        gl10.glScalef(1f / scaleX, 1f / scaleY, 1f);
//        gl10.glTranslatef(-left, -top, 0f);

        //        gl10.glLoadIdentity();
        
        gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl10.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
	
	protected FloatBuffer getFBSize(int textureNumber) {
		return vertexBuffer;
	}
	
	protected abstract void prepareTexture(GL10 gl10, Paint paint);
	
	public void skipDraw() {
		
	}
	
	public boolean contains(int x, int y) {
		if (visible && !ignoreTouch) {
			return rectLocation.contains(x, y);
		} else {
			return false;
		}
	}
	
	public boolean contains(int x, int y, int offsetX, int offsetY) {
		return this.contains(x - offsetX, y - offsetY);
	}
	
	public void click() {
		if (mOnClickEvent != null) {
			mOnClickEvent.onClick(this);
		}
	}
	
	public void longClick() {
		if (mOnLongClickEvent != null) {
			mOnLongClickEvent.onLongClick(this);
		}
	}
	
	public boolean touch(int action, int x, int y, int id) {
		if (action == TouchEventManager.ACTION_DOWN) {
			state = TOUCH_STATE_PRESSED;
		} else if (action == TouchEventManager.ACTION_UP ||
				action == TouchEventManager.ACTION_CANCEL) {
			state = TOUCH_STATE_IDLE;
		}
		
		if (mOnTouchEvent != null) {
			return mOnTouchEvent.onTouch(action, x, y, id);
		}
		
		return true;
	}
	
	public void applyLocation() {
		if (rectLocation == null) return;
		
		rectLocation.set(rectReserved);
	}
	
	protected void setTouchOffset(int x, int y) {
		if (mOnTouchEvent != null) {
			mOnTouchEvent.setOffset(x, y);
		}
	}
	
	public void cancelTouch() {
		state = TOUCH_STATE_IDLE;
		
		if (mOnTouchEvent != null) {
			mOnTouchEvent.cancelTouch();
		}
	}
	
	public void changeTouchState(int newState) {
		state = newState;
	}
	
	public void release() {
		if (textureName != null) {
			deleteTexture(0, textureName);
		}
		
		rectLocation = null;
		rectReserved = null;
		mRequest = null;
		mOnClickEvent = null;
		mOnLongClickEvent = null;
		if (mOnTouchEvent != null) {
			mOnTouchEvent.cancelTouch();
			mOnTouchEvent = null;
		}
		aniInfo = null;
	}
	
	protected boolean isVaild() {
		return rectLocation != null;
	}
	
	public void releaseGl(GL10 gl10) {
		gl10.glDeleteTextures(textureName.length, textureName, 0);
	}
	
	public void offset(int x, int y) {
		rectReserved.offset(x, y);
	}
	
	public void moveLocation(int x, int y) {
		rectReserved.offsetTo(x, y);
//		rectLocation.offsetTo(X, Y);
	}

	public void setLocation(int left, int top,int right, int bottom) {
		rectReserved.set(left, top, right, bottom);
//		rectLocation.set(left, top, right, bottom);
		requestDraw();
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public void setIgnoreScaledDraw(boolean isIgnore) {
		ignoreScaledDraw = isIgnore;
	}
	
	public void setHeight(int height) {
		rectReserved.set(rectLocation.left, rectLocation.top, rectLocation.right, rectLocation.top + height);
//		rectLocation.set(rectLocation.left, rectLocation.top, rectLocation.right, rectLocation.top + height);
		requestDraw();
	}
	
	public void setIgnoreTouch(boolean ignore) {
		ignoreTouch = ignore;
	}
	
	public int getWidth() {
		return rectLocation.width();
	}
	
	public int getHeight() {
		return rectLocation.height();
	}
	
	public int getLeft() {
		return rectLocation.left;
	}

	public int getTop() {
		return rectLocation.top;
	}
	
	public int getRight() {
		return rectLocation.right;
	}
	
	public int getBottom() {
		return rectLocation.bottom;
	}
	
	public Rect getRect() {
		return rectLocation;
	}
	
	public boolean getVisible() {
		return visible && mRequest.isParentVisible();
	}
	
	public boolean getIgnoreScaleDraw() {
		return ignoreScaledDraw;
	}
	
	public boolean canMultiTouch() {
		return false;
	}
	
	public int needDraw() {
		if (aniInfo != null && aniInfo.isEnable()) {
			return DRAW_ANIMATION;
		} else if (visible) {
			return DRAW_NEED;
		}
		
		return DRAW_NO;
	}
	
	protected BoxBinderActivity getBoxBinderActivity() {
		return mBoxBinderActivity;
	}
	
	public void runAnimation() {
		
	}
	
	public void setRequest(BoxRequest request) {
		mRequest = request;
	}
	
	public void setOnTouchEvent(OnTouchEvent onTouchEvent) {
		mOnTouchEvent = onTouchEvent;
	}
	
	public void setOnClickEvent(OnClickEvent onClickEvent) {
		mOnClickEvent = onClickEvent;
	}
	
	public void setOnLongClickEvent(OnLongClickEvent onLongClickEvent) {
		mOnLongClickEvent = onLongClickEvent;
	}
	
	public OnClickEvent getOnClickEvent() {
		return mOnClickEvent;
	}
	
	public OnLongClickEvent getOnLongClickEvent() {
		return mOnLongClickEvent;
	}
	
	public AnimationInfo getAnimation() {
		if (aniInfo == null) {
			aniInfo = new AnimationInfo();
		}
		
		return aniInfo;
	}
	
	public void closeAnimation() {
		if (aniInfo != null) {
			aniInfo.close();
		}
	}
	
	protected FloatBuffer getFloatBufferFromFloatArray(float array[]) {
        ByteBuffer tempBuffer = ByteBuffer.allocateDirect(array.length * 4);
        tempBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = tempBuffer.asFloatBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

	protected ByteBuffer getByteBufferFromByteArray(byte array[]) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(array.length);
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

	protected FloatBuffer getFloatBufferFromTextureArray(float texture[]) {
        ByteBuffer tbb = ByteBuffer.allocateDirect(texture.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = tbb.asFloatBuffer();
        buffer.put(texture);
        buffer.position(0);
        return buffer;
    }
	
	
	
}
