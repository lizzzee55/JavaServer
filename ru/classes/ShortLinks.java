package ru.classes;

import ru.objects.RequestObject;

import java.util.Timer;
import java.util.TimerTask;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aferon on 04.09.2015.
 */
public class ShortLinks {
    private static ShortLinks _instance;

    public static ShortLinks getInstance()
    {

        if (_instance == null) {
            _instance = new ShortLinks();
            System.out.println("ShortLinks instance");
        }
        return _instance;
    }

    public static Map<String, String>links = new HashMap<String, String>();

    public void loadMysql() throws Exception {
        String query = "select * from shortlinks";
        ResultSet rs = Mysql.getInstance().row(query);
        while (rs.next()) {
            String key = rs.getString(2);
            String url = rs.getString(3);
            this.links.put(key,url);
        }
    }
}
