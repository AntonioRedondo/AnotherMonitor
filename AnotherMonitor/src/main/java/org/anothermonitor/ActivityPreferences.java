/*
 * 2010-2015 (C) Antonio Redondo
 * http://antonioredondo.com
 * https://github.com/AntonioRedondo/AnotherMonitor
 *
 * Code under the terms of the GNU General Public License v3.
 *
 */

package org.anothermonitor;

import android.R.drawable;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

/**
 * Class not used. Preferences have been moved to the app bottom slide menu.
 */

public class ActivityPreferences extends Activity {
	private int previousSelected, currentItem =-1, navigationBarHeight;
	private float sSW;
	private LinearLayout mLTabs;
	private CheckBox mCBMemFreeD, mCBBuffersD, mCBCachedD, mCBActiveD, mCBInactiveD, mCBSwapTotalD, mCBDirtyD, mCBCpuTotalD/*, mCBCPURestD*/, mCBCpuAMD;
	private Spinner mSRead, mSUpdate, mSWidth;
	private ViewPager mVP;
	private SharedPreferences mPrefs;
	private Resources res;
	private Bundle mB;
	
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
		mPrefs = getSharedPreferences(getString(R.string.app_name) + "Prefs", MODE_PRIVATE);
		res = getResources();
		navigationBarHeight = res.getDimensionPixelSize(res.getIdentifier("navigation_bar_height", "dimen", "android"));

		mLTabs = (LinearLayout) findViewById(R.id.LTabs);
		
		if (Build.VERSION.SDK_INT >= 19) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			
			sSW = res.getConfiguration().smallestScreenWidthDp;
			
			int statusBarHeight = res.getDimensionPixelSize(res.getIdentifier("status_bar_height", "dimen", "android"));
			
			if (!ViewConfiguration.get(this).hasPermanentMenuKey()
					&& (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || sSW > 560)) {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
				FrameLayout nb = (FrameLayout) findViewById(R.id.LNavigationBar);
				nb.setVisibility(View.VISIBLE);
				((FrameLayout.LayoutParams) nb.getLayoutParams()).height = navigationBarHeight;
			}
			
			int paddingTop = mLTabs.getPaddingTop();
			int paddingBottom = mLTabs.getPaddingBottom();
			int paddingLeft = mLTabs.getPaddingLeft();
			int paddingRight = mLTabs.getPaddingRight();
			mLTabs.setPadding(paddingLeft, paddingTop + statusBarHeight, paddingRight, paddingBottom);
		}
		

		findViewById(R.id.TVTabMain).setActivated(true);
		findViewById(R.id.TVTabMain).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mVP.setCurrentItem(0);
			}
		});
		findViewById(R.id.TVTabShowRecord).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mVP.setCurrentItem(1);
			}
		});
		
		mVP = (ViewPager) findViewById(R.id.VP);
		mVP.setAdapter(new MyPreferencesAdapter());
