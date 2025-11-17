package com.app.roombooking.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.servlet.annotation.WebServlet;
import com.app.roombooking.db.DbHelper;

@WebServlet("/roomListByRole")
public class RoleFilteredRoomListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        JSONObject result = new JSONObject();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            result.put("success", false)
                    .put("message", "Login required");
            response.getWriter().write(result.toString());
            return;
        }

        String role = (String) session.getAttribute("role");

        try (Connection c = DbHelper.getConnection()) {
            String sql;
            if ("STUDENT".equalsIgnoreCase(role)) {
                sql = "SELECT ROOMID AS id, ROOMNAME AS roomName FROM rooms WHERE STUDENT_VISIBLE=TRUE ORDER BY ROOMNAME";
            } else {
                sql = "SELECT ROOMID AS id, ROOMNAME AS roomName FROM rooms ORDER BY ROOMNAME";
            }

            JSONArray rooms = new JSONArray();
            try (PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    JSONObject room = new JSONObject();
                    room.put("id", rs.getLong("id"));
                    room.put("name", rs.getString("roomName"));
                    rooms.put(room);
                }
            }

            response.getWriter().write(rooms.toString());

        } catch (Exception e) {
            result.put("success", false)
                    .put("message", e.getMessage());
            response.getWriter().write(result.toString());
        }
    }
}