package com.github.rafaelsouzaf.monitorplus.controllers.actions;

/**
 * Created by rafaelsouzaf on 8/24/2017.
 */

public interface Action {

    /**
     * Set title of the block
     * @return String
     */
    public String setTitle();
    public String setDescription();
    public String setRequeriments();
    public String setIcon();
    public String setBackgroundColor();
    public boolean setEnableNotification();
    public String setJsNotificationParameter();
    public String setCommandShell();
    public String setJsFunctionFilter();
    public int setTTL();

}
