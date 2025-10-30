package com.app.roombooking;

import com.app.roombooking.dao.RoomDao;
import com.app.roombooking.dao.jdbc.JdbcRoomDao;
import com.app.roombooking.model.Room;

import java.time.LocalDateTime;

public class DbSmokeTest {
    public static void main(String[] args) throws Exception {
        //THIS PATH NAKES THE TEST RUN ON THE SAME DB AS TOMCAT TO SEE THE TABLES
        com.app.roombooking.db.DbBootstrap.run();
        // Optional: for local runs without VM options, uncomment and set your path
        // System.setProperty("ROOMDB_PATH", "/Users/rperaza/TomcatBase/roombooking-DB/roomdb-data");
// before using any DAO:
        System.setProperty("ROOMDB_PATH", "/Users/rperaza/TomcatBase/roombooking-DB/roomdb-data");
        RoomDao roomDao = new JdbcRoomDao();

        // Ensure a test room exists without violating UNIQUE(name)
        Room room = roomDao.findByName("Room A").orElseGet(() -> {
            try {
                return roomDao.create(new Room(null, "Room A"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to create Room A", e);
            }
        });
        System.out.println("Created/Found: " + room);

        // List all rooms
        System.out.println("Rooms = " + roomDao.findAll());

        // Availability window check (now .. +1h)
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = start.plusHours(1);
        System.out.println("Available: " + roomDao.findAvailableBetween(start, end));
    }
}