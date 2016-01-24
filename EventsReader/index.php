<!DOCTYPE HTML>
<html>
	<head>
		<title>Sign-In</title>
		<link rel="stylesheet" type="text/css" href="css/style.css">
		<style>
		#box {
		    width: 320px;
		    padding: 10px;
		    border: 5px solid gray;
		    margin: 0;
		    text-align: left;
		    background-color: #dcdcbc;
		}
</style>
	</head>
	<?php
		$dir = "files";
		//create necessary directory to store recordings
		if (!file_exists($dir) ) {
			mkdir($dir);
			echo "Directory ".$dir." created...";
		}
	?>
	<body id="body-color">
		<br/><br/>

		<div align = 'center' vertical-align = 'middle'>
<div id = 'box'>
		<span >
		<div >
				Place: <input type="text" name="place" id = "place" value=""/>
				</br></br>
				Audio file name: 
				 <select id = 'audioSelect'>
					  <option value="amazing">amazing</option>
					  <option value="comeon">comeon</option>
					  <option value="wajile">wajile</option>
					  <option value="pass">pass</option>
					  <option value="loveyou">loveyou</option>
					  <option value="harmony">harmony</option>
				</select> 
					<br/>
					 <audio id = 'player'>
					  	<source id = 'wavSrc' src="amazing.wav" type="audio/wav">
						Your browser does not support the audio element.
					</audio> 
					<br>
		</div>

		<div>
			Time Elapsed: 
			<span id = "time">0</span>
		</div>
	</br>
		
		<div >
			<button id="start">Start Recording</button>
			<button id="stop" >Stop Recording</button>
		</div>
		
		</br>
		Password: <input type="password" name="password" id = 'password' onfocus="start()" onblur = "stop()"  disabled>
		</br>
		<br>
			<div id = "status">Services Status  </br>
				.....................................</div>
			<div id = "audioRecorder">Audio Recording: NOT STARTED </div>
			<div id = "audioPlayer" class = "audioPlayer">Audio Player: NOT STARTED</div>
			<div id = "eventsReader" class = "demo">Events Recording: NOT STARTED</div>
			<div id = "timeSynchronizer" class = "demo">Time Synchronizer: NOT STARTED </div>
			<div id = "gcm" class = "demo">GCM Message: NOT SENT</div>
			<div id="container" style="padding:1em 1em;"></div>
		
		</span>
<div>
</div>
	

		
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
					var password = document.getElementById('password');
					var audioSelectField = document.getElementById("audioSelect");
						
					/* ends of fileName */
					var audioEnds = 'browser_audio';
					var audioTimeEnds = 'browser_audio_time';
					var timeSyncEnds = 'browser_time_sync';
					var eventEnds = 'browser_events';

					var timeVar, placeValue, dir;
					
					/* List of events for which handler is to be added. */
					var eventsList = [
									"keyup",
									"keydown",
									"mousedown",
									"mouseup",
									"mousemove"
									];
					stop.disabled = true;
				
					password.onfocus = function(){
						console.log('password field focus');
						var audioName = audioSelectField.options[audioSelectField.selectedIndex].value;
						bAudioFile = getFileName(dir, timeVar, placeValue, audioName, audioEnds);
						bAudioTimeFile = getFileName(dir, timeVar, placeValue,audioName, audioTimeEnds);
						bTimeSyncFile = getFileName(dir, timeVar, placeValue, audioName, timeSyncEnds);
						bEventsFile = getFileName(dir, timeVar, placeValue, audioName, eventEnds);
						
						isStart = true;
						startTime();
						
						playAudio();
						startAudioRecordings(bAudioFile, bAudioTimeFile);
						addEventsHandler(bEventsFile, eventsList);
						startTimeSync(bTimeSyncFile);
						
						
					}
					password.onblur = function(){
						console.log('password field blur');
					}

					/* start button click event listener*/
				    start.onclick = function(){
				    	console.log('start clicked');
				    	placeValue = place.value;
						dir = "<?php echo $dir?>";
						var date = new Date();
						timeVar = date.getTime();
						
						var audioName = audioSelectField.options[audioSelectField.selectedIndex].value;
					
						/* Send start message to android device with file initials*/
						var msgToDevice = "start," + timeVar + "_" + placeValue + "_" + audioName;
						 //alert(msgToDevice);
						sendMessageToDevice(msgToDevice);

						audioSelectField = document.getElementById("audioSelect");
						audioFileName = 'audio/' + audioSelectField.options[audioSelectField.selectedIndex].value;
						

						password.disabled = false;
						start.disabled = true;
						stop.disabled = false;
						
					};


					/* stop buttion click event listener */
					stop.onclick = function() {
						console.log('stop clicked');
						stopServices();
						
				    };
					
					document.querySelector('#password').addEventListener('keypress', function (e) {
					    var key = e.which || e.keyCode;
					    if (key === 13) { // 13 is enter
					    		console.log('enter key pressed');
					    		stopServices();
					    	}
						});

				    function stopServices(){
				    	sendMessageToDevice("stop");

				    	document.querySelector('#password').value = "";
						stopAudioRecording();
						pauseAudio();
						stopTimeSync();
						removeEventsHandler(eventsList);
						stop.disabled = true;
						start.disabled = false;
						password.disabled = true;
						console.log('stop clicked');
						stopTime();
				    }

				    function playAudio(){
				    	var audioFileName = 'audio/' + audioSelectField.options[audioSelectField.selectedIndex].value + ".wav";
					
				    	//alert(audioFileName);
						var player=document.getElementById('player');
					    var sourceWav=document.getElementById('wavSrc');
					    sourceWav.src=audioFileName;
					    player.load(); //just start buffering (preload)
					   	player.play(); //start playing
						
						var audioPlayerField = 'AUDIO_PLAYER';
						//alert(getFieldID(audioPlayerField));
						
						showMessage(audioPlayerField, "Audio Player: STARTED");
										    	
					}

					function pauseAudio(){
						document.getElementById('player').pause();
						var audioPlayerField = 'AUDIO_PLAYER';
						showMessage(audioPlayerField, "Audio Player: STOPPED");
						
					}

// showing clock in html page
					var startSeconds;
					var isStart = false;
					var timeDisplay;
					function startTime() {
						//console.log('start time');
						var today = new Date();
					    var h = today.getHours();
					    var m = today.getMinutes();
					    var s = today.getSeconds();
					    m = checkTime(m);
					    s = checkTime(s);
					    if(isStart){
					    	startSeconds = s;
					    	isStart = false;
					    }
					    document.getElementById('time').innerHTML = s - startSeconds;
					    timeDisplay = setTimeout(startTime, 500);
					}
					function stopTime(){
						isStart = false;
						clearInterval(timeDisplay);
						document.getElementById('time').innerHTML = '0';
					}
					function checkTime(i) {
					    if (i < 10) {i = "0" + i};  // add zero in front of numbers < 10
					    return i;
					}
			});  
		</script>

	</body>

</html> 
