package edu.uab.cis.spies.twofactorauthentication.WebServer;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prakashs on 7/21/2015.
 */
public class PushToServer extends AsyncTask<String, Void, Void> {
    private String LOG_TAG = PushToServer.class.getSimpleName();
    private String URL; // = "http://students.cis.uab.edu/prakashs/EventsReader/mReceiveData.php";
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    public static final String GCMDeviceRegistration = "GCMDeviceRegistration";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Void doInBackground(String... arg) {
        // TODO Auto-generated method stub
        String msgKey = arg[0];
        buildParams(arg);

        HttpServiceHandle serviceClient = new HttpServiceHandle();

        String json = serviceClient.makeServiceCall(URL, HttpServiceHandle.POST, params);

        Log.d("Create  ", "> " + json);

        if (json != null) {
            handleResponse(json, msgKey);
        } else {
            Log.e(LOG_TAG, "JSON data error!");
        }
        return null;
    }

    private void handleResponse(String json, String msgKey)
    {
        try {
            JSONObject jsonObj = new JSONObject(json);
            boolean error = jsonObj.getBoolean("error");
            // checking for error node in json
            if (!error) {
                Log.d(LOG_TAG, "Success in " + msgKey + " >> " + jsonObj.getString("message"));
            } else {
                Log.e(LOG_TAG, "Error while " + msgKey + " >> " + jsonObj.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

    private void buildParams(String... arg)
    {
        String msgKey = arg[0];
        if(msgKey.equalsIgnoreCase(GCMDeviceRegistration)) {
            initParamsForGCMDeviceRegistration(arg);
        }

    }
    //init URL as well as params to push to the server
    private void initParamsForGCMDeviceRegistration(String... arg){
        String projectName = arg[1];
        String regId = arg[2];
        Log.d(LOG_TAG, "initParamsForGCMDeviceRegistration()>>Project Name: " + projectName+ "\n regId :"+regId);
        URL = "http://students.cis.uab.edu/prakashs/EventsReader/gcm/registerDevice.php";
        // Preparing post params
        params.add(new BasicNameValuePair("projectName", projectName));
        params.add(new BasicNameValuePair("regId", regId));


    }
}
