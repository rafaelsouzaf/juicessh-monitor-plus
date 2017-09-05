package com.sonelli.juicessh.performancemonitor.controllers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.performancemonitor.controllers.actions.ActionBean;
import com.sonelli.juicessh.performancemonitor.util.JavascriptEvaluator;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionExecuteListener;

/**
 * Created by rafaelsouzaf on 8/23/2017.
 */

public class JsonDynamicallyController extends BaseController {

    private ActionBean action;

    public JsonDynamicallyController(Context context, ActionBean action) {
        super(context);
        this.action = action;
    }

    @Override
    public BaseController start() {

        super.start();

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

            try {

                final StringBuilder fullResponse = new StringBuilder();

                getPluginClient().executeCommandOnSession(getSessionId(), getSessionKey(), action.getCommandShell(), new OnSessionExecuteListener() {

                    @Override
                    public void onCompleted(int exitCode) {
                        System.out.println("-----exitCode-------" + exitCode);
                        switch(exitCode){
                            case 127:
                                setText(getString(R.string.error));
                                Log.d(action.getTitle(), "Tried to run a command but the command was not found on the server");
                                break;
                            case 0:
                                System.out.println("========================" + fullResponse.toString());
                                String eval = JavascriptEvaluator.evaluate(action.getJsFunctionFilter(), fullResponse.toString());
                                setText(eval);
                                break;
                        }
                    }
                    @Override
                    public void onOutputLine(String result) {
                        if (fullResponse.toString().isEmpty()) {
                            fullResponse.append(result);
                        } else {
                            fullResponse.append(result);
                            fullResponse.append(System.getProperty("line.separator"));
                        }
                    }
                    @Override
                    public void onError(int error, String reason) {
                        toast(reason);
                    }
                });
            } catch (ServiceNotConnectedException e){
                Log.d(action.getTitle(), "Tried to execute a command but could not connect to JuiceSSH plugin service");
            }

            if(isRunning()) {
                handler.postDelayed(this, action.getTtl() * 1000L);
            }

            }

        });

        return this;

    }

}