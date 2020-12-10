package ru.classes;

import ru.objects.RequestObject;

import java.io.IOException;
import java.lang.Throwable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;

/**
 * Created by Aferon on 04.09.2015.
 */
public class Analize {
    private static Analize _instance;

    public static Analize getInstance()
    {

        if (_instance == null) {
            _instance = new Analize();
            System.out.println("Analize instance");

        }
        return _instance;
    }




    public String getHostname(String URL) throws Exception {
        String Hostname = null;
        try {

            Pattern pattern = Pattern.compile("https?://([^/]+)");
            Matcher matcher = pattern.matcher(URL);
            if(matcher.find()){
                Hostname = matcher.group(1);
            }

        } catch (Throwable e) {
            //System.out.println("regexp"+e.toString());
        }

        //System.out.println("regexp"+Hostname);
        /*System.out.println("regexp"+matcher.matches());
            if (matcher.matches()) {

            }*/

        return Hostname;

        /*if(URL == null) {
            return null;
        }
        int index = URL.indexOf("https");
        if(index >=0) {
            URL = URL.substring(8, URL.length());
        }

        index = URL.indexOf("http");
        if(index >=0) {
            URL = URL.substring(7, URL.length());
        }

        index = URL.indexOf('/');
        if(index >=0) {
            URL = URL.substring(0,index);
        }
        return URL;*/
    }

    public String getOS(String UserAgent) throws Exception {
        String os = "undefined";

        if(UserAgent != null) {
            if (UserAgent.indexOf("Windows") >= 0) { os = "win"; }
            if (UserAgent.indexOf("Linux") >= 0) { os = "linux"; }
            if (UserAgent.indexOf("Mac") >= 0) { os = "mac"; }
            if (UserAgent.indexOf("FreeBSD") >= 0) {
                os = "freebsd";
            }
        }
        return os;
    }


    public String request(String UserAgent, String Referer, String IP, String Lang, String Indent, String host, String uid, String ChromeId) throws Exception {
        Analize.getInstance().cntAll++;


        //Referer = "http://ru.aliexpress.com/ru_home.htm";
        //System.out.println("IP: "+IP);

        String Hostname = getHostname(Referer);

        if(Referer == null && host == null) {
            Hostname = "clicks.miracal.ru";
        }

        if(Hostname == null && host != null) {
            Hostname = host;
        }
        //System.out.println("Host: "+Hostname);

        if(this.lock == 1) {
            return Hostname;
        }

        //System.out.println("Hostname: "+Hostname);
        RequestObject req = (RequestObject)AnalizeStore.getInstance().getUserData(uid);
        if(req == null) {
            req = new RequestObject();
        }

		String updateTime = System.currentTimeMillis() / 1000L+"";
        //System.out.println("req: "+req.IP);

        req.IP = IP;
        req.HOST = Hostname;
        req.OS = this.getOS(UserAgent);
        req.Lang = Lang;
        req.Time = updateTime;
        req.Indent = Indent;
        req.Uid = uid;
        req.SitesOpened++;
        req.ChromeId = ChromeId;
        //System.out.println("req: "+req.SitesOpened);

       // System.out.println("req: "+req.SitesOpened);

        AnalizeStore.getInstance().userData(req);


        return Hostname;
    }

    public int lock = 0;

    public void dump() throws Exception {
        this.lock = 1;
        AnalizeStore.getInstance().saveToDB();
        this.lock = 0;

    }

    public String getSiteJson() throws Exception {
        this.dump();

        return "Force save to DATABASE";//result;
    }

    public Timer tm = new Timer();
    public void saveData() {
        this.tm.schedule(new RemindTask(), 60*1000); //swith to 180

    }

    public int cntAll = 0;

    public Timer tm2 = new Timer();
    public void statistic() {
        this.tm2.schedule(new TickRequest(), 10*1000);
    }

    class TickRequest extends TimerTask {
        public void run() {
            System.err.println ("Queries/10 = "+ Analize.getInstance().cntAll+";");
            Analize.getInstance().cntAll = 0;
            Analize.getInstance().statistic();
        }
    }

    class RemindTask extends TimerTask {
        public void run() {
            //System.err.println ("Start saving data");
            try {
                Analize.getInstance().dump();
            } catch (Throwable t) {
                /*do nothing*/
            }
            Analize.getInstance().saveData();
        }
    }

}
