package com.wordbrain2.service.game;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class TimerService {
    private final Map<String, ScheduledFuture<?>> roomTimers = new ConcurrentHashMap<>();
    private final Map<String, Integer> roomTimeRemaining = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    
    public void startTimer(String roomCode, int duration, Runnable onTick, Runnable onComplete) {
        stopTimer(roomCode);
        
        roomTimeRemaining.put(roomCode, duration);
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            int remaining = roomTimeRemaining.get(roomCode);
            if (remaining > 0) {
                roomTimeRemaining.put(roomCode, remaining - 1);
                onTick.run();
            } else {
                stopTimer(roomCode);
                onComplete.run();
            }
        }, 0, 1, TimeUnit.SECONDS);
        
        roomTimers.put(roomCode, future);
    }
    
    public void stopTimer(String roomCode) {
        ScheduledFuture<?> future = roomTimers.remove(roomCode);
        if (future != null) {
            future.cancel(false);
        }
        roomTimeRemaining.remove(roomCode);
    }
    
    public void pauseTimer(String roomCode) {
        ScheduledFuture<?> future = roomTimers.get(roomCode);
        if (future != null) {
            future.cancel(false);
        }
    }
    
    public void addTime(String roomCode, int seconds) {
        Integer current = roomTimeRemaining.get(roomCode);
        if (current != null) {
            roomTimeRemaining.put(roomCode, current + seconds);
        }
    }
    
    public int getTimeRemaining(String roomCode) {
        return roomTimeRemaining.getOrDefault(roomCode, 0);
    }
    
    public boolean isTimerActive(String roomCode) {
        ScheduledFuture<?> future = roomTimers.get(roomCode);
        return future != null && !future.isDone();
    }
    
    public void startCountdown(String roomCode, int seconds, Runnable onComplete) {
        scheduler.schedule(() -> {
            onComplete.run();
        }, seconds, TimeUnit.SECONDS);
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}