//		mVP.setOffscreenPageLimit(2);
		mVP.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				int currentItem = 0;
				if (position == 1)
					currentItem = 2;
				mLTabs.getChildAt(currentItem).setActivated(true);
				mLTabs.getChildAt(previousSelected).setActivated(false);
				previousSelected = currentItem;
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		mVP.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mVP.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				if (currentItem != -1)
					mVP.setCurrentItem(currentItem, false);
			}
		});
		
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			mB = savedInstanceState;
			currentItem = savedInstanceState.getInt(C.currentItem);
			
			currentItem = savedInstanceState.getInt(C.currentItem);
			currentItem = savedInstanceState.getInt(C.currentItem);
			currentItem = savedInstanceState.getInt(C.currentItem);
		}
	}
	
	
	
	
	
	private void showAlertDialog(int title, final View view) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(getString(R.string.tab_main_alert_title))
				.setMessage(getString(title))
				.setIcon(drawable.ic_dialog_alert)
				.setCancelable(false)
				.setNeutralButton(R.string.tab_main_alert_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						view.requestFocus();
					}
		});
		adb.create().show();
	}
	
	
	
	
	
	@Override
	public void onBackPressed() {
		int intervalWidth = 0, intervalRead = 0, intervalUpdate = 0;
		
		switch (mSRead.getSelectedItemPosition()) {
			case 0: intervalRead = 500; break;
			case 1: intervalRead = 1000; break;
			case 2: intervalRead = 2000; break;
			case 3: intervalRead = 4000;
		}
		
		switch (mSUpdate.getSelectedItemPosition()) {
			case 0: intervalUpdate = 1000; break;
			case 1: intervalUpdate = 2000; break;
			case 2: intervalUpdate = 4000; break;
			case 3: intervalUpdate = 8000;
		}
		
		if (intervalRead > intervalUpdate)
			showAlertDialog(R.string.tab_main_alert_text, mSUpdate);
		else {
			switch (mSWidth.getSelectedItemPosition()) {
				case 0: intervalWidth = 1; break;
				case 1: intervalWidth = 2; break;
				case 2: intervalWidth = 5; break;
				case 3: intervalWidth = 10;
			}
			mPrefs.edit()
					.putInt(C.intervalRead, intervalRead)
					.putInt(C.intervalUpdate, intervalUpdate)
					.putInt(C.intervalWidth, intervalWidth)
					.putBoolean(C.cpuTotal, mCBCpuTotalD.isChecked())
//					.putBoolean(Constants.cpuRestD, mCBCPURestD.isChecked())
					.putBoolean(C.cpuAM, mCBCpuAMD.isChecked())
					.putBoolean(C.memFree, mCBMemFreeD.isChecked())
					.putBoolean(C.cached, mCBCachedD.isChecked())
					.commit();
			setResult(1);
			super.onBackPressed();
		}
	}
	
	
	
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// There is a weird behaviour with ViewPagers. If onSaveInstanceState() it's not overriden manually (like here)
		// the ViewPager will save  the currentItem by itself when screen rotation, and when the new Activity is created the saved
		// currentItem position will be requested to be instantiated first instead of item 0.
		outState.putInt(C.currentItem, mVP.getCurrentItem());
		
		outState.putInt(C.mSRead, mSRead.getSelectedItemPosition());
		outState.putInt(C.mSUpdate, mSUpdate.getSelectedItemPosition());
		outState.putInt(C.mSWidth, mSWidth.getSelectedItemPosition());

		outState.putBoolean(C.mCBMemFreeD, mCBMemFreeD.isChecked());
		outState.putBoolean(C.mCBBuffersD, mCBBuffersD.isChecked());
		outState.putBoolean(C.mCBCachedD, mCBCachedD.isChecked());
		outState.putBoolean(C.mCBActiveD, mCBActiveD.isChecked());
		outState.putBoolean(C.mCBInactiveD, mCBInactiveD.isChecked());
		outState.putBoolean(C.mCBSwapTotalD, mCBSwapTotalD.isChecked());
		outState.putBoolean(C.mCBDirtyD, mCBDirtyD.isChecked());
		outState.putBoolean(C.mCBCpuTotalD, mCBCpuTotalD.isChecked());
