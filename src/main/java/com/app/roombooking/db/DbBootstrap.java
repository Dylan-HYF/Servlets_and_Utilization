package com.app.roombooking.db;

import java.sql.*;

public final class DbBootstrap {
    private DbBootstrap() {}

    public static void run() {
        try (Connection c = DbHelper.getConnection()) {
            // THIS METHOD MAKES SCHEME AND SEEDS ONE ATOMIC TRANSACTION.
            c.setAutoCommit(false);
            // ENSURES TABLES/CONSTRAINTS EXIST
            createSchema(c);
            // OPTIONAL: SEEDS A DEFAULT ADMIN AND SAMPLE ROOMS TO USE THE APP ASAP
            seedIfEmpty(c);
            // SEEDS USERS
            seedUsersIfEmpty(c);
            // IF AUTOCOMMIT IS FALSE THIS IS REQUIRED
            c.commit();
            System.out.println("[DbBootstrap] schema ensured & seed applied");
        } catch (Exception e) {
            throw new RuntimeException("DbBootstrap failed", e);
        }
    }
    // THIS METHOD RECEIVES AN OPEN JDBC CONNECTION FROM THE DBHELPER LETTING IT RUN SQL
    private static void createSchema(Connection c) throws SQLException {
        // THIS PART OF THE CODE LETS US RUN THE CODE IN THE SAME OBJECT C SESSION/TRANSACTION
        try (Statement st = c.createStatement()) {
            // THE METHOD executeUpdate CALLS THE JDBC METHOD THAT EXECUTES NON-SELECT SQL STTM

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            " id INTEGER IDENTITY PRIMARY KEY," +
                            " roomRole VARCHAR(25) NOT NULL," +
                            " email VARCHAR(255) NOT NULL," +
                            " password VARCHAR(20) NOT NULL)"
            );

            System.out.println("[DbBootstrap] Ensuring table rooms exists");
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS rooms (" +
                    " roomId INTEGER IDENTITY PRIMARY KEY," +
                    " roomName VARCHAR(100) NOT NULL UNIQUE," +
                    " roomRole VARCHAR(25) NOT NULL" +
                    ")"
            );

            System.out.println("[DbBootstrap] Ensuring table bookings exists");
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS bookings (" +
                    " id INTEGER IDENTITY PRIMARY KEY," +
                    " roomId INTEGER NOT NULL," +
                    " bookedby VARCHAR(100) NULL," +
                    " startTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    " endTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    " createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                    " active BOOLEAN DEFAULT TRUE NOT NULL ," +
                    " CONSTRAINT fk_booking_room FOREIGN KEY (roomId) REFERENCES rooms(roomId)," +
                    "CONSTRAINT ck_time_order CHECK (endTime > startTime)" +
                    ")"
            );
        }
    }

    private static void seedIfEmpty(Connection c) throws SQLException {
        // allow disabling seeding with -DSEED_DATA=false
        if ("false".equalsIgnoreCase(System.getProperty("SEED_DATA", "true"))) {
            System.out.println("[DbBootstrap] SEED_DATA=false → skipping seed");
            return;
        }
        final String[][] rooms = {
                { "Room A", "Student" },
                { "Room B", "Student" },
                { "Room C", "Professor" },
                { "Room D", "Professor" }
        };

        String checkSql  = "SELECT 1 FROM rooms WHERE ROOMNAME = ?";
        String insertSql = "INSERT INTO rooms (ROOMNAME, ROOMROLE) VALUES (?, ?)";

        try (PreparedStatement sel = c.prepareStatement(checkSql);
             PreparedStatement ins = c.prepareStatement(insertSql)) {

            for (String[] room : rooms) {
                String name = room[0];
                String role = room[1];

                sel.setString(1, name);
                try (ResultSet rs = sel.executeQuery()) {
                    if (!rs.next()) {
                        ins.setString(1, name);
                        ins.setString(2, role);
                        ins.executeUpdate();
                        System.out.println("[DbBootstrap] Seeded room: " + name + " (" + role + ")");
                    } else {
                        System.out.println("[DbBootstrap] Room already exists: " + name);
                    }
                }
            }
        }
    }
    private static void seedUsersIfEmpty(Connection c) throws SQLException {
        // only run if SEED_DATA=true
        if ("false".equalsIgnoreCase(System.getProperty("SEED_DATA", "true"))) return;

        try (PreparedStatement check = c.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = check.executeQuery()) {

            rs.next();
            int count = rs.getInt(1);
            if (count > 0) {
                System.out.println("[DbBootstrap] users already seeded (" + count + ")");
                return;
            }
        }

        try (PreparedStatement ins = c.prepareStatement(
                "INSERT INTO users (email, password, roomRole) VALUES (?, ?, ?)")) {

            // Student
            ins.setString(1, "student@uni.edu");
            ins.setString(2, "pass123");
            ins.setString(3, "Student");
            ins.executeUpdate();

            // Professor
            ins.setString(1, "professor@uni.edu");
            ins.setString(2, "professor");
            ins.setString(3, "Professor");
            ins.executeUpdate();

            System.out.println("[DbBootstrap] Seeded users table (student + professor)");
        }
    }
}