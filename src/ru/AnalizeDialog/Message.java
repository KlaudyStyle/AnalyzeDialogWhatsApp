package ru.AnalizeDialog;

import java.time.LocalDateTime;

public class Message {
    private LocalDateTime timestamp;
    private String sender;
    private String content;

    public Message(LocalDateTime timestamp, String sender, String content) {
        this.timestamp = timestamp;
        this.sender = sender;
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }
}

