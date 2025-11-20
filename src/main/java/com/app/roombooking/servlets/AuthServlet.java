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
        response.setCharacterEncoding("UTF-8");

        JSONObject result = new JSONObject();

        // 1) Read JSON body (email + password) from the request
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false)
                    .put("message", "Could not read request body.");
            response.getWriter().write(result.toString());
            return;
        }

        String email;
        String password;
        try {
            JSONObject json = new JSONObject(jsonBuilder.toString());
            email = json.optString("email", null);
            password = json.optString("password", null);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false)
                    .put("message", "Invalid JSON format in request.");
            response.getWriter().write(result.toString());
            return;
        }

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false)
                    .put("message", "Email and password required");
            response.getWriter().write(result.toString());
            return;
        }

        // 2) Look up user in DB by email
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, password, roomRole FROM users WHERE email = ?")) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // No such email → login fail
                    System.out.println("[Auth] LOGIN FAIL (no user): email=" + email);

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    result.put("success", false)
                            .put("message", "Invalid email or password");
                    response.getWriter().write(result.toString());
                    return;
                }

                long Id = rs.getLong("id");          // adjust column name if needed
                String dbPassword = rs.getString("password");
                String dbRole = rs.getString("roomRole"); // "Student" or "Professor"

                if (!dbPassword.equals(password)) {
                    System.out.println("[Auth] LOGIN FAIL (bad password): email=" + email);

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    result.put("success", false)
                            .put("message", "Invalid email or password");
                    response.getWriter().write(result.toString());
                    return;
                }

                // 3) Success → set session attributes for RoomListServlet
                HttpSession session = request.getSession(true);
                session.setAttribute("email", email);
                session.setAttribute("role", dbRole);

                System.out.println("[Auth] LOGIN OK: " + email +
                        " role=" + dbRole +
                        " sessionId=" + session.getId());

                // 4) Build success JSON expected by the FE
                result.put("success", true)
                        .put("id", Id)
                        .put("role", dbRole)
                        .put("message", "Login successful");

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(result.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = new JSONObject();
            result.put("success", false)
                    .put("message", "Server error");
            response.getWriter().write(result.toString());
        }
    }
}
