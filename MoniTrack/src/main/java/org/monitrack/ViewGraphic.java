

package org.monitrack;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.AttributeSet;
import android.view.TextureView;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.Rect;

public class ViewGraphic extends TextureView {
	
	private boolean graphicInitialised,
					 cpuTotalD, cpuMTD,
					 memUsedD, memAvailableD, memFreeD, cachedD, thresholdD;
	private int processesMode, graphicMode, yTop, yBottom, xLeft, xRight, yBottomTextSpace=25, xLeftTextSpace=10, yLegendSpace = 8, graphicHeight, graphicWidth, minutes, seconds, intervalTotalNumber, memTotal,
				 thickParam, thickGrid, thickEdges, tempVar, textSize, textSizeLegend, yTopSeparation;
	private String readIntervalText, updateIntervalText, graphicIntervaWidthlText, recordingText = "Recording";
	private Rect bgRect;
	private Paint bgPaint, circlePaint, textPaintRecording, textPaintLegend, textPaintLegendV, textPaintInside, linesEdgePaint, linesGridPaint,
			cpuTotalPaint, cpuMTPaint,
			memUsedPaint, memAvailablePaint, memFreePaint, cachedPaint, thresholdPaint;
	private List<Float> cpuTotal, cpuMT;
	private List<Integer> memoryMT;
	private List<String> memUsed, memAvailable, memFree, cached, threshold;
	private Map<String, Paint> paints;
	private DecimalFormat mFormatPercent = new DecimalFormat("0.#");
	private ServiceReader mSR;
	private Resources res;
	private Thread mThread;
	
	
	
	
	
	public ViewGraphic(Context context, AttributeSet attrs) {
		super(context, attrs);

		res = getResources();
		float sD = res.getDisplayMetrics().density;

		readIntervalText = res.getString(R.string.interval_read);
		updateIntervalText = res.getString(R.string.interval_update);
		graphicIntervaWidthlText = res.getString(R.string.interval_width);

		thickGrid = (int) Math.ceil(1*sD);
		thickParam = (int) Math.ceil(1*sD);
		thickEdges = (int) Math.ceil(2*sD);

		textSize = (int) Math.ceil(10*sD);
		textSizeLegend = (int) Math.ceil(10*sD);
		
		yTopSeparation = (int) Math.ceil(13*sD);
	}
	
	
	
	

