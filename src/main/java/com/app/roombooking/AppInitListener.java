package com.app.roombooking;

import com.app.roombooking.db.DbBootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitListener implements ServletContextListener {
// TOMCAT CALLS THIS ONCE THE APP STARTS
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("AppInitListener - Starting bootstrap...");
        DbBootstrap.run(); // CREATES TABLES IF MISSING FROM THIS CLASS
        System.out.println("AppInitListener - Bootstrap finished.");

    }

    //THIS IS AN EXTRA METHOD TO CLEAN UP ONLY
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("AppInitListener - Shutting down...");
    }
}
