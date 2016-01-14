function sendMessageToDevice(msg){
	dataToSend = {
			message	: msg
	};
	
	$.ajax({
		type: "POST",
		url: "./gcm/gcm_engine.php",
		data: dataToSend,
		success: function() {
			//$('#div2').html("Send " + msg +"  to device");
			console.log("Send " + msg +"  to device");
		}
	});//end of .ajax({})
	
}