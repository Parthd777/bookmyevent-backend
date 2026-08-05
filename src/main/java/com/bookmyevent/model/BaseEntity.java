package com.bookmyevent.model;

import java.time.LocalDateTime;
import java.time.ZoneId;

public abstract class BaseEntity {
    private long id;
    private LocalDateTime createdAt;

    protected BaseEntity() {
        this.createdAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    protected BaseEntity(long id) {
        this();
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
