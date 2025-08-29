package com.wordbrain2.util;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TimeSync {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        .withZone(ZoneOffset.UTC);
    
    private final AtomicLong serverStartTime;
    private final AtomicLong lastSyncTime;
    
    public TimeSync() {
        long currentTime = System.currentTimeMillis();
        this.serverStartTime = new AtomicLong(currentTime);
        this.lastSyncTime = new AtomicLong(currentTime);
    }
    
    /**
     * Get current server time in milliseconds
     */
    public long getCurrentServerTime() {
        return System.currentTimeMillis();
    }
    
    /**
     * Get server uptime in milliseconds
     */
    public long getServerUptime() {
        return getCurrentServerTime() - serverStartTime.get();
    }
    
    /**
     * Get synchronized timestamp for game events
     */
    public long getSyncedTimestamp() {
        long currentTime = getCurrentServerTime();
        lastSyncTime.set(currentTime);
        return currentTime;
    }
    
    /**
     * Format timestamp for display
     */
    public String formatTimestamp(long timestamp) {
        return TIMESTAMP_FORMAT.format(Instant.ofEpochMilli(timestamp));
    }
    
    /**
     * Calculate time difference in milliseconds
     */
    public long getTimeDifference(long startTime, long endTime) {
        return endTime - startTime;
    }
    
    /**
     * Check if time period has elapsed
     */
    public boolean hasElapsed(long startTime, long duration) {
        return getCurrentServerTime() - startTime >= duration;
    }
    
    /**
     * Get remaining time in a period
     */
    public long getRemainingTime(long startTime, long duration) {
        long elapsed = getCurrentServerTime() - startTime;
        return Math.max(0, duration - elapsed);
    }
    
    /**
     * Calculate game timer countdown
     */
    public long calculateCountdown(long levelStartTime, long levelDuration) {
        return getRemainingTime(levelStartTime, levelDuration);
    }
    
    /**
     * Check if countdown has reached zero
     */
    public boolean isCountdownFinished(long startTime, long duration) {
        return getRemainingTime(startTime, duration) <= 0;
    }
    
    /**
     * Get time until next sync point
     */
    public long getTimeUntilNextSync(long syncInterval) {
        long lastSync = lastSyncTime.get();
        return getRemainingTime(lastSync, syncInterval);
    }
    
    /**
     * Create time synchronization data for clients
     */
    public TimeSyncData createSyncData() {
        long serverTime = getCurrentServerTime();
        return new TimeSyncData(serverTime, getServerUptime());
    }
    
    /**
     * Validate client timestamp against server time
     */
    public boolean isValidClientTimestamp(long clientTimestamp, long maxDrift) {
        long serverTime = getCurrentServerTime();
        long drift = Math.abs(serverTime - clientTimestamp);
        return drift <= maxDrift;
    }
    
    /**
     * Calculate network delay compensation
     */
    public long calculateNetworkDelay(long clientSentTime, long serverReceivedTime, long serverResponseTime) {
        return (serverReceivedTime - clientSentTime + serverResponseTime - serverReceivedTime) / 2;
    }
    
    /**
     * Adjust client time for server synchronization
     */
    public long adjustClientTime(long clientTime, long networkDelay) {
        return clientTime + networkDelay;
    }
    
    /**
     * Format duration in human-readable format
     */
    public String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        }
        
        long seconds = milliseconds / 1000;
        if (seconds < 60) {
            return seconds + "s";
        }
        
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        if (minutes < 60) {
            return minutes + "m " + remainingSeconds + "s";
        }
        
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return hours + "h " + remainingMinutes + "m " + remainingSeconds + "s";
    }
    
    /**
     * Get performance timing statistics
     */
    public TimingStats getTimingStats(long operationStart) {
        long operationEnd = getCurrentServerTime();
        long duration = operationEnd - operationStart;
        return new TimingStats(operationStart, operationEnd, duration);
    }
    
    // Inner classes for data structures
    public static class TimeSyncData {
        private final long serverTime;
        private final long serverUptime;
        
        public TimeSyncData(long serverTime, long serverUptime) {
            this.serverTime = serverTime;
            this.serverUptime = serverUptime;
        }
        
        public long getServerTime() { return serverTime; }
        public long getServerUptime() { return serverUptime; }
    }
    
    public static class TimingStats {
        private final long startTime;
        private final long endTime;
        private final long duration;
        
        public TimingStats(long startTime, long endTime, long duration) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.duration = duration;
        }
        
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public long getDuration() { return duration; }
        
        @Override
        public String toString() {
            return "TimingStats{duration=" + duration + "ms}";
        }
    }
}