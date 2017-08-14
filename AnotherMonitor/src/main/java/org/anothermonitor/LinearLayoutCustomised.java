/*
 * 2010-2017 (C) Antonio Redondo
 * http://antonioredondo.com
 * http://github.com/AntonioRedondo/AnotherMonitor
 *
 * Code under the terms of the GNU General Public License v3.
 *
 */

package org.anothermonitor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;


public class LinearLayoutCustomised extends LinearLayout {
	private boolean touchEventsDisabled = true;
	
//	public LinearLayoutCustomised(Context context) {
//		super(context);
//	}
	
	public LinearLayoutCustomised(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return touchEventsDisabled;
	}
	
	public void interceptChildTouchEvents(boolean b) {
		touchEventsDisabled = b;
	}
}
