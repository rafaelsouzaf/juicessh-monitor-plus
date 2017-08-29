package com.sonelli.juicessh.performancemonitor.activities;

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.evgenii.jsevaluator.JsEvaluator;
import com.evgenii.jsevaluator.interfaces.JsCallback;
import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.performancemonitor.adapters.ConnectionSpinnerAdapter;
import com.sonelli.juicessh.performancemonitor.controllers.BaseController;
import com.sonelli.juicessh.performancemonitor.controllers.DiskUsageController;
import com.sonelli.juicessh.performancemonitor.controllers.JsonDynamicallyController;
import com.sonelli.juicessh.performancemonitor.loaders.ConnectionListLoader;
import com.sonelli.juicessh.performancemonitor.util.Util;
import com.sonelli.juicessh.performancemonitor.views.AutoResizeTextView;
import com.sonelli.juicessh.pluginlibrary.PluginClient;
import com.sonelli.juicessh.pluginlibrary.PluginContract;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnClientStartedListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionFinishedListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionStartedListener;
import com.sonelli.juicessh.performancemonitor.controllers.actions.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;

public class MainActivity extends AppCompatActivity implements ActionBar.OnNavigationListener, OnSessionStartedListener, OnSessionFinishedListener {

    public static final String TAG = "MainActivity";

    private boolean isClientStarted = false;
    private final PluginClient client = new PluginClient();
    private final static int JUICESSH_REQUEST_CODE = 2585;

    private static final int REQUESTID_PERMISSIONS = 1388;
    private final static String PERMISSION_READ_CONNECTIONS = "com.sonelli.juicessh.api.v1.permission.READ_CONNECTIONS";
    private final static String PERMISSION_OPEN_SESSIONS = "com.sonelli.juicessh.api.v1.permission.OPEN_SESSIONS";

    private Button connectButton;
    private Button disconnectButton;

    private ConnectionSpinnerAdapter spinnerAdapter;

    // Controllers
    private BaseController loadAverageController;
    private BaseController freeRamController;
    private BaseController cpuUsageController;
    private BaseController diskUsageController;
    private BaseController networkUsageController;

    // Text displays
    private AutoResizeTextView loadAverageTextView;
    private AutoResizeTextView freeRamTextView;
    private AutoResizeTextView cpuUsageTextView;
    private AutoResizeTextView networkUsageTextView;
    private AutoResizeTextView diskUsageTextView;

    // State
    private volatile int sessionId;
    private volatile String sessionKey;
    private volatile boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_new);
//        super.onCreate(savedInstanceState);
//        setContentView(mainLayout);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        LinearLayout viewById = (LinearLayout) findViewById(R.id.main_new);
        AutoResizeTextView testBox2 = (AutoResizeTextView) findViewById(R.id.free_memory);
        if (testBox2 == null) {
            System.out.println("========================================= PORRA testBox2 ES NULL");
            //return;
        }

