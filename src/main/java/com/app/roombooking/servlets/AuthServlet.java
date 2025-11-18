package com.app.roombooking.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.JSONObject;
import javax.servlet.annotation.WebServlet;
import com.app.roombooking.db.DbHelper;

@WebServlet("/login")
public class AuthServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        JSONObject result = new JSONObject();
        // Mock data
        // 1. Read JSON body from the request
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        } catch (Exception e) {
            // Handle error reading JSON body
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not read request body.");
            return;
        }

        String email = null;
        String password = null;

        try {
            JSONObject json = new JSONObject(jsonBuilder.toString());
            email = json.optString("email");
            password = json.optString("password");

        } catch (Exception e) {
            // Handle JSON parsing error
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format in request.");
            return;
        }

        // 2. Mock Authentication Logic
        if (email == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false)
                    .put("message", "Email and password required");

        } else if ("student@uni.edu".equals(email) && "pass123".equals(password)) {
            // Mock Success: Return Student Role
            String role = "Student";

            // Set session attributes (if session management is required)
            HttpSession session = request.getSession();
            session.setAttribute("email", email);
            session.setAttribute("role", role);

            response.setStatus(HttpServletResponse.SC_OK);
            result.put("success", true)
                    .put("id", "S1001") // Mock ID for frontend
                    .put("role", role)
                    .put("message", "Login successful");

        } else if ("professor@uni.edu".equals(email) && "professor".equals(password)) {
            // Mock Success: Return Admin Role
            String role = "Professor";

            HttpSession session = request.getSession();
            session.setAttribute("email", email);
            session.setAttribute("role", role);

            response.setStatus(HttpServletResponse.SC_OK);
            result.put("success", true)
                    .put("id", "A999") // Mock ID for frontend
                    .put("role", role)
                    .put("message", "Login successful");

        } else {
            // Mock Failure
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401
            result.put("success", false)
                    .put("message", "Invalid email or password");
        }

        response.getWriter().write(result.toString());

        // connect to db
//        try {
//            // Read parameters (support JSON or form)
//            String email = request.getParameter("email");
//            String password = request.getParameter("password");
//
//            if (email == null || password == null) {
//                result.put("success", false)
//                        .put("message", "email and password required");
//                response.getWriter().write(result.toString());
//                return;
//            }
//
//            try (Connection c = DbHelper.getConnection();
//                 PreparedStatement ps = c.prepareStatement(
//                         "SELECT ROLE FROM users WHERE EMAIL=? AND PASSWORD=?")) {
//
//                ps.setString(1, email);
//                ps.setString(2, password);
//
//                try (ResultSet rs = ps.executeQuery()) {
//                    if (rs.next()) {
//                        String role = rs.getString("ROLE");
//
//                        HttpSession session = request.getSession();
//                        session.setAttribute("email", email);
//                        session.setAttribute("role", role);
//
//                        result.put("success", true)
//                                .put("role", role)
//                                .put("message", "Login successful");
//                    } else {
//                        result.put("success", false)
//                                .put("message", "Invalid email or password");
//                    }
//                }
//            }
//
//            response.getWriter().write(result.toString());
//
//        } catch (Exception e) {
//            result.put("success", false)
//                    .put("message", e.getMessage());
//            response.getWriter().write(result.toString());
//        }
    }
}