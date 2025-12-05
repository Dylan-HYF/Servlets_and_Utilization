package com.app.roombooking.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.JSONObject;
import javax.servlet.annotation.WebServlet;
import com.app.roombooking.db.DbHelper;

@WebServlet("/addRoom")
public class AddRoomServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject result = new JSONObject();

        // StringBuilder to reconstruct the JSON body from the request stream
        StringBuilder jsonBuilder = new StringBuilder();
        String jsonBody = null;

        try {
            // --- 2. Read the request body ---
            // Use getReader() to read the JSON content sent by the React frontend
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
            }

            jsonBody = jsonBuilder.toString();

            // --- 3. Print the received JSON body ---
            System.out.println("--- RECEIVED JSON BODY for Add Room ---");
            System.out.println(jsonBody);
            System.out.println("---------------------------------------");

            // --- 4. Parse JSON to verify content ---
            JSONObject roomData = new JSONObject(jsonBody);
            String roomName = roomData.optString("name");
            String roomRole = roomData.optString("role");

            // Print parsed data for verification
            System.out.println("Parsed Name: " + roomName);
            System.out.println("Parsed Role: " + roomRole);

            if (roomName == null || roomName.isBlank() || roomRole == null || roomRole.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("success", false)
                      .put("message", "Room name and role are required.");
                response.getWriter().write(result.toString());
                return;
            }

            // --- 5. Insert into DB using DbHelper ---
            long generatedId = -1L;

            String sql = "INSERT INTO rooms (ROOMNAME, ROOMROLE) VALUES (?, ?)"; // adjust column names if needed

            try (Connection conn = DbHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, roomName);
                ps.setString(2, roomRole);

                int affected = ps.executeUpdate();

                if (affected == 0) {
                    throw new SQLException("Creating room failed, no rows affected.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getLong(1);
                    }
                }
            }

            // --- 6. Send Success Response ---
            response.setStatus(HttpServletResponse.SC_CREATED); // 201 Created is typical for a resource creation
            result.put("success", true)
                  .put("message", "Room created successfully.")
                  .put("id", generatedId)
                  .put("name", roomName)
                  .put("role", roomRole);

        } catch (Exception e) {
            e.printStackTrace();
            // Handle any exceptions during JSON parsing or reading
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false)
                    .put("message", "Error processing request: " + e.getMessage());
        }

        response.getWriter().write(result.toString());
    }
}
