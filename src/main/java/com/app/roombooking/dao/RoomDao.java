package com.app.roombooking.dao;

import com.app.roombooking.model.Room;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoomDao {

    // CREATE — returns room with generated id set
    Room create(Room room) throws SQLException;

    // READ
    Optional<Room> findById(long id) throws SQLException;
    Optional<Room> findByName(String name) throws SQLException;
    List<Room> findAll() throws SQLException;

    // UPDATE
    boolean update(Room room) throws SQLException;

    // DELETE
    boolean delete(long id) throws SQLException;

    // Custom query — rooms with NO overlapping booking in [start, end)
    List<Room> findAvailableBetween(LocalDateTime start, LocalDateTime end) throws SQLException;
}