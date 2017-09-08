package com.github.rafaelsouzaf.monitorplus.controllers.actions;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rafaelsouzaf on 8/24/2017.
 */

public class ActionBean {

    private String title;
    private String description;
    private String requeriments;
    private String icon;
    private String backgroundColor;
    private boolean enableNotification;
    private String jsNotificationParameter;
    private String commandShell;
    private String jsFunctionFilter;
    private int ttl;

    public ActionBean() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequeriments() {
        return requeriments;
    }

    public void setRequeriments(String requeriments) {
        this.requeriments = requeriments;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isEnableNotification() {
        return enableNotification;
    }

    public void setEnableNotification(boolean enableNotification) {
        this.enableNotification = enableNotification;
    }

    public String getJsNotificationParameter() {
        return jsNotificationParameter;
    }

    public void setJsNotificationParameter(String jsNotificationParameter) {
        this.jsNotificationParameter = jsNotificationParameter;
    }

    public String getCommandShell() {
        return commandShell;
    }

    public void setCommandShell(String commandShell) {
        this.commandShell = commandShell;
    }

    public String getJsFunctionFilter() {
        return jsFunctionFilter;
    }

    public void setJsFunctionFilter(String jsFunctionFilter) {
        this.jsFunctionFilter = jsFunctionFilter;
    }

    public int getTtl() {
        // the minimum site should be 1
        if (ttl < 1) {
            ttl = 1;
        }
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public void parseJson(JSONObject obj) throws JSONException {
        this.setTitle(obj.get("title").toString());
        this.setDescription(obj.get("description").toString());
        this.setRequeriments(obj.get("requirements").toString());
        this.setIcon(obj.get("icon").toString());
        this.setBackgroundColor(obj.get("background-color").toString());
        this.setEnableNotification((boolean) obj.get("enable-notification"));
        this.setJsNotificationParameter(obj.get("js-notification-parameter").toString());
        this.setCommandShell(obj.get("command-shell").toString());
        this.setTtl((int) obj.get("ttl"));
        this.setJsFunctionFilter(obj.get("js-function-filter").toString());
    }

}
