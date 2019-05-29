package com.chensi.box.widget;

import javax.microedition.khronos.opengles.GL10;

import com.chensi.box.customview.BoxBinderActivity;
import com.chensi.box.customview.BoxPage.AfterDrawCallback;
import com.chensi.box.manager.TouchEventManager;
import com.chensi.box.manager.TouchEventManager.OnDismissEvent;
import com.chensi.box.util.BoxUtil;

import android.graphics.Paint;
import android.graphics.Rect;

public class DialogBox extends ContainerBox {
	
	public static final int BUTTON_1ST = 0;
	public static final int BUTTON_2ST = 1;
	public static final int BUTTON_3ST = 2;
	
	private static final int BACKGROUND_ALPHA = 100;
	private static final int ANIMATION_TIME_IN = 400;
	private static final int ANIMATION_TIME_OUT = 250;

	private Rect rectContainer;
	private int layoutAlign = Box.ALIGN_CENTER;
	private boolean isBlockBackgroundTouch = false;
	private boolean withAnimation = true;
	
	private OnDismissEvent mOnDismissEvent;
	
	private TextBox txtTitle;
	private ButtonBox[] btnCommand;
	private int backgroundAlpha = BACKGROUND_ALPHA;
//	private int buttonHeight = 50;
	
	
	public DialogBox(BoxBinderActivity boxBinderActivity, Rect location) {
		super(boxBinderActivity, location);
		rectContainer = new Rect(location);
		super.visible = false;
		super.padding = (int) BoxUtil.getMapSizeYByDP(4);
		
		initTitleAndButtons(boxBinderActivity, rectContainer);
	}
	
	// Ignore offset from page.
//	@Override
//	public void draw(GL10 gl10, Paint paint, float left, float top) {
//		if (super.getAnimation().isEnable()) {
//			super.draw(gl10, paint, left, top);
//		} else {
//			super.draw(gl10, paint);
//		}
//	}
	
	@Override
	public boolean contains(int x, int y, int offsetX, int offsetY) {
		if (super.getVisible()) {
			return true;
		} else if (super.getAnimation().isEnable()) {
			return super.contains(x, y, offsetX, offsetY);
		}
		
		return false;
	}
	
	@Override
	public boolean contains(int x, int y) {
		if (super.getVisible()) {
			return true;
		}
		
		return false;
	}

	private void initTitleAndButtons(BoxBinderActivity mBoxBinderActivity, Rect location) {
		txtTitle = new TextBox(mBoxBinderActivity, new Rect(10, 10, location.width() - 10, 50));

		txtTitle.setText("Title");
		txtTitle.setTextSize(20);
		txtTitle.setTextColor(0xFF000000);
		txtTitle.setTextAlign(TextBox.ALIGN_CENTER);
		btnCommand = new ButtonBox[3];
		btnCommand[0] = new ButtonBox(mBoxBinderActivity, new Rect(0, 0, 50, 30));
		btnCommand[1] = new ButtonBox(mBoxBinderActivity, new Rect(0, 0, 50, 30));
		btnCommand[2] = new ButtonBox(mBoxBinderActivity, new Rect(0, 0, 50, 30));
		btnCommand[0].setText("OK");
		btnCommand[1].setText("No");
		btnCommand[2].setText("Cancel");
		btnCommand[0].setTextSize(20);
		btnCommand[1].setTextSize(20);
		btnCommand[2].setTextSize(20);
		btnCommand[0].setVisible(false);
		btnCommand[1].setVisible(false);
		btnCommand[2].setVisible(false);
		
		super.addChildBox(txtTitle);
		for (ButtonBox btn : btnCommand) {
			super.addChildBox(btn);
		}
		
		super.setOnTouchEvent(TouchEventManager.getBoxTouchEvent(super.aBox, super.rectLocation.left, super.rectLocation.top));
	}

	
//	@Override
//	protected void onDraw(Canvas canvas, Paint paint, Rect rect) {
//		// Draw background
//		if (isDrawBackground && !super.getAnimation().isEnable()) {
//			canvas.drawARGB(backgroundAlpha, 0, 0, 0);
//		}
//		// Draw boxs
//		super.onDraw(canvas, paint, rect);
//	}
	
	@Override
	public boolean touch(int action, int x, int y, int id) {
		if (!isBlockBackgroundTouch && !super.contains(x, y) 
				&& (action == TouchEventManager.ACTION_DOWN)){
			dismiss();
			return true;
		}
		
		return super.touch(action, x, y, id);
	}

