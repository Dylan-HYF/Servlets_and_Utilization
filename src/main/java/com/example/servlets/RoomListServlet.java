package com.example.servlets;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.servlet.annotation.WebServlet;

@WebServlet("/roomList")
public class RoomListServlet extends HttpServlet {

    // Helper method to create a single availability slot object
    private JSONObject createSlot(String time, boolean booked) {
        JSONObject slot = new JSONObject();
        slot.put("timeSlot", time);
        slot.put("isBooked", booked);
        return slot;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json");

        JSONArray rooms = new JSONArray();

        // mock data:
        // --- MOCK DATA GENERATION START ---

        // --- Room 1: 9:00 AM Booked, 10:00 AM Available ---
        JSONObject room1 = new JSONObject();
        JSONArray room1Availability = new JSONArray();

        // Generate slots for Room 1
        room1Availability.put(createSlot("9:00 AM", true)); // Booked
        room1Availability.put(createSlot("10:00 AM", false)); // Available
        room1Availability.put(createSlot("11:00 AM", false)); // Available

        room1.put("id", 1);
        room1.put("name", "Study Room 1");
        room1.put("availability", room1Availability);
        rooms.put(room1);


        // --- Room 2: 1:00 PM Booked, 2:00 PM Available ---
        JSONObject room2 = new JSONObject();
        JSONArray room2Availability = new JSONArray();

        // Generate slots for Room 2
        room2Availability.put(createSlot("1:00 PM", true)); // Booked
        room2Availability.put(createSlot("2:00 PM", false)); // Available
        room2Availability.put(createSlot("3:00 PM", false)); // Available

        room2.put("id", 2);
        room2.put("name", "Study Room 2");
        room2.put("availability", room2Availability);
        rooms.put(room2);


        // --- Room 3: All available ---
        JSONObject room3 = new JSONObject();
        JSONArray room3Availability = new JSONArray();

        // Generate slots for Room 3
        room3Availability.put(createSlot("4:00 PM", false));
        room3Availability.put(createSlot("5:00 PM", false));
        room3Availability.put(createSlot("6:00 PM", false));

        room3.put("id", 3);
        room3.put("name", "Study Room 3");
        room3.put("availability", room3Availability);
        rooms.put(room3);

        // --- MOCK DATA GENERATION END ---

        response.getWriter().write(rooms.toString());

        // get data from db
//        try {
//            Connection conn = DBUtil.getConnection();
//            Statement stmt = conn.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT id, name, capacity FROM rooms");

            // JSONArray rooms = new JSONArray();// Array
            // get data from db:
            // while (rs.next()) {
            // JSONObject r = new JSONObject();//Object
            // r.put("id", rs.getInt("id"));
            // r.put("name", rs.getString("name"));
            // r.put("capacity", rs.getInt("capacity"));
            // rooms.put(r);//Place inside array
            // }

            // rs.close();
            // stmt.close();
            // conn.close();



            // response.getWriter().write(rooms.toString());
//        } catch (SQLException e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
//        }
    }
}