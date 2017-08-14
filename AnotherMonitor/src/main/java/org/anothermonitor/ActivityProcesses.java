/* 
 * 2010-2017 (C) Antonio Redondo
 * http://antonioredondo.com
 * https://github.com/AntonioRedondo/AnotherMonitor
 *
 * Code under the terms of the GNU General Public License v3.
 *
 */

package org.anothermonitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.jaredrummler.android.processes.AndroidProcesses;

public class ActivityProcesses extends Activity {
	private int navigationBarHeight;
								// List
									// Map
										// C.pId, value
										// C.pName, value
	private List<Map<String, Object>> mListProcesses = new ArrayList<Map<String, Object>>(),
									   mListSelected = new ArrayList<Map<String, Object>>();
	private SimpleAdapter mSA;
	private ListView mLV;
	private Button mBOK;
	
	private BroadcastReceiver receiverFinish = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};
	
	
	
	
	
	@SuppressLint({ "InlinedApi", "NewApi" })
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_processes);
		final Resources res = getResources();
		
		mLV = (ListView) findViewById(R.id.listView);
		mBOK = (Button) findViewById(R.id.BOK);
		
		
		if (Build.VERSION.SDK_INT >= 19) {
			float sSW = res.getConfiguration().smallestScreenWidthDp, sD = res.getDisplayMetrics().density;
			
			int statusBarHeight = res.getDimensionPixelSize(res.getIdentifier(C.sbh, C.dimen, C.android));
			
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			if (!ViewConfiguration.get(this).hasPermanentMenuKey() && !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)
					&& (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || sSW > 560)) {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
				
				navigationBarHeight = res.getDimensionPixelSize(res.getIdentifier(C.nbh, C.dimen, C.android));
				if (navigationBarHeight == 0)
					navigationBarHeight = (int) (48*sD);
				
//				mLV.setPadding(0, 0, 0, navigationBarHeight);
				
				FrameLayout nb = (FrameLayout) findViewById(R.id.LNavigationBar);
				nb.setVisibility(View.VISIBLE);
				((FrameLayout.LayoutParams) nb.getLayoutParams()).height = navigationBarHeight;
			}
			
			RelativeLayout lTopBar = (RelativeLayout) findViewById(R.id.LWindowMyPlacesTopBar);
			int pLeft = lTopBar.getPaddingLeft();
			int pTop = lTopBar.getPaddingTop();
			int pRight = lTopBar.getPaddingRight();
			int pBottom = lTopBar.getPaddingBottom();
			lTopBar.setPadding(pLeft, pTop + statusBarHeight, pRight, pBottom);
		}
		
		
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			mListProcesses = (List<Map<String, Object>>) savedInstanceState.getSerializable(C.listProcesses);
			mListSelected = (List<Map<String, Object>>) savedInstanceState.getSerializable(C.listSelected);
			if (mListSelected != null && !mListSelected.isEmpty()) {
				for(Map<String, Object> process : mListProcesses)
					for (Map<String, Object> selected : mListSelected)
						if (process.get(C.pId).equals(selected.get(C.pId)))
							process.put(C.pSelected, Boolean.TRUE);
			} else mListSelected = new ArrayList<Map<String, Object>>();
			
		} else {
			PackageManager pm = getPackageManager();
			
			List<ActivityManager.RunningAppProcessInfo> runningAppProcesses;
			if (Build.VERSION.SDK_INT < 22) { // http://stackoverflow.com/questions/30619349/android-5-1-1-and-above-getrunningappprocesses-returns-my-application-packag
				runningAppProcesses = ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getRunningAppProcesses();
			} else runningAppProcesses = AndroidProcesses.getRunningAppProcessInfo(this);
			
			if (runningAppProcesses != null) {
				int pid = Process.myPid();
				for (ActivityManager.RunningAppProcessInfo p : runningAppProcesses) {
					if (pid != p.pid) {
						String name = null;
						try {
							name = (String) pm.getApplicationLabel(pm.getApplicationInfo(p.pkgList != null && p.pkgList.length > 0 ? p.pkgList[0] : p.processName, 0));
						} catch (NameNotFoundException e) {
						} catch (NotFoundException e) {
						}
						
						if (name == null)
							name = p.processName;
						
						mListProcesses.add(mapDataForPlacesList(false, name, String.valueOf(p.pid), p.pkgList != null && p.pkgList.length > 0 ? p.pkgList[0] : p.processName, p.processName));
					}
				}
				
				Collections.sort(mListProcesses, new Comparator<Map<String, Object>>(){
					 public int compare(Map<String, Object> o1, Map<String, Object> o2){
						 if(o1.get(C.pAppName).equals(o2.get(C.pAppName)))
							 return 0;
						 return ((String) o1.get(C.pAppName)).compareTo((String) o2.get(C.pAppName)) < 0 ? -1 : 1;
					 }
				});
				
				List<Map<String, Object>> mListSelectedProv = (List<Map<String, Object>>) getIntent().getSerializableExtra(C.listSelected);
				if (mListSelectedProv != null && !mListSelectedProv.isEmpty()) {
					for (Map<String, Object> processSelected : mListSelectedProv) {
						Iterator<Map<String, Object>> iteratorListProcesses = mListProcesses.iterator();
						while (iteratorListProcesses.hasNext()) {
							Map<String, Object> process = iteratorListProcesses.next();
							if (process.get(C.pId).equals(processSelected.get(C.pId)))
								iteratorListProcesses.remove();
						}
					}
				}
				
			} else {
				mLV.setVisibility(View.GONE);
				mBOK.setVisibility(View.GONE);
				findViewById(R.id.LProcessesProblem).setVisibility(View.VISIBLE);
			}
		}
		
		
		if (mListProcesses == null || mListProcesses.isEmpty()) {
			mLV.setVisibility(View.GONE);
			mBOK.setVisibility(View.GONE);
			findViewById(R.id.LProcessesProblem).setVisibility(View.VISIBLE);
			((TextView )findViewById(R.id.TVError)).setText(R.string.w_processes_android_51_problem);
			return;
		}
		
		mSA = new SimpleAdapterCustomised(this, mListProcesses, R.layout.activity_processes_entry,
				new String[] { C.pSelected, C.pPackage, C.pName, C.pId },
				new int[] { R.id.LpBG, R.id.IVpIconBig, R.id.TVpAppName, R.id.TVpName });
		
		mLV.setAdapter(mSA);
		mLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SimpleAdapterCustomised.Tag tag = (SimpleAdapterCustomised.Tag) view.getTag();
				tag.selected = !tag.selected;
				Map<String, Object> newEntry = new HashMap<String, Object>();
				newEntry.put(C.pId, mListProcesses.get(position).get(C.pId));
				newEntry.put(C.pName, mListProcesses.get(position).get(C.pName));
				newEntry.put(C.pAppName, mListProcesses.get(position).get(C.pAppName));
				newEntry.put(C.pPackage, mListProcesses.get(position).get(C.pPackage));
				if (tag.selected) {
					mListSelected.add(newEntry);
				} else {
					Iterator<Map<String, Object>> i = mListSelected.iterator();
					while (i.hasNext())
						if (i.next().get(C.pId).equals(newEntry.get(C.pId)))
							i.remove();
				}
				
				mListProcesses.get(position).put(C.pSelected, tag.selected);
				mSA.notifyDataSetChanged();
			}
		});
		
		mBOK.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListSelected.size() != 0) {
					setResult(1, new Intent(ActivityProcesses.this, ActivityMain.class).putExtra(C.listSelected, (Serializable) mListSelected));
					finish();
				} else {
					Toast.makeText(ActivityProcesses.this, getString(R.string.w_processes_select_some_process), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	
	
	
	
	private Map<String, Object> mapDataForPlacesList(boolean selected, String pAppName, String pid, String pPackage, String pName) {
		Map<String, Object> entry = new HashMap<String, Object>();
		entry.put(C.pSelected, selected);
		entry.put(C.pAppName, pAppName);
		entry.put(C.pId, pid);
		entry.put(C.pPackage, pPackage);
		entry.put(C.pName, pName);
		return entry;
	}
	
	
	
	
	
	@Override
	public void onSaveInstanceState(Bundle outState)  {
		if (mListProcesses.size() != 0)
			outState.putSerializable(C.listProcesses, (Serializable) mListProcesses);
		if (mListSelected.size() != 0)
			outState.putSerializable(C.listSelected, (Serializable) mListSelected);
	}
	
	
	
	
	
	class SimpleAdapterCustomised extends SimpleAdapter {
		public SimpleAdapterCustomised(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			Tag tag = null;
			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.activity_processes_entry, parent, false);
				tag = new Tag();
				tag.l = (LinearLayout) view.findViewById(R.id.LpBG);
				tag.iv = (ImageView) view.findViewById(R.id.IVpIconBig);
				tag.tvPAppName = (TextView) view.findViewById(R.id.TVpAppName);
				tag.tvPName = (TextView) view.findViewById(R.id.TVpName);
				view.setTag(tag);
			} else  tag = (Tag) view.getTag();
			
			if (position == mListProcesses.size()-1)
				view.setPadding(0, 0, 0, navigationBarHeight);
			else view.setPadding(0, 0, 0, 0);
			
			if ((Boolean) mListProcesses.get(position).get(C.pSelected))
				tag.l.setBackgroundColor(ActivityProcesses.this.getResources().getColor(R.color.bgProcessessSelected));
			else tag.l.setBackgroundColor(Color.TRANSPARENT);
			try {
				if (mListProcesses.get(position).get(C.pAppName).equals(mListProcesses.get(position).get(C.pName)))
					tag.iv.setImageDrawable(getDrawable(R.drawable.transparent_pixel));
				else tag.iv.setImageDrawable(getPackageManager().getApplicationIcon((String) mListProcesses.get(position).get(C.pPackage)));
			} catch (NameNotFoundException e) {
			}
			tag.tvPAppName.setText((String) mListProcesses.get(position).get(C.pAppName));
			tag.tvPName.setText(mListProcesses.get(position).get(C.pName) + " - Pid: " + mListProcesses.get(position).get(C.pId));
			
			return view;
		}
		
		class Tag {
			boolean selected;
			LinearLayout l;
			ImageView iv;
			TextView tvPName, tvPAppName;
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
