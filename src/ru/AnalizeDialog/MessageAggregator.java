package ru.AnalizeDialog;

import java.time.LocalDate;
import java.util.*;

public class MessageAggregator {

    public static Map<LocalDate, Map<String, Integer>> aggregateMessagesByDay(List<Message> messages) {
        Map<LocalDate, Map<String, Integer>> aggregatedData = new TreeMap<>();

        for (Message msg : messages) {
            LocalDate date = msg.getTimestamp().toLocalDate();
            String sender = msg.getSender();

            aggregatedData.putIfAbsent(date, new HashMap<>());
            Map<String, Integer> senderCounts = aggregatedData.get(date);
            senderCounts.put(sender, senderCounts.getOrDefault(sender, 0) + 1);
        }

        return aggregatedData;
    }
}
