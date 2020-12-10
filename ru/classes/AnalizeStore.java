package ru.classes;

import java.util.*;
import java.sql.*;
import ru.objects.RequestObject;
import ru.classes.Mysql;
import java.util.concurrent.ConcurrentHashMap;

public class AnalizeStore {
    private static AnalizeStore	_instance;

    public static AnalizeStore getInstance()
    {

        if (_instance == null) {
            _instance = new AnalizeStore();
			System.out.println("ru.classes.Analize");
		}
        return _instance;
    }

    public static ConcurrentHashMap<String, RequestObject> Users = new ConcurrentHashMap<String, RequestObject>(100);
    public static ConcurrentHashMap<String, Integer> TopHost = new ConcurrentHashMap<String, Integer>(100);


    public void userData(RequestObject obj) {
        AnalizeStore.Users.put(obj.Uid, obj);
        //Integer i = (Integer)AnalizeStore.TopHost.get(obj.HOST);
        //if(i == null) i=0;

        //i++;
        //AnalizeStore.TopHost.put(obj.HOST, i);
    }

    public RequestObject getUserData(String uid) {
        return AnalizeStore.Users.get(uid);
    }

    public void saveToDB() throws Exception {
        String sql = null;
        try
        {
            Mysql.getInstance().mysqlInit();
            System.out.println("Save data");

            //String updateTime = System.currentTimeMillis() / 1000L+"";
            int sqlc = 0;

            RequestObject obj;
            Iterator<Map.Entry<String, RequestObject>> itr = AnalizeStore.Users.entrySet().iterator();
            while(itr.hasNext()) {
                Map.Entry<String,RequestObject> x = itr.next();
                obj = x.getValue();

                sql = "Insert into users (`ip`,`last_site`,`os`,`lang`,`time`,`indent`, `unicue_id`, `sites_opened`, `sites_opened_good`, `clicks`, `chrome_id`) values ('"+obj.IP+"','"+obj.HOST+"','"+obj.OS+"','"+obj.Lang+"','"+obj.Time+"','"+obj.Indent+"','"+x.getKey()+"', '"+obj.SitesOpened+"', '"+obj.SitesOpenedGood+"', '"+obj.Click+"', '"+obj.ChromeId+"') ON DUPLICATE KEY UPDATE  `last_site`='"+obj.HOST+"', `ip`='"+obj.IP+"', `os`='"+obj.OS+"', `lang`='"+obj.Lang+"', `time`='"+obj.Time+"', `indent`='"+obj.Indent+"', `sites_opened`=`sites_opened`+"+obj.SitesOpened+", `sites_opened_good`=`sites_opened_good`+"+obj.SitesOpenedGood+", `clicks`=`clicks`+"+obj.Click+", `chrome_id`='"+obj.ChromeId+"';";

                //System.out.println("SQL "+sql);

                Mysql.getInstance().query(sql);
                sqlc++;
            }

            System.out.println("Users queries "+sqlc);
            sqlc = 0;

            AnalizeStore.Users.clear();

            /* Iterator<Map.Entry<String, Integer>> itr2 = AnalizeStore.TopHost.entrySet().iterator();

            while(itr2.hasNext()) {
                Map.Entry<String,Integer> x = itr2.next();

                sql = "Insert into all_sites (`url`,`cnt`) values ('"+x.getKey()+"',`cnt`+"+x.getValue()+") ON DUPLICATE KEY UPDATE `cnt`=`cnt`+"+x.getValue()+";";
                Mysql.getInstance().query(sql);
                sqlc++;
            }

            System.out.println("Sites queries "+sqlc);
            AnalizeStore.TopHost.clear();
            */
            Mysql.getInstance().mysqlClose();
        }
        catch (Exception e)
        {
            System.err.println ("Error query"+sql);
            e.printStackTrace();
        }
    }

    public void loadUsersFromMysql() throws Exception {
        this.Users.clear();

        String query = "select * from users";
        ResultSet rs = Mysql.getInstance().row(query);
        Integer count = 0;
        while (rs.next()) {
            if(rs.getString(1).length() > 1) {
                RequestObject user = new RequestObject();
                user.Uid = rs.getString(1);
                user.IP = rs.getString(2);
                user.Lang = rs.getString(3);
                user.OS = rs.getString(6);
                //user.Time = rs.getString(7);

                user.Indent = rs.getString(8);
                user.HOST = rs.getString(10);

                user.SitesOpened = rs.getInt(11);
                user.SitesOpenedGood = rs.getInt(12);

                user.Age = rs.getInt(13);
                user.MaleFamale = rs.getInt(14);


                this.Users.put(rs.getString(1), user);
                count++;
            }


        }
        System.out.println("Loaded "+count+" bots");
    }
}