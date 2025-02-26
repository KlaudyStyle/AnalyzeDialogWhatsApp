package ru.AnalizeDialog;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ResponseTimeCalculator {
    private List<Message> messages;
    private Map<LocalDate, List<Long>> responseTimesPerDay;

    public ResponseTimeCalculator(List<Message> messages) {
        this.messages = messages;
        this.responseTimesPerDay = new HashMap<>();
        calculateResponseTimes();
    }

    private void calculateResponseTimes() {
        if (messages.isEmpty()) return;

        Message previousMessage = messages.get(0);
        for (int i = 1; i < messages.size(); i++) {
            Message currentMessage = messages.get(i);
            if (!currentMessage.getSender().equals(previousMessage.getSender())) {
                Duration duration = Duration.between(previousMessage.getTimestamp(), currentMessage.getTimestamp());
                long minutes = duration.toMinutes();

                LocalDate date = currentMessage.getTimestamp().toLocalDate();
                responseTimesPerDay.computeIfAbsent(date, k -> new ArrayList<>()).add(minutes);
            }

            previousMessage = currentMessage;
        }
    }

    public double getAverageResponseTime() {
        long total = 0;
        int count = 0;
        for (List<Long> times : responseTimesPerDay.values()) {
            for (Long time : times) {
                total += time;
                count++;
            }
        }
        return count > 0 ? (double) total / count : 0;
    }

    public long getMaxResponseTime() {
        long max = 0;
        for (List<Long> times : responseTimesPerDay.values()) {
            for (Long time : times) {
                if (time > max) {
                    max = time;
                }
            }
        }
        return max;
    }

    public long getMinResponseTime() {
        long min = Long.MAX_VALUE;
        for (List<Long> times : responseTimesPerDay.values()) {
            for (Long time : times) {
                if (time < min) {
                    min = time;
                }
            }
        }
        return (min == Long.MAX_VALUE) ? 0 : min;
    }

    public Map<LocalDate, List<Long>> getResponseTimesPerDay() {
        return responseTimesPerDay;
    }
}
