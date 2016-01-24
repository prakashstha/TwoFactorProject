/**
* @param dir directory where all the recording files are stored.
* @timeVar timeStamp time stamp for uniqueness of filename
* @placeValue name of place where recordings are made
* @type type of file -  bTimeSync, bAudio, bAudioTime, bEvents
*/
function getFileName(dir, timeVar, placeValue, audioName, type){
  var fileName = dir + '/' + timeVar + '_' + placeValue + '_' + audioName + '_' + type;
  if(type == 'browser_audio'){
    fileName += '.wav';
  }else{
    fileName += '.csv';
  }
  return fileName;
}
 
