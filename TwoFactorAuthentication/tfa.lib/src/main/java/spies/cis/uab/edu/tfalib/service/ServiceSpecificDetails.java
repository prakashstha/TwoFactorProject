package spies.cis.uab.edu.tfalib.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import spies.cis.uab.edu.tfalib.common.Constants;


/**
 * Created by prakashs on 7/25/2015.
 */
public class ServiceSpecificDetails {
    private static final String LOG_TAG = ServiceSpecificDetails.class
            .getSimpleName();

    private TextView sInfoText = null;
    private Intent sIntent = null;
    private Messenger sReqMessenger = null;
    private Messenger sResMessenger = null;
    private ServiceState sState = null;

    public ServiceSpecificDetails(TextView infoText, Intent intent) {
        super();
        this.sInfoText = infoText;
        this.sIntent = intent;
        this.sState = ServiceState.NOT_CONNECTED;
        setInfoText(sState.name());
    }
    public void setInfoText(String infoText) {
        sInfoText.setText(infoText);
    }

    public Intent getIntent() {
        return sIntent;
    }

    public void setIntent(Intent sIntent) {
        this.sIntent = sIntent;
    }

    public Messenger getReqMessenger() {
        return sReqMessenger;
    }

    public void setReqMessenger(Messenger sReqMessenger) {
        this.sReqMessenger = sReqMessenger;
    }

    public Messenger getResMessenger() {
        return sResMessenger;
    }

    public void setResMessenger(Messenger sResMessenger) {
        this.sResMessenger = sResMessenger;
    }

    public ServiceState getState() {
        return sState;
    }

    public void setState(ServiceState sState) {
        this.sState = sState;
        setInfoText(this.sState.name());
    }
    private void sendMessage(String msg) {
        sendMessage(ServiceMessage.INFO, Constants.INFO_TEXT_KEY, msg);
    }

    public boolean sendMessage(ServiceMessage what) {
        return sendMessage(what, Constants.NOTHING, "");
    }

    public boolean sendMessage(ServiceMessage what, String key, String extra) {
        if (sResMessenger == null || sReqMessenger == null) {
            return false;
        }
        Message msg = Message.obtain(null, what.ordinal());
        Bundle msgBundle = new Bundle();
        msgBundle.putString(key, extra);
        msg.setData(msgBundle);
        msg.replyTo = sReqMessenger;
        try {
            sResMessenger.send(msg);
            return true;
        } catch (RemoteException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
            return false;
        }
    }

}
