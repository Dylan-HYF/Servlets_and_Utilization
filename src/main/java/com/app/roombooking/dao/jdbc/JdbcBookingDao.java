package com.app.roombooking.dao.jdbc;

import com.app.roombooking.dao.BookingDao;
import com.app.roombooking.db.DbHelper;
import com.app.roombooking.model.Bookings;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of BookingDao using HSQLDB.
 * Overlap rule: (existing.start < newEnd) AND (existing.end > newStart)
 */
public class JdbcBookingDao implements BookingDao {

    // ---------- SQL snippets ----------
    private static final String COLS = "id, room_id, booked_by, start_time, end_time, created_at, updated_at";

    private static final String SQL_FIND_BY_ID =
            "SELECT " + COLS + " FROM bookings WHERE id = ?";

    private static final String SQL_FIND_BY_ROOM =
            "SELECT " + COLS + " FROM bookings WHERE room_id = ? ORDER BY start_time";

    private static final String SQL_FIND_ACTIVE_BY_ROOM_BETWEEN =
            "SELECT " + COLS + " FROM bookings " +
            "WHERE room_id = ? " +
            "  AND start_time < ? " +   // overlap: starts before requested end
            "  AND end_time   > ? " +   //         ends after requested start
            "ORDER BY start_time";

    private static final String SQL_EXIST_OVERLAP_EXCLUDING_ID =
            "SELECT COUNT(*) FROM bookings " +
            "WHERE (? = 0 OR id <> ?) " +  // exclude a booking id when updating; 0 means ignore
            "  AND start_time < ? " +
            "  AND end_time   > ? " +
            "  AND room_id    = ?";

    private static final String SQL_LIST_FOR_DAY =
            "SELECT " + COLS + " FROM bookings " +
            "WHERE start_time < ? AND end_time > ? " + // any overlap with day window
            "ORDER BY room_id, start_time";

    private static final String SQL_FIND_ROOM_ID_BY_BOOKING_ID = "SELECT room_id FROM bookings WHERE id = ?";

    private static final String SQL_INSERT =
            "INSERT INTO bookings(room_id, booked_by, start_time, end_time) VALUES (?,?,?,?)";

    private static final String SQL_DELETE =
            "DELETE FROM bookings WHERE id = ?";

    // ---------- Mapping ----------
    private static Bookings map(ResultSet rs) throws SQLException {
        Bookings b = new Bookings();
        b.setId(rs.getLong("id"));
        b.setRoomId(rs.getLong("room_id"));
        b.setBookedBy(rs.getString("booked_by"));
        b.setStartTime(rs.getTimestamp("start_time"));
        b.setEndTime(rs.getTimestamp("end_time"));
        // created_at is typically non-null
        b.setCreatedAt(rs.getTimestamp("created_at"));
        // updated_at may not exist depending on schema; set only if it is selected
        try {
            Timestamp updated = rs.getTimestamp("updated_at");
            b.setUpdatedAt(updated);
        } catch (SQLException ignore) {
            // column not present in SELECT; safe to ignore
        }
        return b;
    }

    // ---------- DAO methods ----------
    @Override
    public Bookings create(Bookings bookings) throws SQLException {
        // Guard: prevent overlap in the same room
        if (existOverlap(0L, bookings.getStartTime().toLocalDateTime(), bookings.getEndTime().toLocalDateTime(), bookings.getRoomId())) {
            throw new SQLException("OVERLAP_CONFLICT");
        }

        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, bookings.getRoomId());
            ps.setString(2, bookings.getBookedBy());
            ps.setTimestamp(3, bookings.getStartTime());
            ps.setTimestamp(4, bookings.getEndTime());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) bookings.setId(keys.getLong(1));
            }
            return bookings;
        }
    }

    @Override
    public Optional<Bookings> findById(Long id) throws SQLException {
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Bookings> findByRoom(long roomId) throws SQLException {
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_BY_ROOM)) {
            ps.setLong(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Bookings> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    @Override
    public List<Bookings> findActiveByRoomBetween(long roomId, LocalDateTime start, LocalDateTime end) throws SQLException {
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ACTIVE_BY_ROOM_BETWEEN)) {
            ps.setLong(1, roomId);
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                List<Bookings> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    @Override
    public boolean existOverlap(long bookingId, LocalDateTime newStart, LocalDateTime newEnd) throws SQLException {
        if (bookingId == 0) throw new IllegalArgumentException("bookingId must be non-zero for update overlap check");
        // Look up the room_id for this booking, then reuse the private overload
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_FIND_ROOM_ID_BY_BOOKING_ID)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No booking found with id=" + bookingId);
                }
                long roomId = rs.getLong(1);
                return existOverlap(bookingId, newStart, newEnd, roomId);
            }
        }
    }

    // Private overload used internally when we know the room id (e.g., on create)
    private boolean existOverlap(long excludeId, LocalDateTime newStart, LocalDateTime newEnd, long roomId) throws SQLException {
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_EXIST_OVERLAP_EXCLUDING_ID)) {
            ps.setLong(1, excludeId);
            ps.setLong(2, excludeId);
            ps.setTimestamp(3, Timestamp.valueOf(newEnd));
            ps.setTimestamp(4, Timestamp.valueOf(newStart));
            ps.setLong(5, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public List<Bookings> listForDay(LocalDate day) throws SQLException {
        LocalDateTime startOfDay = day.atStartOfDay();
        LocalDateTime endOfDay = day.atTime(LocalTime.MAX); // 23:59:59.999999999
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_LIST_FOR_DAY)) {
            ps.setTimestamp(1, Timestamp.valueOf(endOfDay));
            ps.setTimestamp(2, Timestamp.valueOf(startOfDay));
            try (ResultSet rs = ps.executeQuery()) {
                List<Bookings> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    @Override
    public boolean delete(long bookingId) throws SQLException {
        try (Connection c = DbHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_DELETE)) {
            ps.setLong(1, bookingId);
            return ps.executeUpdate() > 0;
        }
    }
}