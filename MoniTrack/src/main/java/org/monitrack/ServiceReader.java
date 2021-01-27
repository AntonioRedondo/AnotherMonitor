
package org.monitrack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.*;
import android.os.Debug.MemoryInfo;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class ServiceReader extends Service {

	private static final String CHANNEL_ID = "Foreground Notification";
	private boolean recording, firstRead = true, topRow = true;
	private int memTotal, pId, intervalRead, intervalUpdate, intervalWidth, maxSamples = 2000;
	private long workT, totalT, workMTT, total, totalBefore, work, workBefore, workMT, workMTBefore;
	private String s;
	private String[] sa;
	private List<Float> cpuTotal, cpuMT;
	private List<Integer> memoryMT;
	private List<Map<String, Object>> mListSelected;
	private List<String> memUsed, memAvailable, memFree, cached, threshold;
	private ActivityManager am;
	private Debug.MemoryInfo[] amMI;
	private ActivityManager.MemoryInfo mi;
	private NotificationManager mNM;
	private Notification mNotificationRead, mNotificationRecord;
	private BufferedReader reader;
	private BufferedWriter mW;
	private File mFile;
	private SharedPreferences mPrefs;
	private Runnable readRunnable = new Runnable() {
		@Override
		public void run() {
			Thread thisThread = Thread.currentThread();
			while (readThread == thisThread) {
				read();
				try {
					Thread.sleep(intervalRead);
				} catch (InterruptedException e) {
					break;
				}



			}
		}




	};
	private volatile Thread readThread = new Thread(readRunnable, C.readThread);
	private BroadcastReceiver receiverStartRecord = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			startRecord();
			sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		}
	},
	receiverStopRecord = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			stopRecord();
			sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		}
	},
	receiverClose = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
			sendBroadcast(new Intent(C.actionFinishActivity));
			stopSelf();
		}
	};





	class ServiceReaderDataBinder extends Binder {
		ServiceReader getService() {
			return ServiceReader.this;
		}
	}





	@Override
	public void onCreate() {
		cpuTotal = new ArrayList<Float>(maxSamples);
		cpuMT = new ArrayList<Float>(maxSamples);
		memoryMT = new ArrayList<Integer>(maxSamples);
		memUsed = new ArrayList<String>(maxSamples);
		memAvailable = new ArrayList<String>(maxSamples);
		memFree = new ArrayList<String>(maxSamples);
		cached = new ArrayList<String>(maxSamples);
		threshold = new ArrayList<String>(maxSamples);

		pId = Process.myPid();

		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		amMI = am.getProcessMemoryInfo(new int[]{ pId });
		mi = new ActivityManager.MemoryInfo();

		mPrefs = getSharedPreferences(getString(R.string.app_name) + C.prefs, MODE_PRIVATE);
		intervalRead = mPrefs.getInt(C.intervalRead, C.defaultIntervalRead);
		intervalUpdate = mPrefs.getInt(C.intervalUpdate, C.defaultIntervalUpdate);
		intervalWidth = mPrefs.getInt(C.intervalWidth, C.defaultIntervalWidth);

		readThread.start();

		registerReceiver(receiverStartRecord, new IntentFilter(C.actionStartRecord));
		registerReceiver(receiverStopRecord, new IntentFilter(C.actionStopRecord));
		registerReceiver(receiverClose, new IntentFilter(C.actionClose));

		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = getString(R.string.app_name);
			String description = getString(R.string.notification_channel_description);
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			mNM.createNotificationChannel(channel);
		}
		PendingIntent contentIntent =  TaskStackBuilder.create(this)

				.addNextIntentWithParentStack(new Intent(this, ActivityMain.class))
				.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pIStartRecord = PendingIntent.getBroadcast(this, 0, new Intent(C.actionStartRecord), PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pIStopRecord = PendingIntent.getBroadcast(this, 0, new Intent(C.actionStopRecord), PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pIClose = PendingIntent.getBroadcast(this, 0, new Intent(C.actionClose), PendingIntent.FLAG_UPDATE_CURRENT);

		mNotificationRead = new NotificationCompat.Builder(this,CHANNEL_ID)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.notify_read2))
				.setSmallIcon(R.drawable.icon_b)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon, null))
				.setWhen(0) // Removes the time
				.setOngoing(true)
				.setContentIntent(contentIntent) // PendingIntent.getActivity(this, 0, new Intent(this, ActivityMain.class), 0)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notify_read2)))
				.addAction(R.drawable.icon_play, getString(R.string.menu_record), pIStartRecord)
				.addAction(R.drawable.icon_times_ai, getString(R.string.menu_close), pIClose)
				.build();

		mNotificationRecord = new NotificationCompat.Builder(this,CHANNEL_ID)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.notify_record2))
				.setTicker(getString(R.string.notify_record))
				.setSmallIcon(R.drawable.icon_recording)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_recording, null))
				.setWhen(0)
				.setOngoing(true)
				.setContentIntent(contentIntent)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notify_record2)))
				.addAction(R.drawable.icon_pause, getString(R.string.menu_stop_record), pIStopRecord)
				.addAction(R.drawable.icon_times_ai, getString(R.string.menu_close), pIClose)
				.build();


		startForeground(10, mNotificationRead); // If not the AM service will be easily killed when a heavy-use memory app (like a browser or Google Maps) goes onto the foreground
	}





	@Override
	public void onDestroy() {
		if (recording)
			stopRecord();
		mNM.cancelAll();

		unregisterReceiver(receiverStartRecord);
		unregisterReceiver(receiverStopRecord);
		unregisterReceiver(receiverClose);

		try {
			readThread.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		synchronized (this) {
			readThread = null;
			notify();
		}
	}





	@Override
	public IBinder onBind(Intent intent) {
		return new ServiceReaderDataBinder();
	}





	@SuppressLint("NewApi")
	@SuppressWarnings("unchecked")
	
	private void read() {
		try {
			reader = new BufferedReader(new FileReader("/proc/meminfo"));
			s = reader.readLine();





			while (s != null) {
				// Memory is limited as far as we know
				while (memFree.size() >= maxSamples) {
					cpuTotal.remove(cpuTotal.size() - 1);
					cpuMT.remove(cpuMT.size() - 1);
					memoryMT.remove(memoryMT.size() - 1);

					memUsed.remove(memUsed.size() - 1);
					memAvailable.remove(memAvailable.size() - 1);
					memFree.remove(memFree.size() - 1);
					cached.remove(cached.size() - 1);
					threshold.remove(threshold.size() - 1);
				}
				if (mListSelected != null && !mListSelected.isEmpty()) {
					List<Integer> l = (List<Integer>) (mListSelected.get(0)).get(C.pFinalValue);
					if (l != null && l.size() >= maxSamples)
						for (Map<String, Object> m : mListSelected) {
							((List<Integer>) m.get(C.pFinalValue)).remove(l.size() - 1);
							((List<Integer>) m.get(C.pTPD)).remove(((List<Integer>) m.get(C.pTPD)).size() - 1);
						}
				}
				if (mListSelected != null && !mListSelected.isEmpty()) {
					for (Map<String, Object> m : mListSelected) {
						List<Integer> l = (List<Integer>) m.get(C.pFinalValue);
						if (l == null)
							break;
						while (l.size() >= maxSamples)
							l.remove(l.size() - 1);
						l = (List<Integer>) m.get(C.pTPD);
						while (l.size() >= maxSamples)
							l.remove(l.size() - 1);
					}
				}

				// Memory values. Percentages are calculated in the ActivityMain class.
				if (firstRead && s.startsWith("MemTotal:")) {
					memTotal = Integer.parseInt(s.split("[ ]+", 3)[1]);
					firstRead = false;
				} else if (s.startsWith("MemFree:"))
					memFree.add(0, s.split("[ ]+", 3)[1]);
				else if (s.startsWith("Cached:"))
					cached.add(0, s.split("[ ]+", 3)[1]);

				s = reader.readLine();
			}
			reader.close();

			am.getMemoryInfo(mi);
			if (mi == null) { // Sometimes mi is null
				memUsed.add(0, String.valueOf(0));
				memAvailable.add(0, String.valueOf(0));
				threshold.add(0, String.valueOf(0));
			} else {
				memUsed.add(0, String.valueOf(memTotal - mi.availMem/1024));
				memAvailable.add(0, String.valueOf(mi.availMem/1024));
				threshold.add(0, String.valueOf(mi.threshold/1024));
			}

			memoryMT.add(amMI[0].getTotalPrivateDirty());
			if (Build.VERSION.SDK_INT < 26) {
				reader = new BufferedReader(new FileReader("/proc/stat"));
				sa = reader.readLine().split("[ ]+", 9);
				work = Long.parseLong(sa[1]) + Long.parseLong(sa[2]) + Long.parseLong(sa[3]);
				total = work + Long.parseLong(sa[4]) + Long.parseLong(sa[5]) + Long.parseLong(sa[6]) + Long.parseLong(sa[7]);
				reader.close();
			}

			reader = new BufferedReader(new FileReader("/proc/" + pId + "/stat"));
			sa = reader.readLine().split("[ ]+", 18);
			workMT = Long.parseLong(sa[13]) + Long.parseLong(sa[14]) + Long.parseLong(sa[15]) + Long.parseLong(sa[16]);
			reader.close();

			if (mListSelected != null && !mListSelected.isEmpty()) {
				int[] arrayPIds = new int[mListSelected.size()];
				synchronized (mListSelected) {
					int n=0;
					for (Map<String, Object> p : mListSelected) {
						try {
							if (p.get(C.pDead) == null) {
								reader = new BufferedReader(new FileReader("/proc/" + p.get(C.pId) + "/stat"));
								arrayPIds[n] = Integer.valueOf((String) p.get(C.pId));
								++n;
								sa = reader.readLine().split("[ ]+", 18);
								p.put(C.work, (float) Long.parseLong(sa[13]) + Long.parseLong(sa[14]) + Long.parseLong(sa[15]) + Long.parseLong(sa[16]));
								reader.close();
							}
						} catch (FileNotFoundException e) {
							p.put(C.pDead, Boolean.TRUE);
							Intent intent = new Intent(C.actionDeadProcess);
							intent.putExtra(C.process, (Serializable) p);
							sendBroadcast(intent);
						}
					}
				}

				MemoryInfo[] mip = am.getProcessMemoryInfo(arrayPIds);
				int n = 0;
				for (Map<String, Object> entry : mListSelected) {
					List<Integer> l = (List<Integer>) entry.get(C.pTPD);
					if (l == null) {
						l = new ArrayList<Integer>();
						entry.put(C.pTPD, l);
					}
					if (entry.get(C.pDead) == null) {

							l.add(0, mip[n].getTotalPrivateDirty());
							++n;
					} else l.add(0, 0);
				}
	}

			if (totalBefore != 0) {
				totalT = total - totalBefore;
				workT = work - workBefore;
				workMTT = workMT - workMTBefore;

				cpuTotal.add(0, restrictPercentage(workT * 100 / (float) totalT));
				cpuMT.add(0, restrictPercentage(workMTT * 100 / (float) totalT));

				if (mListSelected != null && !mListSelected.isEmpty()) {
					int workPT = 0;
					List<Float> l;

					synchronized (mListSelected) {
						for (Map<String, Object> p : mListSelected) {
							if (p.get(C.workBefore) == null)
								break;
							l = (List<Float>) p.get(C.pFinalValue);
							if (l == null) {
								l = new ArrayList<Float>();
								p.put(C.pFinalValue, l);
							}
							while (l.size() >= maxSamples)
								l.remove(l.size() - 1);

							workPT = (int) ((Float) p.get(C.work) - (Float) p.get(C.workBefore));
							l.add(0, restrictPercentage(workPT * 100 / (float) totalT));
						}
					}
				}
			}

			totalBefore = total;
			workBefore = work;
			workMTBefore = workMT;

			if (mListSelected != null && !mListSelected.isEmpty())
				for (Map<String, Object> p : mListSelected)
					p.put(C.workBefore, p.get(C.work));

			reader.close();

			if (recording)
				record();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}





	private float restrictPercentage(float percentage) {
		if (percentage > 100)
			return 100;
		else if (percentage < 0)
			return 0;
		else return percentage;
	}





	@SuppressWarnings("unchecked")
	private void record() {
		if (mW == null) {
			File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DropsyncFiles");
			dir.mkdirs();
			mFile = new File(dir, new StringBuilder().append(getString(R.string.app_name)).append("Record-").append(getDate()).append(".csv").toString());

			try {
				mW = new BufferedWriter(new FileWriter(mFile));
			} catch (IOException e) {
				notifyError(e);
				return;
			}
		}

		try {
			if (topRow) {
				StringBuilder sb = new StringBuilder()
						.append(getString(R.string.app_name))
						.append(" Record,Starting date and time:,")
						.append(getDate())
						.append(",Read interval (ms):,")
						.append(intervalRead)
						.append(",MemTotal (kB),")
						.append(memTotal)
						.append("\nTotal CPU usage (%),MoniTrack (Pid ").append(Process.myPid()).append(") CPU usage (%),MoniTrack Memory (kB)");
				if (mListSelected != null && !mListSelected.isEmpty())
					for (Map<String, Object> p : mListSelected)
						sb.append(",").append(p.get(C.pAppName)).append(" (Pid ").append(p.get(C.pId)).append(") CPU usage (%)")
						  .append(",").append(p.get(C.pAppName)).append(" Memory (kB)");

				sb.append(",,Memory used (kB),Memory available (MemFree+Cached) (kB),MemFree (kB),Cached (kB),Threshold (kB)");

				mW.write(sb.toString());
				mNM.notify(10, mNotificationRecord);
				topRow = false;
			}

			StringBuilder sb = new StringBuilder()
					.append("\n").append(cpuTotal.get(0))
					.append(",").append(cpuMT.get(0))
					.append(",").append(memoryMT.get(0));
			if (mListSelected != null && !mListSelected.isEmpty())
				for (Map<String, Object> p : mListSelected) {
					if (p.get(C.pDead) != null)
						sb.append(",DEAD,DEAD");
					else sb.append(",").append(((List<Integer>) p.get(C.pFinalValue)).get(0))
							.append(",").append(((List<Integer>) p.get(C.pTPD)).get(0));
				}
			sb.append(",")
					.append(",").append(memUsed.get(0))
					.append(",").append(memAvailable.get(0))
					.append(",").append(memFree.get(0))
					.append(",").append(cached.get(0))
					.append(",").append(threshold.get(0));

			mW.write(sb.toString());
		} catch (IOException e) {
			notifyError(e);
		}
	}





	void startRecord() {
		if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			Toast.makeText(this, getString(R.string.w_main_storage_permission), Toast.LENGTH_LONG).show();
			return;
		}
		recording = true;
		sendBroadcast(new Intent(C.actionSetIconRecord));
	}

	void stopRecord() {
		recording = false;
		sendBroadcast(new Intent(C.actionSetIconRecord));
		try {
			mW.flush();
			mW.close();
			mW = null;

		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(Uri.fromFile(mFile)));

			Toast.makeText(this, new StringBuilder().append(getString(R.string.app_name)).append("Record-").append(getDate()).append(".csv ")
					.append(getString(R.string.notify_toast_saved))
					.append(" " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/DropsyncFiles").append(" and uploaded to Dropbox."), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, getString(R.string.notify_toast_error) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
		topRow = true;
		mNM.notify(10, mNotificationRead);
	}

	boolean isRecording() {
		return recording;
	}





	void notifyError(final IOException e) {
		e.printStackTrace();
		if (mW != null)
			stopRecord();
		else {
			recording = false;
			sendBroadcast(new Intent(C.actionSetIconRecord));

			// http://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(ServiceReader.this, getString(R.string.notify_toast_error_2) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
				}
			});

			mNM.notify(10, mNotificationRead);
		}
	}





	private String getDate() {
		Calendar c = Calendar.getInstance();
		DecimalFormat df = new DecimalFormat("00");
		return new StringBuilder()
				.append(df.format(c.get(Calendar.YEAR))).append("-")
				.append(df.format(c.get(Calendar.MONTH) + 1)).append("-")
				.append(df.format(c.get(Calendar.DATE))).append("-")
				.append(df.format(c.get(Calendar.HOUR_OF_DAY))).append("-")
				.append(df.format(c.get(Calendar.MINUTE))).append("-")
				.append(df.format(c.get(Calendar.SECOND))).toString();
	}





	void setIntervals(int intervalRead, int intervalUpdate, int intervalWidth) {
		this.intervalRead = intervalRead;
		this.intervalUpdate = intervalUpdate;
		this.intervalWidth = intervalWidth;
	}





	List<Map<String, Object>> getProcesses() {
		return mListSelected != null && !mListSelected.isEmpty() ? mListSelected : null;
	}

	void addProcess(Map<String, Object> process) {
		
		if (mListSelected == null)
			mListSelected = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
		mListSelected.add(process);
	}

	void removeProcess(Map<String, Object> process) {
		synchronized (mListSelected) {
			Iterator<Map<String, Object>> i = mListSelected.iterator();
			while (i.hasNext())
				if (i.next().get(C.pId).equals(process.get(C.pId))) {
					i.remove();
					Log.i(getString(R.string.w_processes_dead_notification), (String) process.get(C.pName));
				}
		}
	}





	int getIntervalRead() {
		return 1000;
	}

	int getIntervalUpdate() {
		return 1000;
	}

	int getIntervalWidth() {
		return 1;
	}





	List<Float> getCPUTotalP() {
		return cpuTotal;
	}

	List<Float> getCPUMTP() {
		return cpuMT;
	}

	List<Integer> getMemoryMT() {
		return memoryMT;
	}

	int getMemTotal() {
		return memTotal;
	}

	List<String> getMemUsed() {
		return memUsed;
	}

	List<String> getMemAvailable() {
		return memAvailable;
	}

	List<String> getMemFree() {
		return memFree;
	}

	List<String> getCached() {
		return cached;
	}

	List<String> getThreshold() {
		return threshold;
	}
}