	@SuppressWarnings("unchecked")
	protected void onDrawCustomised(Canvas canvas, Thread thread) {
		if (mSR == null || canvas == null)
			return;
		else if (!graphicInitialised)
			initializeGraphic();
		mThread = thread;

		if (mThread == null || mThread.isInterrupted())
			return;
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		if (mThread.isInterrupted())
			return;
		canvas.drawRect(bgRect, bgPaint);

		for (float n=0.1f; n<1.0f; n=n+0.2f) {
			if (mThread.isInterrupted())
				return;
			canvas.drawLine(xLeft, yTop + graphicHeight*n, xRight, yTop + graphicHeight*n, linesGridPaint);
		}
		

		for (int n=1; n<=minutes; ++n) {
			tempVar = xRight-n*mSR.getIntervalWidth()*(int)(60/((float) mSR.getIntervalRead()/1000));
			if (mThread.isInterrupted())
				return;
			canvas.drawLine(tempVar, yTop, tempVar, yBottom, linesGridPaint);

		}

		if (cpuTotalD)
			drawLineFloat(cpuTotal, canvas, cpuTotalPaint);
		if (cpuMTD)
			if (processesMode == C.processesModeShowCPU)
				drawLineFloat(cpuMT, canvas, cpuMTPaint);
			else drawLineInteger(memoryMT, canvas, cpuMTPaint);
		
		List<Map<String, Object>> l = mSR.getProcesses();
		if (l != null && !l.isEmpty())
			for (int n=0; n<l.size(); ++n) {
				if ((Boolean) l.get(n).get(C.pSelected)) {
					if (paints == null)
						paints = new HashMap<String, Paint>();
					Paint paint = paints.get(l.get(n).get(C.pId));
					if (paint == null) {
						paint = getPaint((Integer) l.get(n).get(C.pColour), Paint.Align.CENTER, 12, false, thickParam);
						paints.put((String) l.get(n).get(C.pId), paint);
					}
					if (processesMode == C.processesModeShowCPU) // processesMode==0 CPU usage, processesMode==1 Memory
						drawLineFloat((List<Float>) l.get(n).get(C.pFinalValue), canvas, paint);
					else drawLineInteger((List<Integer>) l.get(n).get(C.pTPD), canvas, paint); 
				}
			}
		
		if (graphicMode == C.graphicModeShowMemory) {
			if (memUsedD)
				drawLine(memUsed, canvas, memUsedPaint);
			if (memAvailableD)
				drawLine(memAvailable, canvas, memAvailablePaint);
			if (memFreeD)
				drawLine(memFree, canvas, memFreePaint);
			if (cachedD)
				drawLine(cached, canvas, cachedPaint);
			if (thresholdD)
				drawLine(threshold, canvas, thresholdPaint);
		}
		
		// Horizontal edges
		if (mThread.isInterrupted())
			return;
		canvas.drawLine(xLeft, yTop, xRight, yTop, linesEdgePaint);
		if (mThread.isInterrupted())
			return;
		canvas.drawLine(xLeft, yBottom, xRight, yBottom, linesEdgePaint);
		
		// Vertical edges
		if (mThread.isInterrupted())
			return;
		canvas.drawLine(xLeft, yTop, xLeft, yBottom, linesEdgePaint);
		if (mThread.isInterrupted())
			return;
		canvas.drawLine(xRight, yBottom, xRight, yTop, linesEdgePaint);


		for (int n=0; n<=minutes; ++n) {
			if (mThread.isInterrupted())
				return;
			canvas.drawText(n+"'", xRight-n*mSR.getIntervalWidth()*(int)(60/((float)mSR.getIntervalRead()/1000)), yBottom+yBottomTextSpace, textPaintLegend);
		}
		if (minutes==0) {
			if (mThread.isInterrupted())
				return;
			canvas.drawText(seconds+"\"", xLeft, yBottom+yBottomTextSpace, textPaintLegend);
		}

		tempVar = xLeft-xLeftTextSpace;
		if (mThread.isInterrupted())
			return;
		canvas.drawText("100%", tempVar, yTop + 5, textPaintLegendV);
		if (mThread.isInterrupted())
			return;
		canvas.drawText("90%", tempVar, yTop + graphicHeight*0.1f + yLegendSpace, textPaintLegendV);
		if (mThread.isInterrupted())
			return;
		canvas.drawText("70%", tempVar, yTop + graphicHeight*0.3f + yLegendSpace, textPaintLegendV);
		if (mThread.isInterrupted())
			return;
		canvas.drawText("50%", tempVar, yTop + graphicHeight*0.5f + yLegendSpace, textPaintLegendV);
		if (mThread.isInterrupted())
			return;
		canvas.drawText("30%", tempVar, yTop + graphicHeight*0.7f + yLegendSpace, textPaintLegendV);
		if (mThread.isInterrupted())
			return;
		canvas.drawText("10%", tempVar, yTop + graphicHeight*0.9f + yLegendSpace, textPaintLegendV);
		if (mThread.isInterrupted())
			return;
		canvas.drawText("0%", tempVar, yBottom + yLegendSpace, textPaintLegendV);
		
		// Read interval, Update interval, Graphic interval width text
		if (mThread.isInterrupted())
			return;
		canvas.drawText(readIntervalText + " " + mFormatPercent.format(mSR.getIntervalRead()/(float)1000) + " s", xLeft+15, yTop+5+yTopSeparation, textPaintInside);
		if (mThread.isInterrupted())
			return;
		canvas.drawText(updateIntervalText + " " + mFormatPercent.format(mSR.getIntervalUpdate()/(float)1000) + " s", xLeft+15, yTop+5+yTopSeparation*2, textPaintInside);
		if (mThread.isInterrupted())
			return;
		canvas.drawText(graphicIntervaWidthlText + " " + mSR.getIntervalWidth() + " dp", xLeft+15, yTop+5+yTopSeparation*3, textPaintInside);


		if (mSR.isRecording()) {
			if (mThread.isInterrupted())
				return;
			canvas.drawText(recordingText, xRight-40, yTop+5+yTopSeparation, textPaintRecording);
			if (mThread.isInterrupted())
				return;
			canvas.drawCircle(xRight-25, yTop+yTopSeparation, 7, circlePaint);
		}
	}
	
	
	
	
	
	private void drawLineInteger(List<Integer> y, Canvas canvas, Paint paint) {
		if(y != null && y.size()>1)
			for(int m=0; m < (y.size()-1) && m < intervalTotalNumber; ++m) {
				if (mThread.isInterrupted())
					return;
				canvas.drawLine(xRight - mSR.getIntervalWidth()*m,
						yBottom - y.get(m)*graphicHeight/memTotal,
						xRight - mSR.getIntervalWidth()*m - mSR.getIntervalWidth(),
						yBottom - y.get(m+1)*graphicHeight/memTotal,
						paint);
			}
	}
	
	
	
	
	
	private void drawLine(List<String> y, Canvas canvas, Paint paint) {
		if(y != null && y.size()>1)
			for(int m=0; m < (y.size()-1) && m < intervalTotalNumber; ++m) {
				if (mThread.isInterrupted())
					return;
				canvas.drawLine(xRight - mSR.getIntervalWidth()*m,
						yBottom - Integer.parseInt(y.get(m))*graphicHeight/memTotal,
						xRight - mSR.getIntervalWidth()*m - mSR.getIntervalWidth(),
						yBottom - Integer.parseInt(y.get(m+1))*graphicHeight/memTotal,
						paint);
			}
	}
	
	
	
	
	
