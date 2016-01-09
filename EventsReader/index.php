<!DOCTYPE HTML>
<html>
	<head>
		<title>Sign-In</title>
		<link rel="stylesheet" type="text/css" href="css/style.css">
	</head>
	<?php
		$dir = "files";
		//create necessary directory to store recordings
		if (!file_exists($dir) ) {
			mkdir($dir);
			echo "Directory ".$dir." created...";
		}
	?>
	<body id="body-color" >
		<br/><br/>
		Place: <input type="text" name="place" id = "place" value=""/><br/>
		<br/>
			<br/>
		<button id="start">Start Recording</button>
		<button id="stop" >Stop Recording</button>
			<br/>
			<br/>
		<div id="container" style="padding:1em 2em;"></div>
		<div id = "audioRecorder">Audio Recorder Info</div>
		<div id = "eventsReader" class = "demo">Events Info</div>
		<div id = "timeSynchronizer" class = "demo">Time Synchronizer Info</div>
		<div id = "gcm" class = "demo">GCM Info</div>
		
		<script src="js/jquery.js"></script>
		<script src="js/DisplayMessage.js"></script>
		<script src="js/AudioRecorder.js"></script>
		<script src="js/RecordRTC.js"></script>
		<script src="js/FileUtility.js"></script>
		<script src="js/TimeSynchronizer.js"></script>
		<script src="js/EventsReader.js"></script>
		<script src="js/GCMHandler.js"></script>
		
		<script>
			var bAudioFile, bAudioTimeFile, bTimeSyncFile, bEventsFile;
					
			$("document").ready(function(){
					
					/* get the fields from the login page based on their id */
					var stop = document.getElementById('stop');
					var start = document.getElementById('start');
					var place = document.getElementById('place');
					
					/* ends of fileName */
					var audioEnds = 'browser_audio';
					var audioTimeEnds = 'browser_audio_time';
					var timeSyncEnds = 'browser_time_sync';
					var eventEnds = 'browser_events';
					
					/* List of events for which handler is to be added. */
					var eventsList = [
									"keyup",
									"keydown",
									"mousedown",
									"mouseup",
									"mousemove"
									];
					stop.disabled = true;
				
					
					/* start button click event listener*/
				    start.onclick = function(){
				    	console.log('start clicked');
						
						var placeValue = place.value;
						var dir = "<?php echo $dir?>";
						var date = new Date();
						var timeVar = date.getTime();
						
						/*Send start message to android device with file initials*/
						var msgToDevice = "start," + timeVar + "_" + placeValue;
						 alert(msgToDevice);
						sendMessageToDevice(msgToDevice);
					   
						
						bAudioFile = getFileName(dir, timeVar, placeValue, audioEnds);
						bAudioTimeFile = getFileName(dir, timeVar, placeValue, audioTimeEnds);
						bTimeSyncFile = getFileName(dir, timeVar, placeValue, timeSyncEnds);
						bEventsFile = getFileName(dir, timeVar, placeValue, eventEnds);
						
						start.disabled = true;
						stop.disabled = false;
						
						startAudioRecordings(bAudioFile, bAudioTimeFile);
						addEventsHandler(bEventsFile, eventsList);
						startTimeSync(bTimeSyncFile);
						
						
						
					};


					/* stop buttion click event listener */
					stop.onclick = function() {
						console.log('stop clicked');
						sendMessageToDevice("stop");
						stopAudioRecording();
						stopTimeSync();
						removeEventsHandler(eventsList);
						stop.disabled = true;
						start.disabled = false;
						console.log('stop clicked');
						
				    };
			});  
		</script>

	</body>

</html> 
