package com.example.servlets;

import utils.DBUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class RoomListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json");

        try {
            Connection conn = DBUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name, capacity FROM rooms");

            JSONArray rooms = new JSONArray();//Array
            while (rs.next()) {
                JSONObject r = new JSONObject();//Object
                r.put("id", rs.getInt("id"));
                r.put("name", rs.getString("name"));
                r.put("capacity", rs.getInt("capacity"));
                rooms.put(r);//Place inside array
            }

            rs.close();
            stmt.close();
            conn.close();

            response.getWriter().write(rooms.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}