//		outState.putBoolean(Constants.mCBCpuRestD, mCBCPURestD.isChecked());
		outState.putBoolean(C.mCBCpuAMD, mCBCpuAMD.isChecked());
	}
	
	
	
	
	
	private class MyPreferencesAdapter extends PagerAdapter {
		private ViewGroup mViewGroup;
		
		@Override
		public int getCount() {
			return 2;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			// http://stackoverflow.com/questions/7263291/viewpager-pageradapter-not-updating-the-view/8024557
			mViewGroup = container;
			View v = mViewGroup.getChildAt(position);
			if (v == null) {
				if (position == 0) {
					v = getLayoutInflater().inflate(R.layout.activity_preferences_tab1, mViewGroup, false);
					
					mSRead = (Spinner) v.findViewById(R.id.SRead);
					ArrayAdapter<CharSequence> aARead = ArrayAdapter.createFromResource(ActivityPreferences.this,
							R.array.read_interval_array, android.R.layout.simple_spinner_item);
					aARead.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					mSRead.setAdapter(aARead);
					switch (mPrefs.getInt(C.intervalRead, C.defaultIntervalRead)) {
						case 500: mSRead.setSelection(0); break;
						case 1000: mSRead.setSelection(1); break;
						case 2000: mSRead.setSelection(2); break;
						case 4000: mSRead.setSelection(3); break;
					}
					
					mSUpdate = (Spinner) v.findViewById(R.id.SUpdate);
					ArrayAdapter<CharSequence> aAUpdate = ArrayAdapter.createFromResource(ActivityPreferences.this,
							R.array.update_interval_array, android.R.layout.simple_spinner_item);
					aAUpdate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					mSUpdate.setAdapter(aAUpdate);
					switch (mPrefs.getInt(C.intervalUpdate, C.defaultIntervalUpdate)) {
						case 1000: mSUpdate.setSelection(0); break;
						case 2000: mSUpdate.setSelection(1); break;
						case 4000: mSUpdate.setSelection(2); break;
						case 8000: mSUpdate.setSelection(3); break;
					}
					
					mSWidth = (Spinner) v.findViewById(R.id.SWidth);
					ArrayAdapter<CharSequence> aAWidth = ArrayAdapter.createFromResource(ActivityPreferences.this,
							R.array.width_interval_array, android.R.layout.simple_spinner_item);
					aAWidth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					mSWidth.setAdapter(aAWidth);
					switch (mPrefs.getInt(C.intervalWidth, C.defaultIntervalWidth)) {
						case 1: mSWidth.setSelection(0); break;
						case 2: mSWidth.setSelection(1); break;
						case 5: mSWidth.setSelection(2); break;
						case 10: mSWidth.setSelection(3); break;
					}
					
					((Button) v.findViewById(R.id.BReset)).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mSRead.setSelection(1);
							mSUpdate.setSelection(2);
							mSWidth.setSelection(2);
						}
					});
					
					if (mB != null) {
						mSRead.setSelection(mB.getInt(C.mSRead));
						mSUpdate.setSelection(mB.getInt(C.mSUpdate));
						mSWidth.setSelection(mB.getInt(C.mSWidth));
					}
				} else if (position == 1) {
					v = getLayoutInflater().inflate(R.layout.activity_preferences_tab2, mViewGroup, false);
					
					mCBMemFreeD = (CheckBox) v.findViewById(R.id.CBMemFreeD);
					mCBMemFreeD.setChecked(mPrefs.getBoolean(C.memFree, true));
					mCBCachedD = (CheckBox) v.findViewById(R.id.CBCachedD);
					mCBCachedD.setChecked(mPrefs.getBoolean(C.cached, true));
					mCBCpuTotalD = (CheckBox) v.findViewById(R.id.CBCpuTotalD);
					mCBCpuTotalD.setChecked(mPrefs.getBoolean(C.cpuTotal, true));
//					mCBCPURestD = (CheckBox) v.findViewById(R.id.CBCpuRestD);
//					mCBCPURestD.setChecked(mPrefs.getBoolean(Constants.cpuRestD, true));
					mCBCpuAMD = (CheckBox) v.findViewById(R.id.CBCpuAMD);
					mCBCpuAMD.setChecked(mPrefs.getBoolean(C.cpuAM, true));
					
					((Button) v.findViewById(R.id.BSelectAllDraw)).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mCBMemFreeD.setChecked(true);
							mCBBuffersD.setChecked(true);
							mCBCachedD.setChecked(true);
							mCBActiveD.setChecked(true);
							mCBInactiveD.setChecked(true);
							mCBSwapTotalD.setChecked(true);
							mCBDirtyD.setChecked(true);
							mCBCpuTotalD.setChecked(true);
//							mCBCPURestD.setChecked(true);
							mCBCpuAMD.setChecked(true);
						}
					});
					
					((Button) v.findViewById(R.id.BUnselectAllDraw)).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mCBMemFreeD.setChecked(false);
							mCBBuffersD.setChecked(false);
							mCBCachedD.setChecked(false);
							mCBActiveD.setChecked(false);
							mCBInactiveD.setChecked(false);
							mCBSwapTotalD.setChecked(false);
							mCBDirtyD.setChecked(false);
							mCBCpuTotalD.setChecked(false);
//							mCBCPURestD.setChecked(false);
							mCBCpuAMD.setChecked(false);
						}
					});
					
					if (mB != null) {
						mCBMemFreeD.setChecked(mB.getBoolean(C.mCBMemFreeD));
						mCBBuffersD.setChecked(mB.getBoolean(C.mCBBuffersD));
						mCBCachedD.setChecked(mB.getBoolean(C.mCBCachedD));
						mCBActiveD.setChecked(mB.getBoolean(C.mCBActiveD));
						mCBInactiveD.setChecked(mB.getBoolean(C.mCBInactiveD));
						mCBSwapTotalD.setChecked(mB.getBoolean(C.mCBSwapTotalD));
						mCBDirtyD.setChecked(mB.getBoolean(C.mCBDirtyD));
						mCBCpuTotalD.setChecked(mB.getBoolean(C.mCBCpuTotalD));
//						mCBCPURestD.setChecked(mB.getBoolean(Constants.mCBCpuRestD));
						mCBCpuAMD.setChecked(mB.getBoolean(C.mCBCpuAMD));
					}
				}
				
				if (Build.VERSION.SDK_INT >= 19 && !ViewConfiguration.get(ActivityPreferences.this).hasPermanentMenuKey()
						&& !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
						&& (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || sSW > 560)) {
					View vChild = ((ViewGroup) v).getChildAt(0);
					int pTop = vChild.getPaddingTop();
					int pBottom = vChild.getPaddingBottom();
					int pLeft = vChild.getPaddingLeft();
					int pRight = vChild.getPaddingRight();
					vChild.setPadding(pLeft, pTop, pRight, pBottom + navigationBarHeight);
//					((ScrollView.LayoutParams) vChild.getLayoutParams()).setMargins(0, 0, 0, navigationBarHeight);
				}
				mViewGroup.addView(v);
			}
			return v;
		}
		
		@Override
		public void destroyItem (ViewGroup container, int position, Object object) {
	//		container.removeView((View) object);
		}
		
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}
}