	private void drawLineFloat(List<Float> y, Canvas canvas, Paint paint) {
		if(y != null && y.size()>1)
			for(int m=0; m < (y.size()-1) && m < intervalTotalNumber; ++m) {
				if (mThread.isInterrupted())
					return;
				canvas.drawLine(xRight - mSR.getIntervalWidth()*m,
						yBottom - y.get(m)*graphicHeight/100,
						xRight - mSR.getIntervalWidth()*m-mSR.getIntervalWidth(),
						yBottom - y.get(m+1)*graphicHeight/100,
						paint);
			}
	}
	
	
	
	
	
	private void initializeGraphic() {
		yTop = (int) (getHeight()*0.1);
		yBottom = (int) (getHeight()*0.88);
		xLeft = (int) (getWidth()*0.14);
		xRight = (int) (getWidth()*0.94);
		
		graphicWidth = xRight - xLeft;
		graphicHeight = yBottom - yTop;
		
		bgRect = new Rect(xLeft, yTop, xRight, yBottom);
		
		calculateInnerVariables();

		bgPaint = getPaint(res.getColor(R.color.transparentGray), Paint.Align.CENTER, 12, false, 0);
		circlePaint = getPaint(Color.RED, Paint.Align.CENTER, 12, false, 0);

		linesEdgePaint = getPaint(res.getColor(R.color.AliceBlue), Paint.Align.CENTER, 12, false, thickEdges);
		linesGridPaint = getPaint(res.getColor(R.color.androidBlue2), Paint.Align.CENTER, 12, false, thickGrid);
		linesGridPaint.setStyle(Style.STROKE);
		linesGridPaint.setPathEffect(new DashPathEffect(new float[] {8, 8}, 0));

		cpuTotalPaint = getPaint(Color.parseColor("#BF930F"), Paint.Align.CENTER, 12, false, thickParam);
		cpuMTPaint = getPaint(res.getColor(R.color.DarkCyan), Paint.Align.CENTER, 12, false, thickParam);

		memUsedPaint = getPaint(res.getColor(R.color.DarkOrange), Paint.Align.CENTER, 12, false, thickParam);
		memAvailablePaint = getPaint(R.color.DarkOliveGreen, Paint.Align.CENTER, 12, false, thickParam);
		memFreePaint = getPaint(Color.parseColor("#804000"), Paint.Align.CENTER, 12, false, thickParam);
		cachedPaint = getPaint(R.color.purple, Paint.Align.CENTER, 12, false, thickParam);
		thresholdPaint = getPaint(Color.GREEN, Paint.Align.CENTER, 12, false, thickParam);
		
		textPaintRecording = getPaint(Color.BLACK, Paint.Align.RIGHT, textSize, true, 0);
		textPaintInside = getPaint(Color.WHITE, Paint.Align.LEFT, textSize, true, 0);
		textPaintLegend = getPaint(Color.LTGRAY, Paint.Align.CENTER, textSizeLegend, true, 0);
		textPaintLegendV = getPaint(Color.LTGRAY, Paint.Align.RIGHT, textSizeLegend, true, 0);
		
		graphicInitialised = true;
	}

	
	
	
	
	private Paint getPaint(int color, Paint.Align textAlign, int textSize, boolean antiAlias, float strokeWidth) {
		Paint p = new Paint();
		p.setColor(color);
		p.setTextSize(textSize);
		p.setTextAlign(textAlign);
		p.setAntiAlias(antiAlias);
		p.setStrokeWidth(strokeWidth);
		return p;
	}
	
	
	
	
	
	void setService(ServiceReader sr) {
		mSR = sr;
		cpuTotal = mSR.getCPUTotalP();
		cpuMT = mSR.getCPUMTP();
		memoryMT = mSR.getMemoryMT();
		memTotal = mSR.getMemTotal();
		memUsed = mSR.getMemUsed();
		memAvailable = mSR.getMemAvailable();
		memFree = mSR.getMemFree();
		cached = mSR.getCached();
		threshold = mSR.getThreshold();
	}
	
	
	
	
	
	void setParameters(boolean cpuTotalD, boolean cpuMTD,
		boolean memUsedD, boolean memAvailableD, boolean memFreeD, boolean cachedD, boolean thresholdD) {
		this.cpuTotalD = cpuTotalD;
		this.cpuMTD = cpuMTD;
		this.memUsedD = memUsedD;
		this.memAvailableD = memAvailableD;
		this.memFreeD = memFreeD;
		this.cachedD = cachedD;
		this.thresholdD = thresholdD;
	}
	
	
	
	
	
	void setProcessesMode(int processesMode) {
		this.processesMode = processesMode;
	}
	
	
	
	
	
	void setGraphicMode(int graphicMode) {
		this.graphicMode = graphicMode;
	}
	
	
	
	
	
	void calculateInnerVariables() {
		intervalTotalNumber = (int) Math.ceil(graphicWidth/mSR.getIntervalWidth());
		minutes = (int) Math.floor(intervalTotalNumber*mSR.getIntervalRead()/1000/60);
		seconds = (int) Math.floor(intervalTotalNumber*mSR.getIntervalRead()/1000);
	}
}
