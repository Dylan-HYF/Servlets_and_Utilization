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

        try {
            // Read parameters (support JSON or form)
            String email = request.getParameter("email");
            String password = request.getParameter("password");

            if (email == null || password == null) {
                result.put("success", false)
                        .put("message", "email and password required");
                response.getWriter().write(result.toString());
                return;
            }

            try (Connection c = DbHelper.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT ROLE FROM users WHERE EMAIL=? AND PASSWORD=?")) {

                ps.setString(1, email);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String role = rs.getString("ROLE");

                        HttpSession session = request.getSession();
                        session.setAttribute("email", email);
                        session.setAttribute("role", role);

                        result.put("success", true)
                                .put("role", role)
                                .put("message", "Login successful");
                    } else {
                        result.put("success", false)
                                .put("message", "Invalid email or password");
                    }
                }
            }

            response.getWriter().write(result.toString());

        } catch (Exception e) {
            result.put("success", false)
                    .put("message", e.getMessage());
            response.getWriter().write(result.toString());
        }
    }
}