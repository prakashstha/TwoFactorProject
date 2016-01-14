/**
* @param field => a field in the UI where message is to be displayed
*				it can be one of the following field:
*				1. AUDIO	2. EVENTS	3. TIME_SYNCHRONIZER	4. GCM
* @param message => message to be displayed in the field
*/
function showMessage(field, message){
	var fieldId = getFieldID(field);
	var UIField = document.getElementById(fieldId);
	UIField.innerHTML = message;
}
/**
* @param field identification of field in UI
* @return field_id corresponding to field identification
 */
function getFieldID(field){
	if(field == "AUDIO")
		return 'audioRecorder';
	else if(field == "EVENTS")
		return 'eventsReader';
	else if(field == 'TIME_SYNCHRONIZER')
		return 'timeSynchronizer';
	else if(field == 'GCM')
		return 'gcm';
}
 