//        viewById.addView(et);


        /**
         * Set click listener for the ADD button
         */
        View addBox = findViewById(R.id.add_box);
        addBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("TESTING CLICK AQUI");
            }
        });


        //LinearLayout box = (LinearLayout) findViewById(R.id.test_box);
        //mainLayout.addView(box);

        // Create an adapter for populating the actionbar spinner with connections.
        // We're going to pass in TYPE_SSH to disable all spinner items not of this type.
        // This is because sending of commands required to poll performance data is only
        // possible on SSH connections.
        this.spinnerAdapter = new ConnectionSpinnerAdapter(this, PluginContract.Connections.TYPE_SSH);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, this);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        this.loadAverageTextView = (AutoResizeTextView) findViewById(R.id.load_average);
        this.freeRamTextView = (AutoResizeTextView) findViewById(R.id.free_memory);
        this.cpuUsageTextView = (AutoResizeTextView) findViewById(R.id.cpu_usage);
        this.networkUsageTextView = (AutoResizeTextView) findViewById(R.id.network_usage);
        this.diskUsageTextView = (AutoResizeTextView) findViewById(R.id.disk_usage);

        this.connectButton = (Button) findViewById(R.id.connect_button);
        Drawable drawable = getDrawable(R.drawable.login);
        if (drawable != null) {
            drawable.setBounds(0, 0, (int)(drawable.getIntrinsicWidth()*0.2),
                    (int)(drawable.getIntrinsicHeight()*0.2));
        }
        connectButton.setCompoundDrawables(drawable, null, null, null);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final UUID id = spinnerAdapter.getConnectionId(getSupportActionBar().getSelectedNavigationIndex());
                if(id != null){
                    if(isClientStarted){
                        connectButton.setText(R.string.connecting);
                        connectButton.setEnabled(false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    client.connect(MainActivity.this, id, MainActivity.this, JUICESSH_REQUEST_CODE);
                                } catch (ServiceNotConnectedException e){
                                    Toast.makeText(MainActivity.this, "Could not connect to JuiceSSH Plugin Service", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).start();
                    }
                }

            }
        });

        this.disconnectButton = (Button) findViewById(R.id.disconnect_button);
        Drawable drawable1 = getDrawable(R.drawable.logout);
        if (drawable1 != null) {
            drawable1.setBounds(0, 0, (int)(drawable1.getIntrinsicWidth()*0.2),
                    (int)(drawable1.getIntrinsicHeight()*0.2));
        }
        disconnectButton.setCompoundDrawables(drawable1, null, null, null);
        this.disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sessionId > -1 && sessionKey != null){
                    if(isClientStarted){
                        disconnectButton.setText(R.string.disconnecting);
                        disconnectButton.setEnabled(false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    client.disconnect(sessionId, sessionKey);
                                } catch (ServiceNotConnectedException e){
                                    Toast.makeText(MainActivity.this, "Could not connect to JuiceSSH Plugin Service", Toast.LENGTH_SHORT).show();
                                }
                                disconnectButton.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        disconnectButton.setEnabled(true);
                                        disconnectButton.setText(R.string.disconnect);
                                    }
                                });
                            }
                        }).start();
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(loadAverageTextView != null)
            loadAverageTextView.resizeText();

        try {

            if (ContextCompat.checkSelfPermission(this, PERMISSION_READ_CONNECTIONS) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_READ_CONNECTIONS)) {
                    Toast.makeText(this, R.string.plugin_permissions_request, Toast.LENGTH_LONG).show();
                }

                ActivityCompat.requestPermissions(this, new String[]{PERMISSION_READ_CONNECTIONS, PERMISSION_OPEN_SESSIONS}, REQUESTID_PERMISSIONS);

            } else {

                // Use a Loader to load the connection list into the adapter from the JuiceSSH content provider
                // This keeps DB activity async and off the UI thread to prevent the plugin lagging
                getSupportLoaderManager().initLoader(0, null, new ConnectionListLoader(this, spinnerAdapter));

                if(!isClientStarted) {
                    startPluginClient();
                }

            }

        } catch (IllegalArgumentException e){
            Log.e(TAG, "JuiceSSH is not installed. Plugin Library will prompt user to install from the Play Store.");
        }

        if(this.isConnected){
            connectButton.setVisibility(View.GONE);
            disconnectButton.setVisibility(View.VISIBLE);
        } else {
            connectButton.setVisibility(View.VISIBLE);
            disconnectButton.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isClientStarted) {
            if (isConnected){
                try {
                    client.disconnect(sessionId, sessionKey);
                } catch (ServiceNotConnectedException e) {
                    Log.e(TAG, "Failed to disconnect JuiceSSH session used performance monitor plugin");
                }
             }
            client.stop(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // This is important if you want to be able to interact with JuiceSSH sessions that you
        // have started otherwise the plugin won't have access.
        if(requestCode == JUICESSH_REQUEST_CODE){
            client.gotActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSessionStarted(final int sessionId, final String sessionKey) {

        MainActivity.this.sessionId = sessionId;
        MainActivity.this.sessionKey = sessionKey;
        MainActivity.this.isConnected = true;

        connectButton.setText(R.string.connect);
        connectButton.setVisibility(View.GONE);
        connectButton.setEnabled(false);

        disconnectButton.setVisibility(View.VISIBLE);
        disconnectButton.setEnabled(true);

        // Register a listener for session finish events so that we know when the session has been disconnected
        try {
            client.addSessionFinishedListener(sessionId, sessionKey, this);
        } catch (ServiceNotConnectedException ignored){}

        /**
         * Read and start/execute all json files
         */
        List<ActionBean> jsonActions = Util.getJsonActions(this);
        for (ActionBean action: jsonActions) {

            new JsonDynamicallyController(this, action)
                .setSessionId(sessionId)
                .setSessionKey(sessionKey)
                .setPluginClient(client)
                .setTextview(diskUsageTextView)
                .start();

        }

    }

    @Override
    public void onSessionCancelled() {
        // The user cancelled our JuiceSSH connection before it finished
        // connecting or failed authentication.
    }

    @Override
    public void onSessionFinished() {

        MainActivity.this.sessionId = -1;
        MainActivity.this.sessionKey = null;
        MainActivity.this.isConnected = false;

        if(loadAverageController != null){
            loadAverageController.stop();
        }

        if(freeRamController != null){
            freeRamController.stop();
        }

        if(cpuUsageController != null){
            cpuUsageController.stop();
        }

        if(diskUsageController != null){
            diskUsageController.stop();
        }

        if(networkUsageController != null){
            networkUsageController.stop();
        }

        loadAverageTextView.setText("-");
        freeRamTextView.setText("-");
        cpuUsageTextView.setText("-");
        networkUsageTextView.setText("-");
        diskUsageTextView.setText("-");

        disconnectButton.setVisibility(View.GONE);
        disconnectButton.setEnabled(false);

        connectButton.setVisibility(View.VISIBLE);
        connectButton.setText(R.string.connect);
        connectButton.setEnabled(true);

    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch(item.getItemId()){

            case R.id.fork_on_github:
                Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_url)));
                urlIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(Intent.createChooser(urlIntent, getString(R.string.open_address)));
                return true;

            case R.id.rate_plugin:
                String packageName = getResources().getString(R.string.app_package);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e){
                    Toast.makeText(this, getString(R.string.google_play_not_installed), Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;

            case R.id.test_not:
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.cpu)
                                .setContentTitle("My notification")
                                .setContentText("Hello World!");

                // Sets an ID for the notification
                int mNotificationId = 1;
                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                // Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());

                return true;

        }

        return false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUESTID_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 1  && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    getSupportLoaderManager().initLoader(0, null, new ConnectionListLoader(this, spinnerAdapter));
                    if(!isClientStarted) {
                        startPluginClient();
                    }

                } else {
                    if(Build.VERSION.SDK_INT < 23){
                        // We haven't been granted permission, but we're running on a version of Android
                        // that doesn't support dynamic permissions requests. This can only happen if the
                        // plugin was installed before JuiceSSH (so can't find the permissions).
                        // Start the PluginClient - not because it will be able to connect, but it will
                        // do a permission check, and prompt the user with a dialog to warn about the
                        // installation order, and guide them to a fix.
                        startPluginClient();
                    }
                }
            }
        }

    }

    public void startPluginClient(){

        client.start(this, new OnClientStartedListener() {
            @Override
            public void onClientStarted() {
                isClientStarted = true;
                connectButton.setText(R.string.connect);
                connectButton.setEnabled(true);
            }

            @Override
            public void onClientStopped() {
                isClientStarted = false;
                connectButton.setEnabled(false);
            }
        });

    }

}
