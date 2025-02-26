package ru.AnalizeDialog;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MessageFilter {

    public static List<Message> filterMessagesByDate(List<Message> messages, LocalDate startDate, LocalDate endDate) {
        List<Message> filtered = new ArrayList<>();

        for (Message msg : messages) {
            LocalDate msgDate = msg.getTimestamp().toLocalDate();
            boolean afterStart = (startDate == null) || (!msgDate.isBefore(startDate));
            boolean beforeEnd = (endDate == null) || (!msgDate.isAfter(endDate));

            if (afterStart && beforeEnd) {
                filtered.add(msg);
            }
        }

        return filtered;
    }
}
