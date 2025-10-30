package com.app.roombooking.model;

import java.sql.Timestamp;

/**
 * Plain Java model (POJO) for a booking record.
 * Made JavaBean-style: no-arg constructor + getters/setters
 * so JDBC mappers can construct and populate it field-by-field.
 */
public class Bookings {
    private long id;              // maps to bookings.id
    private long roomId;          // maps to bookings.room_id
    private String bookedBy;      // maps to bookings.booked_by
    private Timestamp startTime;  // maps to bookings.start_time
    private Timestamp endTime;    // maps to bookings.end_time
    private Timestamp createdAt;  // maps to bookings.created_at
    private Timestamp updatedAt;  // maps to bookings.updated_at (if present in schema)

    // --- Constructors ---
    public Bookings() { } // required by mappers / frameworks

    // --- Getters / Setters ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRoomId() { return roomId; }
    public void setRoomId(long roomId) { this.roomId = roomId; }

    public String getBookedBy() { return bookedBy; }
    public void setBookedBy(String bookedBy) { this.bookedBy = bookedBy; }

    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Bookings{" +
                "id=" + id +
                ", roomId=" + roomId +
                ", bookedBy='" + bookedBy + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
