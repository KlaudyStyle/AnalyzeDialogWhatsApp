package ru.AnalizeDialog;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatParser {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm", Locale.ENGLISH);

    public static List<Message> parseChat(String filePath) {
        List<Message> messages = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (isNewMessage(line)) {
                    int separatorIndex = line.indexOf(" - ");
                    if (separatorIndex < 0) continue;

                    String timestampStr = line.substring(0, separatorIndex).trim();
                    LocalDateTime timestamp;
                    try {
                        timestamp = LocalDateTime.parse(timestampStr, formatter);
                    } catch (Exception e) {
                        continue;
                    }

                    String rest = line.substring(separatorIndex + 3).trim();
                    int colonIndex = rest.indexOf(": ");
                    if (colonIndex < 0) continue;

                    String sender = rest.substring(0, colonIndex).trim();
                    String content = rest.substring(colonIndex + 2).trim();

                    messages.add(new Message(timestamp, sender, content));
                } else {
                    if (!messages.isEmpty()) {
                        Message lastMessage = messages.get(messages.size() - 1);
                        String updatedContent = lastMessage.getContent() + "\n" + line.trim();
                        messages.set(messages.size() - 1, new Message(lastMessage.getTimestamp(), lastMessage.getSender(), updatedContent));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }
        return messages;
    }

    private static boolean isNewMessage(String line) {
        return line.matches("^\\d{2}\\.\\d{2}\\.\\d{4}, \\d{2}:\\d{2} - .*");
    }
}



