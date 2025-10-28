package com.example.servlets;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.JSONObject;
import javax.servlet.annotation.WebServlet;

@WebServlet("/confirmBooking")
public class BookRoomServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json");

        String roomId = request.getParameter("room_id");
        String bookedSlot = request.getParameter("booked_slot");
        // mock response
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", "Room booked successfully!");
        response.getWriter().write(result.toString());
        // select the record with room_id and time_slot
        // update the record: set is_booked to true


//        try {
//            Connection conn = DBUtil.getConnection();
//
//            PreparedStatement check = conn.prepareStatement(
//                    "SELECT * FROM booking WHERE room_id=? " +
//                            "AND (? BETWEEN start_time AND end_time OR ? BETWEEN start_time AND end_time)"
//            );
//            check.setString(1, roomId);
//            check.setString(2, start);
//            check.setString(3, end);
//            ResultSet rs = check.executeQuery();
//
//            JSONObject result = new JSONObject();
//
//            if (rs.next()) {
//                result.put("success", false);
//                result.put("message", "Room already booked!");
//            } else {
//                PreparedStatement insert = conn.prepareStatement(
//                        "INSERT INTO booking (room_id, user, start_time, end_time) VALUES (?, ?, ?, ?)"
//                );
//                insert.setString(1, roomId);
//                insert.setString(2, user);
//                insert.setString(3, start);
//                insert.setString(4, end);
//                insert.executeUpdate();
//
//                result.put("success", true);
//                result.put("message", "Room booked successfully!");
//                insert.close();
//            }
//
//            rs.close();
//            check.close();
//            conn.close();
//
//            response.getWriter().write(result.toString());
//        } catch (SQLException e) {
//            throw new RuntimeException(e); // exception
//        }
    }
}