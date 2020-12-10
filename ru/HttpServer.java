package ru;

import ru.classes.Analize;
import ru.classes.ShortLinks;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import javax.net.ssl.*;

/**
 * Created by yar 09.09.2009
 */
public class HttpServer {

    public static void main(String[] args) throws Throwable {
        App.dbLoader();
        new Thread(new HTTP()).start();
        new Thread(new CROM()).start();

    }

    private static class CROM implements Runnable {
        public void run() {
            Analize.getInstance().saveData();
            Analize.getInstance().statistic();
        }
    }

    private static class HTTP implements Runnable {

        public void run() {
            try {
                System.out.println("HTTP server start");
                ServerSocket ss = new ServerSocket(8080);
                while (true) {
                    Socket s = ss.accept();
                    new Thread(new SocketProcessor(s)).start();
                }
            } catch (Throwable t) {
                /*do nothing*/
            } finally {
                try {
                } catch (Throwable t) {
                    /*do nothing*/
                }
            }
        }

        private static class SocketProcessor implements Runnable {

            private Socket s;
            private InputStream is;
            private OutputStream os;
            private String IP;

            private SocketProcessor(Socket s) throws Throwable {
                //System.out.println("accept");
                this.s = s;
                this.is = s.getInputStream();
                this.os = s.getOutputStream();
                this.IP = s.getRemoteSocketAddress().toString();

                this.IP = this.IP.substring(1,this.IP.length());
                String split[] = this.IP.split(":");
                this.IP = split[0];
            }

            public void run() {
                try {
                    App App = new App();
                    App.server(is, os, IP);
                    this.s.close();
                } catch (Throwable t) {
                /*do nothing*/
                } finally {
                    try {
                        //System.out.println("Throwable close");
                        s.close();
                    } catch (Throwable t) {
                    /*do nothing*/
                    }
                }
            }

        }
    }

}
 