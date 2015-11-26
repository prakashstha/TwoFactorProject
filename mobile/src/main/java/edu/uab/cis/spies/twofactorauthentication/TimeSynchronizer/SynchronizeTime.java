package edu.uab.cis.spies.twofactorauthentication.TimeSynchronizer;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.uab.cis.spies.twofactorauthentication.WebServer.AsyncResponse;
import edu.uab.cis.spies.twofactorauthentication.WebServer.HttpServiceHandle;

/**
 * Created by prakashs on 7/23/2015.
 */
public class SynchronizeTime extends AsyncTask<String, Void, String> {
    private String LOG_TAG = SynchronizeTime.class.getSimpleName();
    private String URL = "http://students.cis.uab.edu/prakashs/EventsReader/calcTimeOffset.php";
    private long T0, T1, T2, RTT, offSet;
    private String request_time, response_time,server_time;
    private StringBuilder result;
    public AsyncResponse delegate = null;

    /*
    * T0 -> Request Time
    * T1 -> Server Time
    * T2 -> Response Time
    * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(String... arg) {
        // TODO Auto-generated method stub
        T0 = System.currentTimeMillis();
        request_time = String.valueOf(T0);
        Log.d(LOG_TAG, "inside do IN background=> time :" + request_time);
        StringBuilder sb = new StringBuilder();
        // Preparing post params
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("request_time", request_time));

        HttpServiceHandle serviceClient = new HttpServiceHandle();

        String json = serviceClient.makeServiceCall(URL,
                HttpServiceHandle.POST, params);

        Log.d("Create  ", "> " + json);

        if (json != null) {
            try {

                T2 = System.currentTimeMillis();
                response_time = String.valueOf(T2);
                JSONObject jsonObj = new JSONObject(json);
                server_time = jsonObj.getString("server_time");
                request_time = jsonObj.getString("request_time");
                T0 = Long.valueOf(request_time);
                T1 = Long.valueOf(server_time);
                RTT = T2-T0;
                offSet = T1-RTT/2-T0;
                sb.append("Request Time : " + request_time + " ms\n");
                sb.append("Server Time " + server_time+ " ms\n");
                sb.append("Response Time : " + response_time+ " ms\n");
                sb.append("RTT : " + RTT+ " ms\n");
                sb.append("Offset :"+ offSet+ " ms");
                Log.e("Request Time : ", request_time + " ms");
                Log.e("Server Time ", server_time+ " ms");
                Log.e("Response Time : ",response_time+ " ms");
                Log.e("RTT : ",RTT+ " ms");
                Log.e("Offset :",offSet+ " ms");
                result = sb;
                return sb.toString();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Log.e("JSON Data", "JSON data error!");
        }
        return "Errorc";
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.handleResponse(result);
    }
}
