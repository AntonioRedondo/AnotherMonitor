

package org.monitrack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class ActivityAbout extends Activity {

	private BroadcastReceiver receiverFinish = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};

	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		final Resources res = getResources();
		
		if (Build.VERSION.SDK_INT >= 19) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			
			float sSW = res.getConfiguration().smallestScreenWidthDp;
			
			LinearLayout l = (LinearLayout) findViewById(R.id.LParent);
			int statusBarHeight = res.getDimensionPixelSize(res.getIdentifier(C.sbh, C.dimen, C.android));
			int navigationBarHeight = 0;
			
			if (!ViewConfiguration.get(this).hasPermanentMenuKey() && !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
					&& (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || sSW > 560)) {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
				navigationBarHeight = res.getDimensionPixelSize(res.getIdentifier(C.nbh, C.dimen, C.android));
				FrameLayout nb = (FrameLayout) findViewById(R.id.LNavigationBar);
				nb.setVisibility(View.VISIBLE);
				((FrameLayout.LayoutParams) nb.getLayoutParams()).height = navigationBarHeight;
			}
			
			l.setPadding(0, statusBarHeight, 0, navigationBarHeight);
		}
		

	}





	@Override
	public void onStart() {
		super.onStart();
		registerReceiver(receiverFinish, new IntentFilter(C.actionFinishActivity));
	}





	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiverFinish);
	}
}