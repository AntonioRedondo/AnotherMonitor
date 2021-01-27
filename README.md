# MoniTrack

.
<img src="https://lh4.ggpht.com/gfwMh4Ih0VD0AaxI8_eh11m6CRu_zSW6-U6F25AjCdlUjCkliWHBgJMhDb3ePdl_EMoT" width="180px" />
<img src="https://lh4.ggpht.com/fugTTF9i76nsfnpWfv34xe1Xz5u4dDWOqbTYkBaPrzud4zPuYIZtQQhEyH7pX9POjYU" width="180px" />
<img src="https://lh5.ggpht.com/96BmklbBOEOgL5mmXZQkofwswLGEzY4Zf6EirtF2nOBgf_cTo86RxuzCInv7etIfNgTO" width="180px" />


## How it works
The <big><b>MoniTrack</b></big>app allows you to monitor and track CPU and Memory Usage of processes of your choice. In addition, you can:
\n
\n	<b>- View System Statistics</b>
\n	<b>- View Battery Statistics</b>
\n	<b>- Record CPU and Memory Tracking</b>
\n	<b>- Load Records</b>
\n	<b>- Store Record and Upload to Dropbox</b>
\n	<b>- Kill Processes</b>
\n
\n	<big><b>Action bar</b></big>\n\n
	This is the bar at the top. It consists of the application\'s name, a record/stop button and a three-dot dropdown.

	Record/Stop button: start/stop recording of CPU and Memory tracking. Once stopped, a csv is saved in the DropSync folder allocated by your Dropbox application.

	Three-dot dropdown: Navigate to other pages of the application other than the home screen, including this help guide.

\n\n
	<big><b>Home Screen</b></big>\n\n

	The Home Screen consists of three panels:
		Process Panel, Memory Panel, and Graph Panel.\n

\n\n	<big><b>Process Panel</b></big>\n\n

	This lists all the processes being tracked. Click \"Add Process\" to add other running processes to the list and track them. You will see the name as well as the Process ID (denoted by Pid).

	Click on the Memory or CPU Usage button in the top right to toggle between displaying the memory and cpu values of the processes.

	If the device has multiple cores, it displays the combined aggregate.
\n\n
	<big><b>Memory Panel</b></big>\n\n

	This shows the device\'s memory parameters.

	\n	<b>Used Memory:</b> Memory currently in use.\n

	\n	<b>Available Memory:</b> Memory available. It is equivalent to \"Free Memory\" + \"Cached Memory\".\n

	\n		<b>Free Memory:</b> The amount of memory currently free for use.\n

	\n		<b>Cached Memory:</b>the in-memory cache.\n

	\n		<b>Limit:</b> Android\'s set limit for Available Memory at which it is too low and gets killed by Android. This is not native to Linux.
	\n\n

	<big><b>Graph Panel</b></big>\n\n

	The graph shows the cpu and memory usage over time. If you click on the rec button at the top, you can start recording the graph values. Upon clicking the button again, you can stop recording and the csv will be saved to your device and DropboxSync.

\n\n	<big><b>System Stats</b></big>\n\n
	This lists operating system statistics by effectively running shell commands and displaying the output. The <b>"TOP"</b> button will run "top" and the <b>"CPU Info"</b> button will display the CPU information and the <b>"Device Info"</b> will display the information about the device.

	\n\n<big><b>Battery Stats</b></big>\n\n
	This lists battery statistics by reading from system files (in sys/class/power_supply/battery). You can see

	\n	<b>Battery Status:</b> Whether it is charging or not
	\n	<b>Battery Capacity:</b> Maximum power compared to when first manufactured
	\n	<b>Battery Health:</b> How well does the battery perform
	\n	<b>Battery Technology:</b> The technology of the battery
\n
	You can click "Power Usage Summary" to go to the summary of your power usage from your system.
\n\n
	<big><b>Records</b></big>\n\n
	Click on "Load Recording" to open a file picker. Open a previously saved csv file of a tracking record and it will be displayed.
\n\n
	<big><b>Kill Process</b></big>\n\n
	You can enter the package name of the process in the textbox and the application
	will kill the process with that name by using a system service. You can click
	on "View Running Processes" to see System Stats and find the names of running
		processes with package names.	</string>
