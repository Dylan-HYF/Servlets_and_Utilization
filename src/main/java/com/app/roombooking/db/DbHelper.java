package com.app.roombooking.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbHelper {
    // Constructor set as private as no instantiation is needed
    private DbHelper() {}

    // THIS METHOD RETURN THE PATH WHERE THE DB FOLDER WILL BE CREATED
    private static String portablePath(){
        //FORCES THE DB FOLDER TO A SPECIFIC LOCATION
        String override = System.getProperty("ROOMDB_PATH");
        if (override != null && !override.isBlank()) return override;

        String catalinaBase = System.getProperty("catalina.base"); // TOMCAT FOLDER
        if (catalinaBase != null && !catalinaBase.isEmpty()) return catalinaBase + "/roomdb-data";

        return System.getProperty("user.home") + "/roomdb-data";
    }

    // BASE RECEIVES THE PATH GIVEN FROM BASEPATH
    private static final String BASE = portablePath();
    // THIS INITIALIZER ENSURES A NEW FILE IS CREATED IF NEEDED
    static {
        File dir = new File(BASE);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create DB directory: " + BASE);
        }
    }

    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static final String URL = "jdbc:hsqldb:file:" + BASE + "/roombookingdb;shutdown=true";


    // THIS INITIALIZER EXPLICIT LOADS THE DRIVER
    static {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
            System.out.println("[DbHelper] HSQLDB JDBCDriver loaded");
            //debugging
            System.out.println("[DbHelper] BASE=" + BASE);
            System.out.println("[DbHelper] URL=" + URL);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("HSQLDB driver not on classpath", e);
        }
    }

    // This method opens and returns a java.sql.Connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);

    }


}
