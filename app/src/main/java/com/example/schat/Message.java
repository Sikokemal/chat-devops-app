package com.example.schat;

import java.util.Date;

public class Message {
    private String text;
    private String imageUrl; // Новое поле для хранения URL изображения
    private long createdAt;
    private String uId;
    private boolean isRead;

    // Конструктор без параметров для Firebase
    public Message() {
        // Firebase требует конструктор без параметров
    }

    // Конструктор с двумя параметрами для текстового сообщения
    public Message(String uId, String text) {
        this.text = text;
        this.uId = uId;
        this.isRead = false; // По умолчанию сообщение непрочитано
        this.createdAt = new Date().getTime(); // Время создания
        this.imageUrl = ""; // Изображение отсутствует
    }

    // Конструктор с тремя параметрами для текстового сообщения
    public Message(String uId, String text, long createdAt) {
        this.text = text;
        this.uId = uId;
        this.isRead = false; // По умолчанию сообщение непрочитано
        this.createdAt = createdAt;
        this.imageUrl = ""; // Изображение отсутствует
    }

    // Конструктор для отправки изображения
    public Message(String uId, String imageUrl, long createdAt, boolean isRead) {
        this.uId = uId;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.text = ""; // Сообщение пустое, если это изображение
    }

    // Геттеры и сеттеры
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
