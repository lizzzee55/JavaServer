package ru.classes;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Throwable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ru.classes.Mysql;
import java.sql.*;

/**
 * Created by Aferon on 04.09.2015.
 */
public class isShowCookie {
    private static isShowCookie _instance;

    public static isShowCookie getInstance()
    {

        if (_instance == null) {
            _instance = new isShowCookie();
            _instance.clearPerDay();
            System.out.println("isShowCookie instance");
        }
        return _instance;
    }


    public static ConcurrentHashMap<String, HashMap<String, Integer>> ips = new ConcurrentHashMap<String, HashMap<String, Integer>>(100);
    public boolean isShowCookie(String ip, String hostname) {
        int currentTime = (int)(System.currentTimeMillis() / 1000L);

        try {

            HashMap<String, Integer> user = this.ips.get(ip);
            if (user != null) {
                Integer isShow = user.get(hostname);
                //System.out.println("Debug: " + (isShow+30)+">="+currentTime);
                if (isShow != null && (isShow+900) >= currentTime) {
                    //user.put(hostname, currentTime);
                    //System.out.println("old tile: false");
                    return false;
                } else {
                    user.put(hostname, currentTime);
                    //System.out.println("Shwo reclama : new time ok");
                    return true;
                }
            } else {

                HashMap<String, Integer> newClient = new HashMap<String, Integer>();
                newClient.put(hostname, currentTime);
                this.ips.put(ip, newClient);
                return true;
            }
        } catch (Throwable e) {
            System.out.println("Err: " + e.toString());
        }
        return true;
    }

    public void clearIps() {
        isShowCookie.getInstance().ips.clear();
    }

    public Timer tm = new Timer();
    public void clearPerDay() {
        this.tm.schedule(new RemindTask(), 4*60*60*1000);
    }

    class RemindTask extends TimerTask {
        public void run() {
            System.out.println("!!Claer cookie!!");
            isShowCookie.getInstance().clearIps();
            isShowCookie.getInstance().clearPerDay();
        }
    }
}
