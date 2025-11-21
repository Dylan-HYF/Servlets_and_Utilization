package com.app.roombooking.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.json.JSONObject;
import javax.servlet.annotation.WebServlet;
import com.app.roombooking.db.DbHelper;
import java.sql.*;
import java.time.LocalDate;

@WebServlet("/confirmBooking")
public class BookRoomServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // prevent caching while developing
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        try {
            // Prefer JSON body: { "room_id": 1, "booked_slot": "10:00 AM", "booked_by": "Alice" }
            String bodyStr = null;
            String ct = request.getContentType();
            if (ct != null && ct.toLowerCase().contains("application/json")) {
                try (BufferedReader r = request.getReader()) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line);
                    bodyStr = sb.toString();
                }
            }

            String roomIdStr;
            String slotLabel;
            String bookedBy;
            if (bodyStr != null && !bodyStr.isBlank()) {
                JSONObject body = new JSONObject(bodyStr);
                roomIdStr = String.valueOf(body.getLong("room_id"));
                slotLabel = body.getString("booked_slot");
                bookedBy  = body.has("booked_by") ? body.optString("booked_by", null) : null;
            } else {
                // Fallback to form params if not JSON
                roomIdStr = request.getParameter("room_id");
                slotLabel = request.getParameter("booked_slot");
                if (slotLabel == null) slotLabel = request.getParameter("slot"); // tolerate alt name
                bookedBy  = request.getParameter("booked_by");
            }

            if (roomIdStr == null || slotLabel == null || slotLabel.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(new JSONObject()
                        .put("success", false)
                        .put("reason", "missing_parameters")
                        .put("message", "room_id and slot are required")
                        .toString());
                return;
            }

            long roomId = Long.parseLong(roomIdStr);

            // Parse slot label
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter fmt =
                new java.time.format.DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("hh:mm a")
                    .toFormatter(java.util.Locale.ENGLISH);

            java.time.LocalTime startLocalTime = java.time.LocalTime.parse(slotLabel.trim(), fmt);
            java.time.LocalDateTime startLdt = java.time.LocalDateTime.of(today, startLocalTime);
            java.time.LocalDateTime endLdt   = startLdt.plusHours(1);

            java.sql.Timestamp startTs = java.sql.Timestamp.valueOf(startLdt);
            java.sql.Timestamp endTs   = java.sql.Timestamp.valueOf(endLdt);

            try (java.sql.Connection c = com.app.roombooking.db.DbHelper.getConnection();
                 java.sql.PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO bookings(ROOMID, BOOKEDBY, STARTTIME, ENDTIME, ACTIVE) VALUES (?,?,?,?,TRUE)")) {

                ps.setLong(1, roomId);
                if (bookedBy == null || bookedBy.isBlank()) {
                    ps.setNull(2, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(2, bookedBy);
                }
                ps.setTimestamp(3, startTs);
                ps.setTimestamp(4, endTs);
                ps.executeUpdate();

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(new JSONObject()
                        .put("success", true)
                        .put("room_id", roomId)
                        .put("start", startLdt.toString())
                        .put("end", endLdt.toString())
                        .toString());
                return;
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException dup) {
            // Unique constraint (ROOMID, STARTTIME, ENDTIME) prevents double-booking
            response.setStatus(HttpServletResponse.SC_CONFLICT); // 409
            response.getWriter().write(new JSONObject()
                    .put("success", false)
                    .put("reason", "slot_taken")
                    .put("message", "This time slot is already booked for the selected room.")
                    .toString());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(new JSONObject()
                    .put("success", false)
                    .put("reason", "db_error")
                    .put("message", e.getMessage())
                    .toString());
        }
    }
}