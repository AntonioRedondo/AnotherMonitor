/* 
 * 2010-2017 (C) Antonio Redondo
 * http://antonioredondo.com
 * http://github.com/AntonioRedondo/AnotherMonitor
 *
 * Code under the terms of the GNU General Public License v3.
 *
 */

package org.anothermonitor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

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
		
		// http://stackoverflow.com/questions/4790746/links-in-textview
		Linkify.addLinks((TextView) findViewById(R.id.TVAboutText), Linkify.WEB_URLS);
//		Linkify.addLinks((TextView) findViewById(R.id.TVAboutText), Pattern.compile(getString(R.string.about_gnugpl_text)), getString(R.string.about_gnugpl_link));
		
		(findViewById(R.id.BGooglePlay)).setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(C.marketDetails + getPackageName()))
							.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.google_play_app_site)))
							.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
				}
			}
		});
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