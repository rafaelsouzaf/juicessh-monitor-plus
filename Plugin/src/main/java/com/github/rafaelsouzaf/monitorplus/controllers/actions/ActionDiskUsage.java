package com.github.rafaelsouzaf.monitorplus.controllers.actions;

/**
 * Created by rafaelsouzaf on 8/24/2017.
 */

public class ActionDiskUsage implements Action {

    @Override
    public String setTitle() {
        return "Disk usage";
    }

    @Override
    public String setDescription() {
        return null;
    }

    @Override
    public String setRequeriments() {
        return null;
    }

    @Override
    public String setIcon() {
        return null;
    }

    @Override
    public String setBackgroundColor() {
        return "#ccc";
    }

    @Override
    public boolean setEnableNotification() {
        return false;
    }

    @Override
    public String setJsNotificationParameter() {
        return null;
    }

    @Override
    public String setCommandShell() {
        return "df -h";
    }

    @Override
    public String setJsFunctionFilter() {
        return null;
    }

    @Override
    public int setTTL() {
        return 0;
    }

}
