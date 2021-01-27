

package org.monitrack;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.os.Process;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class ActivityMain extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
	
	private boolean cpuTotal, cpuMT,
				memUsed, memAvailable, memFree, cached, threshold,
				settingsShown, canvasLocked, orientationChanged;
	private int intervalRead, intervalUpdate, intervalWidth, statusBarHeight, navigationBarHeight, animDuration=200,
				settingsHeight, orientation, processesMode, graphicMode;
	private float sD;
	private String s;
	private SharedPreferences mPrefs;
	private FrameLayout mLSettings, mLGraphicSurface, mCloseSettings;
	private LinearLayout mLParent, mLTopBar, mLMenu, mLProcessContainer, mLFeedback, mLWelcome,
						mLCPUTotal, mLCPUMT,
						mLMemUsed, mLMemAvailable, mLMemFree, mLCached, mLThreshold;
	private TextView mTVCPUTotalP, mTVCPUMTP, mTVMemoryMT,
					mTVMemTotal, mTVMemUsed, mTVMemAvailable, mTVMemFree, mTVCached, mTVThreshold,
								 mTVMemUsedP, mTVMemAvailableP, mTVMemFreeP, mTVCachedP, mTVThresholdP;
	private ImageView mLButtonMenu, mLButtonRecord;
	private DecimalFormat mFormat = new DecimalFormat("##,###,##0"), mFormatPercent = new DecimalFormat("##0.0"),
							mFormatTime = new DecimalFormat("0.#");
	private Resources res;
	private Button mBChooseProcess, mBMemory, mBRemoveAll;
	private ToggleButton mBHide;
	private ViewGraphic mVG;
	private SeekBar mSBRead;
	private PopupWindow mPWMenu;

	private ServiceReader mSR;
	private List<Map<String, Object>> mListSelected;
	private Intent tempIntent;
	private Handler mHandler = new Handler(), mHandlerVG = new Handler();
	private Thread mThread;

	private Runnable drawRunnable = new Runnable() {
		@SuppressWarnings("unchecked")
		@SuppressLint("NewApi")
		@Override
		public void run() {
			mHandler.postDelayed(this, intervalUpdate);
			if (mSR != null) {
				mHandlerVG.post(drawRunnableGraphic);
				
				setTextLabelCPU(null, mTVCPUTotalP, mSR.getCPUTotalP());
				if (processesMode == C.processesModeShowCPU)
					setTextLabelCPU(null, mTVCPUMTP, mSR.getCPUMTP());
				else setTextLabelCPU(null, mTVCPUMTP, null, mSR.getMemoryMT());
				BufferedReader r22 = null;
				try {
					r22 = new BufferedReader(new FileReader("sys/class/power_supply/battery/capacity"));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				try {
					s = r22.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(Integer.parseInt(s)<20){
					showMess();
				}

				setTextLabelMemory(mTVMemUsed, mTVMemUsedP, mSR.getMemUsed());
				setTextLabelMemory(mTVMemAvailable, mTVMemAvailableP, mSR.getMemAvailable());
				setTextLabelMemory(mTVMemFree, mTVMemFreeP, mSR.getMemFree());
				setTextLabelMemory(mTVCached, mTVCachedP, mSR.getCached());
				setTextLabelMemory(mTVThreshold, mTVThresholdP, mSR.getThreshold());
				
				for (int n=0; n<mLProcessContainer.getChildCount(); ++n) {
					LinearLayout l = (LinearLayout) mLProcessContainer.getChildAt(n);
					setTextLabelCPUProcess(l);
					setTextLabelMemoryProcesses(l);
				}
			}
		}
	}, drawRunnableGraphic = new Runnable() {
		@Override
		public void run() {
			mThread = new Thread() {
				@Override
				public void run() {
					Canvas canvas;
					if (!canvasLocked) {
						canvas = mVG.lockCanvas();
						if (canvas != null) {
							canvasLocked = true;
							mVG.onDrawCustomised(canvas, mThread);

							try {
								mVG.unlockCanvasAndPost(canvas);
							} catch (IllegalStateException e) {
								Log.w("Activity main: ", e.getMessage());
							}
							
							canvasLocked = false;
						}
					}
				}
			};
			mThread.start();
		}
	};
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@SuppressLint("NewApi")
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mSR = ((ServiceReader.ServiceReaderDataBinder) service).getService();
			
			mVG.setService(mSR);
			mVG.setParameters(cpuTotal, cpuMT, memUsed, memAvailable, memFree, cached, threshold);
			
			setIconRecording();

			mTVMemTotal.setText(mFormat.format(mSR.getMemTotal()) + C.kB);
			
			switchParameter(cpuTotal, mLCPUTotal);
			switchParameter(cpuMT, mLCPUMT);
			
			switchParameter(memUsed, mLMemUsed);
			switchParameter(memAvailable, mLMemAvailable);
			switchParameter(memFree, mLMemFree);
			switchParameter(cached, mLCached);
			switchParameter(threshold, mLThreshold);
			
			mHandler.removeCallbacks(drawRunnable);
			mHandler.post(drawRunnable);

			if (tempIntent !=null) {
				tempIntent.putExtra(C.screenRotated, true);
				onActivityResult(1, 1, tempIntent);
				tempIntent = null;
			} else onActivityResult(1, 1, null);
			
			if (Build.VERSION.SDK_INT >= 16) {
				mLProcessContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						mLProcessContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						LayoutTransition lt = new LayoutTransition();
						lt.enableTransitionType(LayoutTransition.APPEARING);
						lt.enableTransitionType(LayoutTransition.DISAPPEARING);
						lt.enableTransitionType(LayoutTransition.CHANGING);
						mLProcessContainer.setLayoutTransition(lt);
						LayoutTransition lt2 = new LayoutTransition();
						lt2.enableTransitionType(LayoutTransition.CHANGING);
						lt2.setStartDelay(LayoutTransition.CHANGING, 300);
						((LinearLayout) mLProcessContainer.getParent()).setLayoutTransition(lt2);
					}
				});
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName className) {
			mSR = null;
		}
	};
	
	private BroadcastReceiver receiverSetIconRecord = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			setIconRecording();
		}
	}, receiverDeadProcess = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			switchParameterForProcess((Map<String, Object>) intent.getSerializableExtra(C.process));
		}
	}, receiverFinish = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};
	
	
	
	
	
	@SuppressWarnings("deprecation")
	@SuppressLint({ "InlinedApi", "NewApi", "InflateParams" })
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		startService(new Intent(this, ServiceReader.class));
		setContentView(R.layout.activity_main);

		mPrefs = getSharedPreferences(getString(R.string.app_name) + C.prefs, MODE_PRIVATE);
		intervalRead = mPrefs.getInt(C.intervalRead, C.defaultIntervalUpdate);
		intervalUpdate = mPrefs.getInt(C.intervalUpdate, C.defaultIntervalUpdate);
		intervalWidth = mPrefs.getInt(C.intervalWidth, C.defaultIntervalWidth);
		
		cpuTotal = mPrefs.getBoolean(C.cpuTotal, true);
		cpuMT = mPrefs.getBoolean(C.cpuMT, true);
		
		memUsed = mPrefs.getBoolean(C.memUsed, true);
		memAvailable = mPrefs.getBoolean(C.memAvailable, true);
		memFree = mPrefs.getBoolean(C.memFree, false);
		cached = mPrefs.getBoolean(C.cached, false);
		threshold = mPrefs.getBoolean(C.threshold, true);

		res = getResources();
		sD = res.getDisplayMetrics().density;
		sD = res.getDisplayMetrics().density;
		orientation = res.getConfiguration().orientation;
		statusBarHeight = res.getDimensionPixelSize(res.getIdentifier(C.sbh, C.dimen, C.android));
		
		final SeekBar mSBWidth = (SeekBar) findViewById(R.id.SBIntervalWidth);
		if (savedInstanceState != null && !savedInstanceState.isEmpty() && savedInstanceState.getInt(C.orientation) != orientation)
			orientationChanged = true;
		
		
		mVG = (ViewGraphic) findViewById(R.id.ANGraphic);
		
		graphicMode = mPrefs.getInt(C.graphicMode, C.graphicModeShowMemory);
		mVG.setGraphicMode(graphicMode);


				graphicMode = graphicMode == C.graphicModeShowMemory ? C.graphicModeHideMemory : C.graphicModeShowMemory;
				mPrefs.edit().putInt(C.graphicMode, graphicMode).apply();


				mHandlerVG.post(drawRunnableGraphic);

		
		processesMode = mPrefs.getInt(C.processesMode, C.processesModeShowCPU);
		mVG.setProcessesMode(processesMode);

		
		mLTopBar = (LinearLayout) findViewById(R.id.LTopBar);
		mLGraphicSurface = (FrameLayout) findViewById(R.id.LGraphicButton);
		
		if (Build.VERSION.SDK_INT >= 19) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			
			float sSW = res.getConfiguration().smallestScreenWidthDp;
			
			if (!ViewConfiguration.get(this).hasPermanentMenuKey() && !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)
					&& (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || sSW > 560)) {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
				navigationBarHeight = res.getDimensionPixelSize(res.getIdentifier(C.nbh, C.dimen, C.android));
				if (navigationBarHeight == 0)
					navigationBarHeight = (int) (48*sD);
				
				FrameLayout nb = (FrameLayout) findViewById(R.id.LNavigationBar);
				nb.setVisibility(View.VISIBLE);
				((FrameLayout.LayoutParams) nb.getLayoutParams()).height = navigationBarHeight;
				((FrameLayout.LayoutParams) mVG.getLayoutParams()).setMargins(0, 0, 0, navigationBarHeight);
				((FrameLayout.LayoutParams) mLGraphicSurface.getLayoutParams()).setMargins(0, 0, 0, navigationBarHeight);
				
				int paddingTop = mSBWidth.getPaddingTop();
				int paddingBottom = mSBWidth.getPaddingBottom();
				int paddingLeft = mSBWidth.getPaddingLeft();
				int paddingRight = mSBWidth.getPaddingRight();
				mSBWidth.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + navigationBarHeight);
			}
			
			int paddingTop = mLTopBar.getPaddingTop();
			int paddingBottom = mLTopBar.getPaddingBottom();
			int paddingLeft = mLTopBar.getPaddingLeft();
			int paddingRight = mLTopBar.getPaddingRight();
			mLTopBar.setPadding(paddingLeft, paddingTop + statusBarHeight, paddingRight, paddingBottom);
		}
		
		mLParent = (LinearLayout) findViewById(R.id.LParent);
		
		mLButtonMenu = (ImageView) findViewById(R.id.LButtonMenu);
		

			mLButtonMenu.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mPWMenu.setAnimationStyle(R.style.Animations_PopDownMenu);
					if (Build.VERSION.SDK_INT < 19)
						mPWMenu.showAtLocation(mLTopBar, Gravity.TOP | Gravity.RIGHT, 0, mLTopBar.getHeight() + statusBarHeight);
					else mPWMenu.showAtLocation(mLTopBar, Gravity.TOP | Gravity.RIGHT, 0, mLTopBar.getHeight());
				}
			});

		
		mLButtonRecord = (ImageView) findViewById(R.id.LButtonRecord);
		mLButtonRecord.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mSR.isRecording())
						mSR.stopRecord();
					else mSR.startRecord();
					mHandlerVG.post(drawRunnableGraphic);
				}
			});
		mLButtonRecord.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				int id = R.string.menu_record_description;
				if (mSR.isRecording())
					id = R.string.menu_record_stop_description;
				Toast.makeText(ActivityMain.this, getString(id), Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		mLMenu = (LinearLayout) getLayoutInflater().inflate(R.layout.layer_menu, null);
		mLMenu.setFocusableInTouchMode(true);
		
		mPWMenu = new PopupWindow(mLMenu, (int) (260*sD), WindowManager.LayoutParams.WRAP_CONTENT, true);
		mPWMenu.setAnimationStyle(R.style.Animations_PopDownMenu);
		mPWMenu.setBackgroundDrawable(new BitmapDrawable());
		mPWMenu.getContentView().setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_UP) {
					mPWMenu.dismiss();
					return true;
				}
				return false;
			}
		});
		
		mLMenu.findViewById(R.id.LHelp).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPWMenu.dismiss();
				startActivity(new Intent(ActivityMain.this, ActivityHelp.class));
			}
		});
		mLMenu.findViewById(R.id.Lbat).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPWMenu.dismiss();
				startActivity(new Intent(ActivityMain.this, ActivityBattery.class));
			}
		});
		
		mLMenu.findViewById(R.id.LAbout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPWMenu.dismiss();
				startActivity(new Intent(ActivityMain.this, ActivityAbout.class));
			}
		});
		mLMenu.findViewById(R.id.Lcsv).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPWMenu.dismiss();
				startActivity(new Intent(ActivityMain.this, ActivityGraph.class));
			}
		});
		
		mLMenu.findViewById(R.id.Lstat).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPWMenu.dismiss();
				startActivity(new Intent(ActivityMain.this, ActivityStat.class));
			}
		});
		mLMenu.findViewById(R.id.Lkill).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPWMenu.dismiss();
				startActivity(new Intent(ActivityMain.this, ActivityKill.class));
			}
		});
		mLProcessContainer = (LinearLayout) findViewById(R.id.LProcessContainer);
		
		mLCPUTotal = (LinearLayout) findViewById(R.id.LCPUTotal);
		mLCPUTotal.setTag(C.cpuTotal);
		mLCPUTotal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchParameter(cpuTotal = !cpuTotal, mLCPUTotal);
			}
		});
		mLCPUMT = (LinearLayout) findViewById(R.id.LCPUMT);
		mLCPUMT.setTag(C.cpuMT);
		mLCPUMT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchParameter(cpuMT = !cpuMT, mLCPUMT);
			}
		});
		((TextView) ((LinearLayout) mLCPUMT.getChildAt(2)).getChildAt(1)).setText("Pid: " + Process.myPid());
		
		mLMemUsed = (LinearLayout) findViewById(R.id.LMemUsed);
		mLMemUsed.setTag(C.memUsed);
		mLMemUsed.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchParameter(memUsed = !memUsed, mLMemUsed);
			}
		});
		mLMemAvailable = (LinearLayout) findViewById(R.id.LMemAvailable);
		mLMemAvailable.setTag(C.memAvailable);
		mLMemAvailable.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchParameter(memAvailable = !memAvailable, mLMemAvailable);
			}
		});
		mLMemFree = (LinearLayout) findViewById(R.id.LMemFree);
		mLMemFree.setTag(C.memFree);
		mLMemFree.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchParameter(memFree = !memFree, mLMemFree);
			}
		});
		mLCached = (LinearLayout) findViewById(R.id.LCached);
		mLCached.setTag(C.cached);
		mLCached.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchParameter(cached = !cached, mLCached);
			}
		});
		mLThreshold = (LinearLayout) findViewById(R.id.LThreshold);
		mLThreshold.setTag(C.threshold);
		mLThreshold.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchParameter(threshold = !threshold, mLThreshold);
			}
		});
		
		mTVCPUTotalP = (TextView) findViewById(R.id.TVCPUTotalP);
		mTVCPUMTP = (TextView) findViewById(R.id.TVCPUMTP);
		mTVMemoryMT = (TextView) findViewById(R.id.TVMemoryMT);
		mTVMemTotal = (TextView) findViewById(R.id.TVMemTotal);
		mTVMemUsed = (TextView) findViewById(R.id.TVMemUsed);
		mTVMemUsedP = (TextView) findViewById(R.id.TVMemUsedP);
		mTVMemAvailable = (TextView) findViewById(R.id.TVMemAvailable);
		mTVMemAvailableP = (TextView) findViewById(R.id.TVMemAvailableP);
		mTVMemFree = (TextView) findViewById(R.id.TVMemFree);
		mTVMemFreeP = (TextView) findViewById(R.id.TVMemFreeP);
		mTVCached = (TextView) findViewById(R.id.TVCached);
		mTVCachedP = (TextView) findViewById(R.id.TVCachedP);
		mTVThreshold = (TextView) findViewById(R.id.TVThreshold);
		mTVThresholdP = (TextView) findViewById(R.id.TVThresholdP);

		
		mLSettings = (FrameLayout) findViewById(R.id.LSettings);
		mLSettings.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mLSettings.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				settingsHeight = mLSettings.getHeight();
				mLSettings.getLayoutParams().height = 0;
			}
		});
		
		mLGraphicSurface.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPWMenu.dismiss();
				startActivity(new Intent(ActivityMain.this, ActivityGraph.class));
			}
		});
		mLGraphicSurface.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				Toast.makeText(ActivityMain.this, getString(R.string.menu_settings_description), Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		mVG.setOpaque(false);

		mVG.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

			}
			
			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
			}
			
			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

				return true;
			}
			
			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture surface) {
			}
			
		});
		mVG.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mVG.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mLGraphicSurface.getLayoutParams();

				lp.setMargins((int) (mVG.getWidth()*0.14), (int) (mVG.getHeight()*0.1), (int) (mVG.getWidth()*0.06), (int) (mVG.getHeight()*0.12) + navigationBarHeight);
			}
		});
		
		mBChooseProcess = (Button) findViewById(R.id.BChooseProcess);
		mBChooseProcess.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ActivityMain.this, ActivityProcesses.class);
				i.putExtra(C.listSelected, (Serializable) mListSelected);
				startActivityForResult(i, 1);
			}
		});
		mBRemoveAll = (Button) findViewById(R.id.BRemoveAll);
		mBRemoveAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListSelected.clear();
				mLProcessContainer.removeAllViews();
				mHandlerVG.post(drawRunnableGraphic);
				mBRemoveAll.animate().setDuration(300).alpha(0).setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mBRemoveAll.setVisibility(View.GONE);
					}
				});
			}
		});

		final TextView mTVIntervalRead = (TextView) findViewById(R.id.TVIntervalRead);
		mTVIntervalRead.setText(getString(R.string.interval_read) + " " + mFormatTime.format(intervalRead/(float)1000) + " s");
		final TextView mTVIntervalUpdate = (TextView) findViewById(R.id.TVIntervalUpdate);
		mTVIntervalUpdate.setText(getString(R.string.interval_update) + " " + mFormatTime.format(intervalUpdate/(float)1000) + " s");
		final TextView mTVIntervalWidth = (TextView) findViewById(R.id.TVIntervalWidth);
		mTVIntervalWidth.setText(getString(R.string.interval_width) + " " + intervalWidth + " dp");

		mSBRead = (SeekBar) findViewById(R.id.SBIntervalRead);
		int t = 0;
		switch (intervalRead) {
			case 500: t = 0; break;
			case 1000: t = 1; break;
			case 2000: t = 2; break;
			case 4000: t = 4;
		}
		mSBRead.setProgress(t);
		mSBRead.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				seekBar.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				int t = 0;
				switch (mSBRead.getProgress()) {
					case 0: t = 500; break;
					case 1: t = 1000; break;
					case 2: t = 2000; break;
					case 3: t = 4000;
				}
				mTVIntervalRead.setText(getString(R.string.interval_read) + " " + mFormatTime.format(t/(float)1000) + " s");
			}
		});
		
		final SeekBar mSBUpdate = (SeekBar) findViewById(R.id.SBIntervalUpdate);
		t = 0;
		switch (intervalUpdate) {
			case 500: t = 0; break;
			case 1000: t = 1; break;
			case 2000: t = 2; break;
			case 4000: t = 3;
		}
		mSBUpdate.setProgress(t);
		mSBUpdate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				seekBar.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				int t = 0;
				switch (mSBUpdate.getProgress()) {
					case 0: t = 500; break;
					case 1: t = 1000; break;
					case 2: t = 2000; break;
					case 3: t = 4000;
				}
				mTVIntervalUpdate.setText(getString(R.string.interval_update) + " " + mFormatTime.format(t/(float)1000) + " s");
			}
		});

		t = 0;
		switch (intervalWidth) {
			case 1: t = 0; break;
			case 2: t = 1; break;
			case 5: t = 2; break;
			case 10: t = 4;
		}
		mSBWidth.setProgress(t);
		mSBWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				seekBar.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				int t = 0;
				switch (mSBWidth.getProgress()) {
					case 0: t = 1; break;
					case 1: t = 2; break;
					case 2: t = 5; break;
					case 3: t = 10;
				}
				mTVIntervalWidth.setText(getString(R.string.interval_width) + " " + t + " dp");
			}
		});

		mCloseSettings = (FrameLayout) findViewById(R.id.LOK);
		mCloseSettings.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				hideSettings();

				int intervalWidth = 0, intervalRead = 0, intervalUpdate = 0;

				switch (mSBRead.getProgress()) {
					case 0: intervalRead = 500; break;
					case 1: intervalRead = 1000; break;
					case 2: intervalRead = 2000; break;
					case 3: intervalRead = 4000;
				}

				switch (mSBUpdate.getProgress()) {
					case 0: intervalUpdate = 500; break;
					case 1: intervalUpdate = 1000; break;
					case 2: intervalUpdate = 2000; break;
					case 3: intervalUpdate = 4000;
				}

				switch (mSBWidth.getProgress()) {
					case 0: intervalWidth = 1; break;
					case 1: intervalWidth = 2; break;
					case 2: intervalWidth = 5; break;
					case 3: intervalWidth = 10;
				}
				
				if (intervalRead > intervalUpdate) {
					intervalUpdate = intervalRead;
					int t = 0;
					switch (intervalUpdate) {
						case 500: t = 0; break;
						case 1000: t = 1; break;
						case 2000: t = 2; break;
						case 4000: t = 3;
					}
					mSBUpdate.setProgress(t);
				}
				
				if (ActivityMain.this.intervalRead != intervalRead) {
					mSR.getCPUTotalP().clear();
					mSR.getCPUMTP().clear();
					
					if (mListSelected != null && !mListSelected.isEmpty())
						for (Map<String, Object> process : mListSelected) {
							process.put(C.pFinalValue, new ArrayList<Float>());
							process.put(C.pTPD, new ArrayList<Integer>());
						}
					
					mSR.getMemUsed().clear();
					mSR.getMemAvailable().clear();
					mSR.getMemFree().clear();
					mSR.getCached().clear();
					mSR.getThreshold().clear();
				}
				
				ActivityMain.this.intervalRead = intervalRead;
				ActivityMain.this.intervalUpdate = intervalUpdate;
				ActivityMain.this.intervalWidth = intervalWidth;
				
				mSR.setIntervals(intervalRead, intervalUpdate, intervalWidth);
				mVG.calculateInnerVariables();
				mHandlerVG.post(drawRunnableGraphic);
				mHandler.removeCallbacks(drawRunnable);
				mHandler.post(drawRunnable);
				mPrefs.edit()
						.putInt(C.intervalRead, intervalRead)
						.putInt(C.intervalUpdate, intervalUpdate)
						.putInt(C.intervalWidth, intervalWidth)
						.apply();
			}
		});
		
		if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
			ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, C.storagePermission);
		
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			processesMode = savedInstanceState.getInt(C.processesMode);
			mBMemory.setText(processesMode == C.processesModeShowCPU ? getString(R.string.w_main_memory) : getString(R.string.p_cpuusage));
			mVG.setProcessesMode(processesMode);
			
			canvasLocked = savedInstanceState.getBoolean(C.canvasLocked);
			settingsShown = savedInstanceState.getBoolean(C.settingsShown);
			if (settingsShown)
				mLSettings.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						mLSettings.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						mLSettings.getLayoutParams().height = settingsHeight;
					}
				});
			if (savedInstanceState.getBoolean(C.menuShown))
				mLTopBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						mLTopBar.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						mPWMenu.showAtLocation(mLTopBar, Gravity.TOP | Gravity.RIGHT, 0, mLTopBar.getHeight());
					}
				});
		}
		

		if (mPrefs.getBoolean(C.welcome, true)) {
			mPrefs.edit().putLong(C.welcomeDate, Calendar.getInstance(TimeZone.getTimeZone(C.europeLondon)).getTimeInMillis()).apply();
			ViewStub v = (ViewStub) findViewById(R.id.VSWelcome);
			if (v != null) {
				mLWelcome = (LinearLayout) v.inflate();
				
				int bottomMargin = 0;
				if (Build.VERSION.SDK_INT >= 19)
						bottomMargin = navigationBarHeight;
				((FrameLayout.LayoutParams) mLWelcome.getLayoutParams()).setMargins(0, 0, 0, (int)(35*sD) + bottomMargin);
				
				(mLWelcome.findViewById(R.id.BHint)).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mPrefs.edit().putBoolean(C.welcome, false).apply();
						mLWelcome.animate().setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								((ViewManager) mLWelcome.getParent()).removeView(mLWelcome);
								mLWelcome = null;
							}
						}).setStartDelay(0).alpha(0).translationYBy(-15*sD);
					}
				});
				
				int animDur = animDuration;
				int delayDur = 500;
				if (orientationChanged) {
					animDur = 0;
					delayDur = 0;
				}
				mLWelcome.animate().setStartDelay(delayDur).setDuration(animDur).alpha(1).translationYBy(15*sD);
			}
		}
		
		long time = Calendar.getInstance(TimeZone.getTimeZone(C.europeLondon)).getTimeInMillis();
		

	}
	
	
	
	
	
	private void showSettings() {
		settingsShown = true;
		mLGraphicSurface.setEnabled(false);

		ValueAnimator va = ValueAnimator.ofInt(0, settingsHeight);
		va.setDuration(animDuration);
		va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				Integer value = (Integer) animation.getAnimatedValue();
				mLSettings.getLayoutParams().height = value.intValue();
				mLSettings.requestLayout();
			}
		});
		va.start();
	}
	
	
	
	
	
	private void hideSettings() {
		settingsShown = false;
		mLGraphicSurface.setEnabled(true);
		ValueAnimator va = ValueAnimator.ofInt(settingsHeight, 0);
		va.setDuration(animDuration);
		va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				Integer value = (Integer) animation.getAnimatedValue();
				mLSettings.getLayoutParams().height = value.intValue();
				mLSettings.requestLayout();
			}
		});
		va.start();
	}
	
	
	
	
	
	private void switchParameter(boolean draw, LinearLayout labelRow) {
		if (mSR == null)
			return;
		
		mPrefs.edit()
				.putBoolean(C.cpuTotal, cpuTotal)
				.putBoolean(C.cpuMT, cpuMT)
				
				.putBoolean(C.memUsed, memUsed)
				.putBoolean(C.memAvailable, memAvailable)
				.putBoolean(C.memFree, memFree)
				.putBoolean(C.cached, cached)
				.putBoolean(C.threshold, threshold)
				
				.apply();
		
		mVG.setParameters(cpuTotal, cpuMT, memUsed, memAvailable, memFree, cached, threshold);
		
		ImageView icon = (ImageView) labelRow.getChildAt(0);
		if (draw)
			icon.setImageResource(R.drawable.icon_play);
		else icon.setImageResource(R.drawable.icon_pause);
		
		mHandlerVG.post(drawRunnableGraphic);
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	private void switchParameterForProcess(Map<String, Object> process) {
		LinearLayout l = null;
		for (int n=0; n<mLProcessContainer.getChildCount(); ++n) {
			l = (LinearLayout) mLProcessContainer.getChildAt(n);
			if (((Map<String, Object>) l.getTag()).get(C.pId).equals(process.get(C.pId)))
				break;
		}
		ImageView iv = (ImageView) l.getChildAt(0);
		
		if (process.get(C.pDead) != null) {
			((TextView) l.findViewById(R.id.TVpPercentage)).setText(getString(R.string.w_processes_dead));
			l.findViewById(R.id.TVpName).setAlpha(0.2f);
			l.findViewById(R.id.TVpAbsolute).setVisibility(View.INVISIBLE);
			l.getChildAt(1).setAlpha(0.3f);
		}
		
		if ((Boolean) process.get(C.pSelected)) {
			iv.setImageResource(R.drawable.icon_play);
			if (process.get(C.pDead) == null)
				setTextLabelCPUProcess(l);
		} else {
			iv.setImageResource(R.drawable.icon_pause);
		}
		
		mHandlerVG.post(drawRunnableGraphic);
	}
	
	
	
	
	
	private void setTextLabelCPU(TextView absolute, TextView percent, List<Float> values, @SuppressWarnings("unchecked") List<Integer>... valuesInteger) {
		if (valuesInteger.length == 1) {
			percent.setText(mFormatPercent.format(valuesInteger[0].get(0) * 100 / (float) mSR.getMemTotal()) + C.percent);
			mTVMemoryMT.setVisibility(View.VISIBLE);
			mTVMemoryMT.setText(mFormat.format(valuesInteger[0].get(0)) + C.kB);
		} else if (!values.isEmpty()) {
			percent.setText(mFormatPercent.format(values.get(0)) + C.percent);
			mTVMemoryMT.setVisibility(View.INVISIBLE);
		}
	}
	
	
	
	
	
	private void setTextLabelMemory(TextView absolute, TextView percent, List<String> values) {
		if (!values.isEmpty()) {
			absolute.setText(mFormat.format(Integer.parseInt(values.get(0))) + C.kB);
			percent.setText(mFormatPercent.format(Integer.parseInt(values.get(0)) * 100 / (float) mSR.getMemTotal()) + C.percent);
		}
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	private void setTextLabelCPUProcess(LinearLayout l) {
		Map<String, Object> entry = (Map<String, Object>) l.getTag();
		if (entry != null
				&& entry.get(C.pFinalValue) != null && ((List<String>) entry.get(C.pFinalValue)).size() != 0
				&& entry.get(C.pTPD) != null && !((List<String>) entry.get(C.pTPD)).isEmpty()
				&& entry.get(C.pDead) == null)
			if (processesMode == C.processesModeShowCPU)
				((TextView) l.findViewById(R.id.TVpPercentage)).setText(mFormatPercent.format(((List<String>) entry.get(C.pFinalValue)).get(0)) + C.percent);
			else ((TextView) l.findViewById(R.id.TVpPercentage)).setText(mFormatPercent.format(((List<Integer>) entry.get(C.pTPD)).get(0) * 100 / (float) mSR.getMemTotal()) + C.percent);
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	private void setTextLabelMemoryProcesses(LinearLayout l) {
		TextView tv = (TextView) l.findViewById(R.id.TVpAbsolute);
		if (processesMode == C.processesModeShowCPU)
			tv.setVisibility(View.INVISIBLE);
		else {
			Map<String, Object> entry = (Map<String, Object>) l.getTag();
			if (entry != null
					&& entry.get(C.pTPD) != null && !((List<String>) entry.get(C.pTPD)).isEmpty()
					&& entry.get(C.pDead) == null) {
				tv.setVisibility(View.VISIBLE);
				tv.setText(mFormat.format(((List<String>) entry.get(C.pTPD)).get(0)) + C.kB);
			}
		}
	}
	
	
	
	
	
	private void setIconRecording() {
		if (mSR == null)
			return;
		if (mSR.isRecording()) {
			mSBRead.setEnabled(false);
			mBChooseProcess.setEnabled(false);
			mLButtonRecord.setImageResource(R.drawable.button_stop_record);
		} else {
			mSBRead.setEnabled(true);
			mBChooseProcess.setEnabled(true);
			mLButtonRecord.setImageResource(R.drawable.button_start_record);
		}
	}
	
	
	
	
	
	private int getColourForProcess(int n) {
		if (n==0)
			return res.getColor(R.color.process3);
		else if (n==1)
			return res.getColor(R.color.process4);
		else if (n==2)
			return res.getColor(R.color.process5);
		else if (n==3)
			return res.getColor(R.color.process6);
		else if (n==4)
			return res.getColor(R.color.process7);
		else if (n==5)
			return res.getColor(R.color.process8);
		else if (n==6)
			return res.getColor(R.color.process1);
		else if (n==7)
			return res.getColor(R.color.process2);
		n-=8;
		return getColourForProcess(n);
	}
	
	
	
	
	
	@SuppressLint({ "NewApi", "InflateParams" })
	@SuppressWarnings("unchecked")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)  {
		if (requestCode == 1 && resultCode == 1) {

			List<Map<String, Object>> mListSelectedProv = null;
			if (data != null) {
				mListSelectedProv = (List<Map<String, Object>>) data.getSerializableExtra(C.listSelected);
				if (mListSelectedProv == null)
					return;
				

				if (mSR == null) {
					tempIntent = data;
					return;
				}
				
				for(Map<String, Object> process : mListSelectedProv) {
					process.put(C.pColour, getColourForProcess(mSR.getProcesses() != null ? mSR.getProcesses().size() : 0));
					mSR.addProcess(process);
				}
				
				mListSelected = mSR.getProcesses();
				
				if (data.getBooleanExtra(C.screenRotated, false))
					mListSelectedProv = mListSelected;
				
			} else {
				mListSelected = mSR.getProcesses();
				mListSelectedProv = mListSelected;
			}
			
			if (mListSelectedProv == null)
				return;
			
			mBRemoveAll.setAlpha(1);
			mBRemoveAll.setVisibility(View.VISIBLE);
			
			synchronized (mListSelected) {
				for (final Map<String, Object> process : mListSelectedProv) {
					if (process.get(C.pSelected) == null)
						process.put(C.pSelected, Boolean.TRUE);
					
					final LinearLayout l = (LinearLayout) getLayoutInflater().inflate(R.layout.layer_process_entry, null);
					l.setTag(process);
					l.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							if (!mSR.isRecording()) {
								mSR.removeProcess(process);
								mListSelected.remove(process);
								mLProcessContainer.removeView(l);
								return true;
							} else return false;
						}
					});
					l.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Boolean b = (Boolean) process.get(C.pSelected);
							process.put(C.pSelected, !b);
							switchParameterForProcess(process);
							

						}
					});
					
					Drawable d = null;
					try {
						d = getPackageManager().getApplicationIcon((String) process.get(C.pPackage));
					} catch (NameNotFoundException e) {
					}
					
					ImageView pIcon = (ImageView) l.getChildAt(1);
					pIcon.setImageDrawable(d);
					
					int colour = (Integer) process.get(C.pColour);
					
					TextView pName = (TextView) l.findViewById(R.id.TVpAppName);
					pName.setText((String) process.get(C.pAppName));
					pName.setTextColor(colour);
					
					TextView pId = (TextView) l.findViewById(R.id.TVpName);
					pId.setText("Pid: " + process.get(C.pId));
					
					TextView pUsage = (TextView) l.findViewById(R.id.TVpPercentage);
					pUsage.setTextColor(colour);
					
					mLProcessContainer.addView(l);
					switchParameterForProcess(process);
					


				}
			}
		}
		
		orientationChanged = false;
	}
	
	
	
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(C.orientation, orientation);
		outState.putBoolean(C.menuShown, mPWMenu.isShowing());
		outState.putBoolean(C.settingsShown, settingsShown);
		outState.putBoolean(C.canvasLocked, canvasLocked);
	}
	
	
	
	
	
	@Override
	public void onStart() {
		super.onStart();
		bindService(new Intent(this, ServiceReader.class), mServiceConnection, 0);
		registerReceiver(receiverSetIconRecord, new IntentFilter(C.actionSetIconRecord));
		registerReceiver(receiverDeadProcess, new IntentFilter(C.actionDeadProcess));
		registerReceiver(receiverFinish, new IntentFilter(C.actionFinishActivity));
	}
	
	
	
	
	
	@Override
	public void onResume() {
		super.onResume();
		mHandler.removeCallbacks(drawRunnable);
		mHandler.post(drawRunnable);
	}
	
	
	
	
	
	@Override
	public void onPause() {
		super.onPause();
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
		mHandler.removeCallbacks(drawRunnable);
	}
	
	
	
	
	
	@Override
	public void onStop() {
		super.onStop();
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
		mHandler.removeCallbacks(drawRunnable);
	}
	
	
	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		orientationChanged = false;
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
		mHandler.removeCallbacks(drawRunnable);
		if (mPWMenu.isShowing())
			mPWMenu.dismiss();
		unregisterReceiver(receiverSetIconRecord);
		unregisterReceiver(receiverDeadProcess);
		unregisterReceiver(receiverFinish);
		unbindService(mServiceConnection);
	}
	
	
	
	
	
	@Override
	public void onBackPressed() {
		if (mLFeedback != null && mLFeedback.getAlpha() != 0) {
			mPrefs.edit().putLong(C.welcomeDate, Calendar.getInstance(TimeZone.getTimeZone(C.europeLondon)).getTimeInMillis()).apply();
			Toast.makeText(ActivityMain.this, getString(R.string.w_main_feedback_no_remind), Toast.LENGTH_LONG).show();
			mLFeedback.animate().setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					((ViewManager) mLFeedback.getParent()).removeView(mLFeedback);
					mLFeedback = null;
				}
			}).setStartDelay(0).alpha(0).translationYBy(-15*sD);
			return;
		}
		if (mLWelcome != null && mLWelcome.getAlpha() != 0) {
			mPrefs.edit().putBoolean(C.welcome, false).apply();
			mLWelcome.animate().setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					((ViewManager) mLWelcome.getParent()).removeView(mLWelcome);
					mLWelcome = null;
				}
			}).setStartDelay(0).alpha(0).translationYBy(-15*sD);
			return;
		}
		
		if (settingsShown) {
			mCloseSettings.performClick();
			return;
		}
		
		super.onBackPressed();
	}
	
	
	
	
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode ==  KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
				mPWMenu.setAnimationStyle(R.style.Animations_PopDownMenuBottom);
				mPWMenu.showAtLocation(mLParent, Gravity.BOTTOM | Gravity.CENTER,  0, 0);
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}


	protected void showMess(){

		Toast.makeText(this," The Battery Level is below the Limit.", Toast.LENGTH_SHORT).show();
		return;

	}

	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == C.storagePermission && PackageManager.PERMISSION_DENIED == grantResults[0]) {
			Toast.makeText(ActivityMain.this, getString(R.string.w_main_storage_permission), Toast.LENGTH_LONG).show();
		}
	}
}
