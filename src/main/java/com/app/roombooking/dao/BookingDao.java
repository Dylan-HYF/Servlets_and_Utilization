package com.app.roombooking.dao;

import com.app.roombooking.model.Bookings;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingDao {
    //CREATE
    Bookings create(Bookings bookings) throws SQLException;

    //READ
    Optional<Bookings> findById(Long id) throws SQLException;
    List<Bookings> findByRoom(long roomId) throws SQLException;
    List<Bookings> findActiveByRoomBetween(long roomId,
       LocalDateTime start, LocalDateTime end) throws SQLException;

    //BUSSINESS RULE HELP WILL HELP IF THERE IS ANY OVERLAPPING
    boolean existOverlap(long bookingId, LocalDateTime newStart,
                         LocalDateTime newEnd) throws SQLException;

    //REPORTS
    List<Bookings> listForDay(LocalDate day) throws SQLException;

    //DELETE
    boolean delete(long bookingId) throws SQLException;






}