	@Override
	public void release() {
		withAnimation = false;
		dismiss();
		mOnDismissEvent = null;
//		for (Box box : btnCommand) {
//			box.release();
//		}
		btnCommand = null;
		super.release();
	}
	
	private boolean isCaculated = false;
	
	public void show() {
		AfterDrawCallback activityFunc = new AfterDrawCallback() {

			@Override
			public void callBack() {
				// Cancel animation.
				closeAnimation();
				
				// prepare show dialog.
				visible = true;
				if (!isCaculated) {
					caculateLayout();
					isCaculated = true;
				}
				
				// show animation.
				if (withAnimation) {
					getAnimation().setAnimationZoomIn(ANIMATION_TIME_IN);
				}
			}
		};
		
		super.getBoxRequest().addAfterDrawCallback(activityFunc);
//		requestDraw();
	}
	


	private void caculateLayout() {
		int showingBtnN = 0, nowBtnNum = 0;
		int widthBtn;
		int extendBottom = 0;
		
//		outlineMargin = txtTitle.getTextSize() / 3;
		// Title
		int offsetTop = BoxUtil.getMapSizeY(txtTitle.getTextSize()) + super.padding * 2;
		txtTitle.setLocation(0, 
				0, 
				super.getWidth() - super.padding, 
				offsetTop);

		// Buttons
		extendBottom = 0;
		for (ButtonBox btn : btnCommand) {
			if (btn.getVisible()) {
				showingBtnN++;
				if (extendBottom < btn.getTextSize()) {
					extendBottom = btn.getTextSize() * 5 / 4;
				}
			}
		}
		
		if (showingBtnN > 0) {
			int buttonHorMargin = 15;
			extendBottom = BoxUtil.getMapSizeY(extendBottom) * 5 / 4 + super.padding * 5;
			widthBtn = (super.getWidth() - buttonHorMargin * 2 - super.padding * (showingBtnN + 1)) / showingBtnN; //  + 10 * (showingBtnN + 1)
			for (ButtonBox btn : btnCommand) {
				if (btn.getVisible()) {
					btn.setLocation(super.padding * nowBtnNum + widthBtn * nowBtnNum + buttonHorMargin,
							rectContainer.height() + offsetTop,
							super.padding * nowBtnNum + widthBtn * (nowBtnNum + 1) + buttonHorMargin, 
							rectContainer.height() + offsetTop + extendBottom - super.padding * 3);
					
					nowBtnNum++;
				}
			}
		}

		// Set Dialog Height
		super.setHeight(rectContainer.height() + offsetTop + extendBottom);
		
		// Move child Boxs
		for (int index = 4; index < super.aBox.size(); index++) {
			super.aBox.get(index).offset(0, offsetTop);
		}
		
		// Locate dialog
		int left, top;
		switch (layoutAlign) {
		case Box.ALIGN_CENTER:
			left = (BoxUtil.MAP_WIDTH - super.getWidth()) / 2;
			top = (BoxUtil.MAP_HEIGHT - super.getHeight()) / 2;
			break;
		default:
			left = 0;
			top = 0;
			break;
		}
		
		super.moveLocation(left, top);
		super.setTouchOffset(left, top);
	}
	
	public void dismiss() {
		if (!super.visible) {
			return;
		}

		super.closeAnimation();
		super.visible = false;
		
		if (withAnimation) {
			super.getAnimation().setAnimationZoomOut(ANIMATION_TIME_OUT);
		}
		if (mOnDismissEvent != null) {
			mOnDismissEvent.onDismiss();
		}
	}
	
	public void setLayoutAlign(int align) {
		layoutAlign = align;
	}
	
	public void setBlockBackgroundTouch(boolean isBlock) {
		isBlockBackgroundTouch = isBlock;
	}
	
//	public void setDrawBackground(boolean isDraw) {
//		isDrawBackground = isDraw;
//	}
	
	public void setEnableAnimation(boolean enable) {
		withAnimation = enable;
	}
	
	public void setOnDismissEvent(OnDismissEvent onDismissEvent) {
		mOnDismissEvent = onDismissEvent;
	}
	
	public void setBackgroundAlpha(int alpha) {
		backgroundAlpha = alpha;
	}
	
	public TextBox getTitle() {
		return txtTitle;
	}
	
	public ButtonBox getButton(int position) {
		return btnCommand[position];
	}
	
	
	
	
	
}
