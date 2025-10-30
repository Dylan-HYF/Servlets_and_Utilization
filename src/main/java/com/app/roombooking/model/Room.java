package com.app.roombooking.model;

public class Room {
    private String roomId;
    private String roomName;

    // CONSTRUCTOR
    public Room(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }

    // GETTERS / SETTERS
    //ROOM
    public String getRoomId() { return roomId; };
    public void setRoomId(String roomId) { this.roomId = roomId; };

    public String getRoomName() { return roomName; };
    public void setRoomName(String roomName) { this.roomName = roomName; };

    // FOR DEBUGGING
    @Override
    public String toString() {
        return "roomId: " + roomId + ", roomName: " + roomName;
    }








}
