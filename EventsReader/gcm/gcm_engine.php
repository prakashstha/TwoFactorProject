<?php

define("GOOGLE_API_KEY", "AIzaSyD2sYeANh3Hqd8kNYu0oHK6i1kEqmOgFi8");
define("GOOGLE_GCM_URL", "https://android.googleapis.com/gcm/send");
$regist_id = "APA91bHtw07VtYB3N99CgMKBQLmpNooGnrnovqdNnIRnYyyC8BdJuI16mOMRsDFZI_mQovQM72aj0tgB4iH8VQyn8eQXzBU8Ocka4of-kxZPVBiw2Y_-tIaUplE57dnMd68NK6gI9fWoX8q4wHBg_HBwtkrnu9sj0w";
//echo $reg_id;
//send_gcm_notify($reg_id, $msg);
echo "inside gcm_engine";

if(isset($_POST['message'])){
	$msg = $_POST['message'];
	send_gcm_notify($regist_id, $msg);
	//sendMessageToRegisteredDevices($msg);
}
else{
	echo "Message not set";
}

function send_gcm_notify($reg_id, $message) {
 
    $fields = array(
		'registration_ids'  => array( $reg_id ),
		'data'              => array( "message" => $message ),
	);
				
	$headers = array(
		'Authorization: key=' . GOOGLE_API_KEY,
		'Content-Type: application/json'
	);
	
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, GOOGLE_GCM_URL);
	curl_setopt($ch, CURLOPT_POST, true);
	curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
	curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

	$result = curl_exec($ch);
	if ($result === FALSE) {
		die('Problem occurred: ' . curl_error($ch));
	}

	curl_close($ch);
	echo $result;
 }






?>