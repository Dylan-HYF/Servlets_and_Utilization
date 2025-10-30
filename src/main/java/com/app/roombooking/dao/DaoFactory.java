package com.app.roombooking.dao;

import com.app.roombooking.dao.jdbc.JdbcBookingDao;
import com.app.roombooking.dao.jdbc.JdbcRoomDao;

/**
 * Tiny factory to hand out DAO singletons.
 * Why: keeps servlets/controllers clean (no 'new' scattered), and lets us swap implementations later.
 */
public final class DaoFactory {
    // Single instances reused across the app (they are stateless)
    private static final RoomDao ROOM_DAO = new JdbcRoomDao();
    private static final BookingDao BOOKING_DAO = new JdbcBookingDao();

    private DaoFactory() {} // no instances

    public static RoomDao rooms()   { return ROOM_DAO; }
    public static BookingDao bookings() { return BOOKING_DAO; }
}