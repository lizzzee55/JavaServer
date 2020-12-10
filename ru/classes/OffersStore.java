package ru.classes;

import java.lang.Integer;
import java.lang.Throwable;
import java.sql.*;
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import ru.objects.Offers;


public class OffersStore {
    private static OffersStore	_instance;

    public static OffersStore getInstance()
    {

        if (_instance == null) {
            _instance = new OffersStore();
			System.out.println("Load offers");
		}
        return _instance;
    }

    public static Map<String, Offers>offersList = new HashMap<String, Offers>();

    public Offers find(String hostname) {
        Iterator<Map.Entry<String, Offers>> itr = offersList.entrySet().iterator();
        Offers obj;

        while(itr.hasNext()) {
            Map.Entry<String,Offers> x = itr.next();
            obj = x.getValue();

            if(hostname.indexOf(x.getKey()) >= 0) {
                //System.out.println("Load offer "+hostname+ "key: "+x.getKey());
                return obj;
            }
        }
        return null;
    }

    public Offers findReg(String hostname) {
        Iterator<Map.Entry<String, Offers>> itr = offersList.entrySet().iterator();
        Offers obj;

        while(itr.hasNext()) {
            Map.Entry<String,Offers> x = itr.next();
            obj = x.getValue();

            if(x.getKey().indexOf(hostname) >= 0) {
                return obj;
            }
        }
        return null;
    }

    public String getJsonOffers() {
        String json = "{";
        Iterator<Map.Entry<String, Offers>> itr = offersList.entrySet().iterator();
        Offers obj;

        while(itr.hasNext()) {
            Map.Entry<String,Offers> x = itr.next();
            String serv = x.getValue().serverUrl;
            json += "\""+x.getValue().hostname+"\": {\"f\": \""+x.getValue().isFrame+"\",\"s\": \""+x.getValue().serverUrl+"\"},";
        }
        return json.substring(0, json.length()-1)+"}";
    }

    public String getJsonOffersList() {
        String json = "{";
        Iterator<Map.Entry<String, Offers>> itr = offersList.entrySet().iterator();
        Offers offer;

        while(itr.hasNext()) {
            Map.Entry<String,Offers> x = itr.next();

            offer = x.getValue();
            if(offer.status == 1) {
                json += "'" + offer.hostname + "': {redirect: '" + offer.redirect + "', status: '" + offer.status + "', url: '" + offer.serverUrl + "', frame: '" + offer.isFrame + "', deep: '" + offer.deep + "', isBlank :'" + offer.isBlank + "'},";
            }
            //String serv = x.getValue().serverUrl;
            //json += "\""+x.getValue().hostname+"\": {\"f\": \""+x.getValue().isFrame+"\",\"s\": \""+x.getValue().serverUrl+"\"},";
        }
        return json.substring(0, json.length()-1)+"}";
    }

    public void loadMysql() throws Exception {
        this.offersList.clear();

        String query = "select * from offers";
        ResultSet rs = Mysql.getInstance().row(query);
        Integer count = 0;
        while (rs.next()) {
            if(rs.getString(2).length() > 1) {
                Offers offers = new Offers();
                offers.hostname = rs.getString(2);
                offers.redirect = rs.getString(3);
                offers.status = rs.getInt(4);
                offers.serverUrl = rs.getString(5);
                offers.isFrame = rs.getInt(6);
                offers.network = rs.getString(7);
                offers.deep = rs.getString(8);
                offers.isBlank = rs.getInt(9);
                this.offersList.put(rs.getString(2), offers);
                count++;
            }


        }
        System.out.println("Loaded "+count+" offers");
    }

}