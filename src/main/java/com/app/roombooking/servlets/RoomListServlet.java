package com.app.roombooking.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.json.JSONArray;
import org.json.JSONObject;
import javax.servlet.annotation.WebServlet;

@WebServlet("/roomList")
public class RoomListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("X-From", "RoomListServlet-DB");
        response.setHeader("X-RoomDbPath", String.valueOf(System.getProperty("ROOMDB_PATH")));

        String[] slots = { "09:00 AM","10:00 AM","11:00 AM","12:00 PM","01:00 PM","02:00 PM","03:00 PM","04:00 PM" };
        var out = new org.json.JSONArray();
        var today = java.time.LocalDate.now();

        try (var c = com.app.roombooking.db.DbHelper.getConnection()) {
            // 1) Rooms
            // Determine role from session (default to Student if missing)
            HttpSession session = request.getSession(false);
            String role = null;

// SAFELY log and read the session
            if (session == null) {
                System.out.println("[RoomList] No HTTP session – defaulting to Student");
                role = "Student";
            } else {
                System.out.println("[RoomList] sessionId=" + session.getId());
                Object roleAttr = session.getAttribute("role");
                if (roleAttr != null) {
                    role = roleAttr.toString();
                }
            }

// Final fallback just in case
            if (role == null || role.isBlank()) {
                role = "Student";
            }

            String normalizedRole = role.toLowerCase();
            System.out.println("[RoomList] normalizedRole=" + normalizedRole);

            // Professors see ALL rooms; students see only student rooms
            String sql;
            boolean hasParam;
            if ("professor".equals(normalizedRole)) {
                sql = "SELECT ROOMID AS id, ROOMNAME AS roomName, roomRole " +
                      "FROM rooms ORDER BY roomName";
                hasParam = false;
            } else {
                sql = "SELECT ROOMID AS id, ROOMNAME AS roomName, roomRole " +
                        "FROM rooms WHERE LOWER(roomRole) = ? ORDER BY roomName";
                hasParam = true;
            }

            try (var ps = c.prepareStatement(sql)) {
                if (hasParam) {
                    ps.setString(1, normalizedRole); // set parameter BEFORE executing
                }

                try (var rs = ps.executeQuery()) {
                    int count = 0;

                    while (rs.next()) {
                        long roomId = rs.getLong("id");
                        String name  = rs.getString("roomName");
                        String roomRole = rs.getString("roomRole");
                        System.out.println("[RoomList] id=" + roomId + " name=" + name + " role=" + roomRole);
                        count++;

                        // 2) Booked times for today (overlap with STARTTIME/ENDTIME)
                        java.util.HashSet<String> booked = new java.util.HashSet<>();
                        java.sql.Timestamp dayStart = java.sql.Timestamp.valueOf(today.atStartOfDay());
                        java.sql.Timestamp dayEnd   = java.sql.Timestamp.valueOf(today.plusDays(1).atStartOfDay());
                        try (var ps2 = c.prepareStatement(
                                "SELECT STARTTIME, ENDTIME FROM bookings WHERE ROOMID=? AND NOT (ENDTIME <= ? OR STARTTIME >= ?)")) {
                            ps2.setLong(1, roomId);
                            ps2.setTimestamp(2, dayStart);
                            ps2.setTimestamp(3, dayEnd);
                            try (var rs2 = ps2.executeQuery()) {
                                // parse slot labels to times once
                                java.time.format.DateTimeFormatter fmt =
                                        new java.time.format.DateTimeFormatterBuilder()
                                                .parseCaseInsensitive()
                                                .appendPattern("hh:mm a")
                                                .toFormatter(java.util.Locale.ENGLISH);
                                java.util.Map<String, java.time.LocalTime> slotStart = new java.util.HashMap<>();
                                java.util.Map<String, java.time.LocalTime> slotEnd   = new java.util.HashMap<>();
                                for (String s : slots) {
                                    String key = s.trim();
                                    java.time.LocalTime st = java.time.LocalTime.parse(key, fmt);
                                    slotStart.put(key, st);
                                    slotEnd.put(key, st.plusHours(1));
                                }
                                while (rs2.next()) {
                                    java.time.LocalDateTime st = rs2.getTimestamp(1).toLocalDateTime();
                                    java.time.LocalDateTime en = rs2.getTimestamp(2).toLocalDateTime();
                                    for (String s : slots) {
                                        String key = s.trim();
                                        java.time.LocalDateTime slotSt = java.time.LocalDateTime.of(today, slotStart.get(key));
                                        java.time.LocalDateTime slotEn = java.time.LocalDateTime.of(today, slotEnd.get(key));
                                        boolean overlaps = !(en.isEqual(slotSt) || en.isBefore(slotSt)
                                                             || st.isEqual(slotEn) || st.isAfter(slotEn));
                                        if (overlaps) booked.add(key);
                                    }
                                }
                            }
                        }

                        // 3) Build availability
                        var avail = new org.json.JSONArray();
                        for (String s : slots) {
                            var slotObj = new org.json.JSONObject();
                            slotObj.put("timeSlot", s);
                            slotObj.put("isBooked", booked.contains(s.trim()));
                            avail.put(slotObj);
                        }

                        // 4) Build room object
                        var roomObj = new org.json.JSONObject();
                        roomObj.put("id", roomId);
                        roomObj.put("name", name);
                        roomObj.put("availability", avail);
                        roomObj.put("role", roomRole);
                        out.put(roomObj);
                    }

                    System.out.println("[RoomList] fetched " + count +
                                       " rooms for role=" + normalizedRole);
                }
            }
            response.getWriter().write(out.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "DB error: " + e.getMessage());
        }
    }
}