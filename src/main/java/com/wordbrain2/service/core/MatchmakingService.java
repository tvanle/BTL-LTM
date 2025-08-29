package com.wordbrain2.service.core;

import com.wordbrain2.model.entity.Player;
import com.wordbrain2.model.entity.Room;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatchmakingService {
    
    private final RoomService roomService;
    
    public MatchmakingService(RoomService roomService) {
        this.roomService = roomService;
    }
    
    public Optional<Room> findAvailableRoom(String topic, int skillLevel) {
        return roomService.getAllRooms().values().stream()
            .filter(this::isRoomAvailable)
            .filter(room -> matchesTopic(room, topic))
            .filter(room -> matchesSkillLevel(room, skillLevel))
            .findFirst();
    }
    
    public List<Room> findCompatibleRooms(Player player) {
        return roomService.getAllRooms().values().stream()
            .filter(this::isRoomAvailable)
            .filter(room -> isPlayerCompatible(room, player))
            .collect(Collectors.toList());
    }
    
    public Optional<Room> quickMatch(Player player) {
        return findAvailableRoom(null, 0);
    }
    
    public boolean canJoinRoom(Room room, Player player) {
        return isRoomAvailable(room) && 
               !room.getPlayerIds().contains(player.getId()) &&
               isPlayerCompatible(room, player);
    }
    
    private boolean isRoomAvailable(Room room) {
        return room.getPlayerIds().size() < room.getMaxPlayers() &&
               room.getStatus().name().equals("WAITING");
    }
    
    private boolean matchesTopic(Room room, String topic) {
        if (topic == null || topic.isEmpty()) {
            return true;
        }
        return topic.equals(room.getTopic());
    }
    
    private boolean matchesSkillLevel(Room room, int skillLevel) {
        return true; // For now, accept all skill levels
    }
    
    private boolean isPlayerCompatible(Room room, Player player) {
        return true; // For now, all players are compatible
    }
    
    public int getRecommendedRoomSize(String topic) {
        switch (topic) {
            case "competitive": return 4;
            case "casual": return 6;
            case "tournament": return 8;
            default: return 4;
        }
    }
}