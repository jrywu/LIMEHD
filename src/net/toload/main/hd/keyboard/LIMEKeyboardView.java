/*    
**    Copyright 2010, The LimeIME Open Source Project
** 
**    Project Url: http://code.google.com/p/limeime/
**                 http://android.toload.net/
**
**    This program is free software: you can redistribute it and/or modify
**    it under the terms of the GNU General Public License as published by
**    the Free Software Foundation, either version 3 of the License, or
**    (at your option) any later version.

**    This program is distributed in the hope that it will be useful,
**    but WITHOUT ANY WARRANTY; without even the implied warranty of
**    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
**    GNU General Public License for more details.

**    You should have received a copy of the GNU General Public License
**    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.toload.main.hd.keyboard;

import net.toload.main.hd.R;
import net.toload.main.hd.keyboard.LIMEBaseKeyboard.Key;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * @author Art Hung
 */
public class LIMEKeyboardView extends LIMEKeyboardBaseView {
	static final boolean DEBUG = false;
	static final String TAG = "LIMEKeyboardView";

	public static final int KEYCODE_OPTIONS = -100;
	public static final int KEYCODE_SHIFT_LONGPRESS = -101;
	public static final int KEYCODE_SPACE_LONGPRESS = -102;
    public static final int KEYCODE_NEXT_IM = -104;
    public static final int KEYCODE_PREV_IM = -105;
    
	//static final String PREF = "LIMEXY";
	
	//private boolean mLongPressProcessed;
	
   // private Keyboard mPhoneKeyboard;
  
    private int mKeyHeight=0;

	public LIMEKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mKeyHeight = context.getResources().getDimensionPixelSize(R.dimen.key_height);
	}

	public LIMEKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mKeyHeight = context.getResources().getDimensionPixelSize(R.dimen.key_height);
	}

	@Override
	protected boolean onLongPress(Key key) {
		if(DEBUG)
			Log.i(TAG, "onLongPress, keycode = "+ key.codes[0] 
				+"; spaceDragDiff = " +((LIMEKeyboard) this.getKeyboard()).getSpaceDragDiff()
				+"; key_height = " + mKeyHeight
					);
		if (key.codes[0] == LIMEBaseKeyboard.KEYCODE_CANCEL) {
			getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null,0,0);
			return true;
		}else if (key.codes[0] == LIMEKeyboard.KEYCODE_SPACE
				&& Math.abs(((LIMEKeyboard) this.getKeyboard()).getSpaceDragDiff() ) < mKeyHeight/5){ //Jeremy '12,4,23 avoid small move blocking the long press.
			getOnKeyboardActionListener().onKey(KEYCODE_SPACE_LONGPRESS, null,0,0);
			return true;
//		} else if (key.codes[0] == Keyboard.KEYCODE_SHIFT) {
//            getOnKeyboardActionListener().onKey(KEYCODE_SHIFT_LONGPRESS, null,0,0);
//            mLongPressProcessed = true;
//            // invalidateAllKeys require API 4 (> 1.5). Use setkeyboard(getKeyboard()) instead, which will also invalidateAllKeys.
//            //invalidateAllKeys();
//            return true;
		} else {
			return super.onLongPress(key);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.inputmethodservice.KeyboardView#onTouchEvent(android.view.MotionEvent
	 * )
	 */
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		if(DEBUG) Log.i(TAG, "OnTouchEvent(), me.getAction() =" + me.getAction());
		LIMEKeyboard keyboard = (LIMEKeyboard) getKeyboard();
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			if(DEBUG) Log.i(TAG, "OnTouchEvent(), ACTION_DOWN");
			keyboard.keyReleased();
		}

		if (me.getAction() == MotionEvent.ACTION_UP) {
			int spaceDrageDirection = keyboard.getSpaceDragDirection();
			if(DEBUG) Log.i(TAG, "OnTouchEvent(), ACTION_UP, spaceDragDirection:" + spaceDrageDirection);
			if (spaceDrageDirection != 0) {
				getOnKeyboardActionListener().onKey(
						spaceDrageDirection == 1 ? KEYCODE_NEXT_IM : KEYCODE_PREV_IM,
								null,0,0);
				me.setAction(MotionEvent.ACTION_CANCEL);
				keyboard.keyReleased();
				return super.onTouchEvent(me);
			}
		}
		return super.onTouchEvent(me);
	}
	
	
//	  public void setPhoneKeyboard(Keyboard phoneKeyboard) {
//	        mPhoneKeyboard = phoneKeyboard;
//	    }

}
