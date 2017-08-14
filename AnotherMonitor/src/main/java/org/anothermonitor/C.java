/*
 * 2010-2017 (C) Antonio Redondo
 * http://antonioredondo.com
 * http://github.com/AntonioRedondo/AnotherMonitor
 *
 * Code under the terms of the GNU General Public License v3.
 *
 */

package org.anothermonitor;

class C {

	static final String prefs = "Prefs";
	static final String sbh = "status_bar_height";
	static final String nbh = "navigation_bar_height";
	static final String dimen = "dimen";
	static final String android = "android";
	static final String europeLondon = "Europe/London";
	static final String marketDetails = "market://details?id=";
	static final int defaultIntervalRead = 1000;
	static final int defaultIntervalUpdate = 1000;
	static final int defaultIntervalWidth = 1;
	
	// ServiceReader
	static final String readThread = "readThread";
	
	static final String actionStartRecord = "actionRecord";
	static final String actionStopRecord = "actionStop";
	static final String actionClose = "actionClose";
	static final String actionSetIconRecord = "actionSetIconRecord";
	static final String actionDeadProcess = "actionRemoveProcess";
	static final String actionFinishActivity = "actionCloseActivity";

	static final String pId = "pId";
	static final String pName = "pName";
	static final String pPackage = "pPackage";
	static final String pAppName = "pAppName";
	static final String pTPD = "pPTD";
	static final String pSelected = "pSelected";
	static final String pDead = "pDead";
	static final String pColour = "pColour";
	static final String work = "work";
	static final String workBefore = "workBefore";
	static final String pFinalValue = "finalValue";
	static final String process = "process";
	static final String screenRotated = "screenRotated";
	static final String listSelected = "listSelected";
	static final String listProcesses = "listProcesses";
	
	// ActivityMain
	static final int storagePermission = 1;
	static final String kB = "kB";
	static final String percent = "%";
//	static final String drawThread = "drawThread";
	static final String menuShown = "menuShown";
	static final String settingsShown = "settingsShown";
	static final String orientation = "orientation";
	static final String processesMode = "processesMode";
	static final String canvasLocked = "canvasLocked";
	
	static final String welcome = "firstTime";
	static final String welcomeDate = "firstTimeDate";
	static final String firstTimeProcesses = "firstTimeProcesses";
	static final String feedbackFirstTime = "feedbackFirstTime";
	static final String feedbackDone = "feedbackDone";
	
	static final String intervalRead = "intervalRead";
	static final String intervalUpdate = "intervalUpdate";
	static final String intervalWidth = "intervalWidth";
	
	static final String cpuTotal = "cpuTotalD";
	static final String cpuAM = "cpuAMD";
	static final String memUsed = "memUsedD";
	static final String memAvailable = "memAvailableD";
	static final String memFree = "memFreeD";
	static final String cached = "cachedD";
	static final String threshold = "thresholdD";
	
	// GraphicView
	static final String processMode = "processMode";
	static final int processesModeShowCPU = 0;
	static final int processesModeShowMemory = 1;

	static final String graphicMode = "graphicMode";
	static final int graphicModeShowMemory = 0;
	static final int graphicModeHideMemory = 1;
	
	// ActivityPreferences
	static final String currentItem = "ci";
	
	static final String mSRead = "mSRead";
	static final String mSUpdate = "mSUpdate";
	static final String mSWidth = "mSWidth";
	
	static final String mCBMemFreeD = "memFreeD";
	static final String mCBBuffersD = "buffersD";
	static final String mCBCachedD = "cachedD";
	static final String mCBActiveD = "activeD";
	static final String mCBInactiveD = "inactiveD";
	static final String mCBSwapTotalD = "swapTotalD";
	static final String mCBDirtyD = "dirtyD";
	static final String mCBCpuTotalD = "cpuTotalD";
	static final String mCBCpuAMD = "cpuAMD";
//	static final String mCBCpuRestD = "cpuRestD";
}
