package com.app.roombooking.dao.jdbc;

import com.app.roombooking.dao.RoomDao;
import com.app.roombooking.db.DbHelper;
import com.app.roombooking.model.Room;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcRoomDao implements RoomDao {

    // --- SQLs (keep them in one place for readability) ---
    private static final String SQL_SELECT_ALL =
            "SELECT roomId, roomName FROM rooms ORDER BY roomName";

    private static final String SQL_SELECT_BY_ID =
            "SELECT roomId, roomName FROM rooms WHERE roomId = ?";


    private static final String SQL_SELECT_BY_NAME =
            "SELECT roomId, roomName FROM rooms WHERE roomName = ?";

    private static final String SQL_INSERT =
            "INSERT INTO rooms (roomName) VALUES (?)";

    private static final String SQL_UPDATE =
            "UPDATE rooms SET roomName = ? WHERE roomId = ?";

    private static final String SQL_DELETE =
            "DELETE FROM rooms WHERE roomId = ?";

    // rooms with NO overlapping bookings in [start, end)
    // overlap condition: existing.start < end AND existing.end > start
    private static final String SQL_FIND_AVAILABLE_BETWEEN =
            "SELECT r.roomId, r.roomName " +
                    "FROM rooms r " +
                    "WHERE NOT EXISTS (" +
                    "  SELECT 1 FROM bookings b " +
                    "  WHERE b.roomId = r.roomId " +
                    "    AND b.startTime < ? " +
                    "    AND b.endTime   > ?" +
                    ") " +
                    "ORDER BY r.roomName";

    @Override
    public List<Room> findAll() throws SQLException {
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            List<Room> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    @Override
    public Optional<Room> findById(long roomId) throws SQLException {
        // WHAT: open a connection to the database.
        // WHY: you need a "phone line" to talk to the DB for this call only.
        // NOTE: try(...) auto-closes the connection when the block ends.
        try (Connection c = DbHelper.getConnection();

             // WHAT: create a precompiled SQL command: "SELECT ... WHERE id = ?"
             // WHY: PreparedStatement is safe (no SQL injection) and lets us plug in values.
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_BY_ID)) {

            // WHAT: put the 'id' value into the first ? in the SQL.
            // WHY: the SQL text has a placeholder; this fills it.
            // NOTE: JDBC parameter indexes start at 1 (not 0).
            ps.setLong(1, roomId);

            // WHAT: run the SELECT and get the results (a cursor over rows).
            // WHY: we need to fetch whatever the DB found.
            try (ResultSet rs = ps.executeQuery()) {

                // WHAT: move to the first row, if any. If there is one, build a Room from it.
                // WHY: findById returns either: the Room (if found) or nothing.
                // Optional.of(...) = found. Optional.empty() = not found.
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Room> findByName(String roomName) throws SQLException {
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_BY_NAME)) {

            ps.setString(1, roomName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Room create(Room room) throws SQLException {
        // Return room with generated id set
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, room.getRoomName());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    room.setRoomId(String.valueOf(keys.getLong(1))); // HSQLDB returns the identity value here
                }
            }
            return room;
        }
    }

    @Override
    public boolean update(Room room) throws SQLException {
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, room.getRoomName());
            ps.setLong(2, Long.parseLong(room.getRoomId()));
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(long roomId) throws SQLException {
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {

            ps.setLong(1, roomId);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public List<Room> findAvailableBetween(LocalDateTime start, LocalDateTime end) throws SQLException {
        // Convert LocalDateTime to SQL Timestamp for HSQLDB
        Timestamp startTime = Timestamp.valueOf(start);
        Timestamp endTime   = Timestamp.valueOf(end);

        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_AVAILABLE_BETWEEN)) {

            // order matters: b.startTime < ? (end), b.endTime > ? (start)
            ps.setTimestamp(1, endTime);
            ps.setTimestamp(2, startTime);

            try (ResultSet rs = ps.executeQuery()) {
                List<Room> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    // --- mapper: ResultSet -> Room (matches MVP schema: roomId, roomName) ---
    private static Room map(ResultSet rs) throws SQLException {
        return new Room(
            String.valueOf(rs.getLong("roomId")), // DB column roomId -> Room.roomId (String)
            rs.getString("roomName")               // DB column roomName -> Room.roomName
        );
    }
}