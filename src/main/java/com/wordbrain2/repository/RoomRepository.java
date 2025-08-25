package com.wordbrain2.repository;

import com.wordbrain2.model.entity.Room;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Repository
public class RoomRepository {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    
    public Room save(Room room) {
        rooms.put(room.getRoomCode(), room);
        return room;
    }
    
    public Optional<Room> findById(String roomCode) {
        return Optional.ofNullable(rooms.get(roomCode));
    }
    
    public List<Room> findAll() {
        return new ArrayList<>(rooms.values());
    }
    
    public List<Room> findByStatus(String status) {
        return rooms.values().stream()
            .filter(room -> room.getStatus().name().equals(status))
            .collect(Collectors.toList());
    }
    
    public List<Room> findByHostId(String hostId) {
        return rooms.values().stream()
            .filter(room -> room.getHostId().equals(hostId))
            .collect(Collectors.toList());
    }
    
    public List<Room> findPublicRooms() {
        return rooms.values().stream()
            .filter(room -> !room.isPrivate())
            .collect(Collectors.toList());
    }
    
    public void deleteById(String roomCode) {
        rooms.remove(roomCode);
    }
    
    public boolean existsById(String roomCode) {
        return rooms.containsKey(roomCode);
    }
    
    public long count() {
        return rooms.size();
    }
    
    public void deleteAll() {
        rooms.clear();
    }
}