package com.sonelli.juicessh.performancemonitor.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.sonelli.juicessh.performancemonitor.controllers.actions.*;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rafaelsouzaf on 8/24/2017.
 */

public class Util {

    public static List<ActionBean> getJsonActions(Context context) {

        List<ActionBean> list = new ArrayList<ActionBean>();

        try {
            String[] listActions = context.getAssets().list("actions");
            for (String fileName: listActions) {
                if (!fileName.endsWith(".json")) {
                    continue;
                }

                String filePath = "actions/" + fileName;
                String json = Util.readJSONFromAsset(context, filePath);
                if (json == null) {
                    continue;
                }

                try {
                    ActionBean action = new ActionBean();
                    action.parseJson(new JSONObject(json));
                    list.add(action);
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;

    }

    private static String readJSONFromAsset(Context context, String filePath) {
        String json = "";
        try {
            InputStream is = context.getAssets().open(filePath);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

}
