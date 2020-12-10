package ru.classes;

import java.lang.Boolean;
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
public class ipControll {
    private static ipControll _instance;

    public static ipControll getInstance()
    {

        if (_instance == null) {
            _instance = new ipControll();
            _instance.clearPerDay();
            System.out.println("IpControll instance");
        }
        return _instance;
    }

    public void loadMysql() throws Exception {
        String sql = "Select * from `scheduleTask` where `task`='ip_clear' order by id DESC limit 0,1";
        ResultSet rs = Mysql.getInstance().row(sql);
        while (rs.next()) {
            String task = rs.getString(2);
            int time = rs.getInt(3);
            this.last_update = time;
        }
    }

    public static int last_update = 0;

    public static ConcurrentHashMap<String, IpControllObject> ips = new ConcurrentHashMap<String, IpControllObject>(100);

    public boolean isShow(String ip, String hostname) {
        IpControllObject obj = this.ips.get(ip);

        if(obj != null) {
            //System.out.println("Ip finded");
            String isVisit = obj.sites.get(hostname);
            if(isVisit != null) {
                return false;
            } else {
                obj.addSite(hostname);
                return true;
            }
        } else {
            obj = new IpControllObject();
            obj.addSite(hostname);
            this.ips.put(ip, obj);
            return true;
        }
    }

    private class IpControllObject {
        public Map<String, String>sites = new HashMap<String, String>();

        public void addSite(String hostname) {
            this.sites.put(hostname, "yep");
        }
    }

    public void clearIpAll() {
        String currentTimeST = System.currentTimeMillis() / 1000L+"";
        int unix = Integer.parseInt(currentTimeST);
        if(this.last_update < unix - (60 * 60 * 21)) {
            try {
                Mysql.getInstance().mysqlInit();
                String sql = "insert into `scheduleTask` (`task`,`time`) VALUES ('ip_clear','"+unix+"')";
                Mysql.getInstance().query(sql);
                Mysql.getInstance().mysqlClose();

                this.last_update = unix;
                System.out.println("clear ipControll time: "+unix);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public Timer tm = new Timer();
    public void clearPerDay() {
        this.tm.schedule(new RemindTask(), 60*1000);
    }

    class RemindTask extends TimerTask {
        public void run() {
            ipControll.getInstance().clearIpAll();
            ipControll.getInstance().clearPerDay();
        }
    }
}
