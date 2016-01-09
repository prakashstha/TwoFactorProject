	  
	 
	  var event_time = 0;
	  var event_type, eType, code, keyCodeValue, sync_event_time;
	  var f_id, field_ID=0;
	  var dataToSend;
	   
	  /*events file that store events information*/
	  var inputEventsFile;
	  
	  /* field to show the events recording info */
      var eventsField = 'EVENTS';
	  /* Messages for UI fields*/
      var EventsRecordingStartMessage = "Events Recording STARTED....";
      var EventsRecordingStopMessage = "Events Recording STOPPED....";
	  var EventsNotRecordedMessage = "Events is NOT RECORDED...";
	  showMessage(eventsField, EventsNotRecordedMessage);
	  
	  /*************************************
	   *	EVENTS ID					   *
	   *************************************/
	  var eventID = {
		KEYDOWN: 1,
		KEYUP:2,
		MOUSEDOWN:4,
		MOUSEUP:5,
		MOUSEMOVE:7,
		SCROLLDOWN:9,
		SCROLLUP:10
	  };
	  
	 /**********************************************
	  *		FIELD IN WHICH EVENT IS TRIGGERED	   *
	  **********************************************/
	 var fieldID = {
		  USERNAME:1,
		  PASSWORD:2,
		  OTHER:3
	  };
	   

    //  $("body").on("keyup keydown mousedown mouseup mousemove",function(event){ 
	function eventsHandler(event){
		   showMessage(eventsField, 'Initiallizing...');
		   event_time = (new Date()).getTime();
		   eType = event.type;
		   f_id = event.target.id;
		   //keyCodeValue = '';
		   if(eType == 'keyup' || eType == 'keydown'){
		   		//code = 
		   		keyCodeValue = event.which || event.keyCode;
				 //document.getElementById("div3").innerHTML = keyCodeValue + ": "+ String.fromCharCode(keyCodeValue);
		   }
		 
		  //$("#div1").html("Time Diff: " + getAvgTimeDiff() + ":  "+ event_time+"<br/>");

		  /* ====  Assigning event id ===== */
		  if(eType == 'keydown'){
			  event_type = eventID.KEYDOWN;
		  }
		  else if(eType == 'keyup'){
			  event_type = eventID.KEYUP;
	      }
		  else if(eType == 'mousedown'){
			  event_type = eventID.MOUSEDOWN;
		  }
		  else if(eType == 'mouseup') {
			  event_type = eventID.MOUSEUP;
		  }
		  else if(eType == 'mousemove'){
			  event_type = eventID.MOUSEMOVE;
		   }
		  
		  /* ====  Assigning field id ===*/
		  if(f_id == 'username'){
			  field_ID = fieldID.USERNAME;
			  //$("#div1").html('keydown:' + event_type);
		  }
		  else if(f_id == 'password'){
			  field_ID = fieldID.PASSWORD;
			  //$("#div1").html('keyup' + event_type);
		  }
		  else{
			  field_ID = fieldID.OTHER;
			  //$("#div1").html('mousedown' + event_type);
		  }
		  if(eType == 'keyup' || eType == 'keydown'){
		  	dataToSend = {
						action	: 'input_event',
						filename : inputEventsFile,
						eventType : event_type,
						eventTime : event_time,
						//syncTime : sync_event_time,
						fieldIDent   : field_ID,
						keyCode : keyCodeValue
		  	};

		  }else{
		  	dataToSend = {
						action	: 'input_event',
						filename : inputEventsFile,
						eventType : event_type,
						eventTime : event_time,
						//syncTime : sync_event_time,
						fieldIDent   : field_ID
		  	};
		  }

		 
		   /*===	 sending to EventsWriter.php to write into files	 ===*/
		   //var dataString = event.type;
		   $.ajax({
				type: "POST",
				url: "eventsWriter.php",
				data: dataToSend,
				success: function() {
					//$('#div2').html("? Logged!" + event.target.id);
				}
		  });//end of .ajax({})
    //});//end of on() function
	}//end of function
	/*===	Registering mouse wheel events	 ===*/
	$(window).bind('DOMMouseScroll', function(event){
		event_time = (new Date()).getTime();
		//sync_event_time = event_time - getAvgTimeDiff();
		eType = event.type;
		f_id = event.target.id;
		if (event.originalEvent.wheelDelta > 0 || event.originalEvent.detail < 0) {
			// scroll up
			event_type = eventID.SCROLLUP;
			//$("#div1").html("Event up: " + eType + ":  "+ event_time+"<br/>"+f_id+"<br/>");
		}
		else {
			// scroll down
			event_type = eventID.SCROLLDOWN;
			//$("#div1").html("Event down: " + eType + ":  "+ event_time+"<br/>"+f_id+"<br/>");
		}
		
		/* ====  Assigning field id ===*/
		  if(f_id == 'username'){
			  field_ID = fieldID.USERNAME;
			  //$("#div1").html('keydown:' + event_type);
		  }
		  else if(f_id == 'password'){
			  field_ID = fieldID.PASSWORD;
			  //$("#div1").html('keyup' + event_type);
		  }
		  else{
			  field_ID = fieldID.OTHER;
			  //$("#div1").html('mousedown' + event_type);
		  }
		
		/*sending to EventsWriter.php to write into files*/
	   //var dataString = event.type;
	   $.ajax({
			type: "POST",
			url: "eventsWriter.php",
			data: {
					action	: 'input_event',
					filename : inputEventsFile,
					eventType : event_type,
					eventTime : event_time,
					//syncTime : sync_event_time
				},
			  
			  success: function() {
				//$('#div2').html("? Logged!" + event.target.id)
			}
	  });//end of .ajax({})
	});//end of DOMMouseScroll
	
	/**
	* Adding supplied events handler
	*/
	function addEventsHandler(bEventsFile, param_events){
		var i;
		inputEventsFile = bEventsFile;
		for(i = 0; i < param_events.length; i++){
			console.log("Events " + i + ": " + param_events[i]);
			document.addEventListener(param_events[i], eventsHandler);	
			showMessage(eventsField, param_events[i] + ' event handler added...');
		}
		showMessage(eventsField, EventsRecordingStartMessage);
		/*document.addEventListener("keyup", eventsHandler);	
		document.addEventListener("keydown", eventsHandler);	
		document.addEventListener("mousedown", eventsHandler);	
		document.addEventListener("mouseup", eventsHandler);	
		document.addEventListener("mousemove", eventsHandler);	*/
	}	
	
	/**
	* 	Removing the events handler 
	*/
	function removeEventsHandler(param_events){
		showMessage(eventsField, 'Removing Events Handler...');
		var i;
		for(i = 0; i < param_events.length; i++){
			document.removeEventListener(param_events[i], eventsHandler);	
			showMessage(eventsField, 'Removing ' + param_events[i] + ' event handler...');    
		}
		showMessage(eventsField, EventsRecordingStopMessage);   
	}
	
   /* method to show message in the eventsreader message field in UI */
   /*function showMessage(message){
	eventsField.innerHTML = message;
   }*/