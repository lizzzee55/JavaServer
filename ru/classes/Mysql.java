package ru.classes;

import java.sql.*;
import java.io.*;
import java.util.Properties;

public class Mysql {
    private static Mysql	_instance;

    public static Mysql getInstance() throws Exception
    {
        if (_instance == null) {
            _instance = new Mysql();
            _instance.loadMysqlProp();
        }

        return _instance;
    }

    private void loadMysqlProp() throws Exception  {
        System.err.println(" config/mysql.properties");
        FileInputStream fis;
        try {
            fis = new FileInputStream("config/mysql.properties");
            this.property.load(fis);
        } catch (IOException e) {
            System.err.println("Error config mysql file");
        }
    }

    public Properties property = new Properties();

    public Connection conn = null;

    public void mysqlInit() throws SQLException {
        try
        {
            String connectString = property.getProperty("URL");
            Class.forName("com.mysql.jdbc.Driver");
            this.conn = DriverManager.getConnection(connectString);
            System.out.println ("Database connection established");
        }
        catch (Exception e)
        {
            System.err.println ("Cannot connect to database server");
            //e.printStackTrace();
        }

    }

    public void mysqlClose() throws SQLException {
	    if(this.conn != null) {
			try
			{
				this.conn.close();
				System.err.println ("Mysql close");
			}
			catch (Exception e)
			{
				System.err.println ("error close mysql");
				e.printStackTrace();
			}
		}
    }

    public void query(String sql) throws SQLException {
        try
        {
            Statement statement = this.conn.createStatement();
            statement.execute(sql);
        }
        catch (Exception e)
        {
            System.err.println ("Sql fail: "+sql);
            //e.printStackTrace();
        }
    }

    public ResultSet row(String sql) throws SQLException {
        ResultSet rs = null;
        try
        {
            Statement statement = this.conn.createStatement();
            rs = statement.executeQuery(sql);
        }
        catch (Exception e)
        {
            System.err.println ("Sql fail: "+sql);
            e.printStackTrace();
        }
        return rs;
    }


}