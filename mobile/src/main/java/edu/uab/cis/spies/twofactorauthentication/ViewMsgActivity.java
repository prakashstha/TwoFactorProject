package edu.uab.cis.spies.twofactorauthentication;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.TextView;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class ViewMsgActivity extends Activity{
    private TextView txt_msg;
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_viewmsg);
        txt_msg = (TextView)findViewById(R.id.txt_msg);
    }
}
