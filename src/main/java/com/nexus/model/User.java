package com.nexus.model;

import java.util.List;

public class User {
    private final String username;
    private final String email;
    
    private List<Task> assignedTasks;

    public User(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }
        this.username = username;
        this.email = email;
    }

    public String consultEmail() { return email; }
    public String consultUsername() { return username; }

    public long calculateWorkload() {
        return 0; 
    }

    public void assignTask(Task task) {
        this.assignedTasks.add(task);
    }

    public List<Task> getAllTasks() {
        return this.assignedTasks;
    }

    public int getCountDoneTasks() {
        return (int) this.assignedTasks.stream()
            .filter(e -> e.getStatus() == TaskStatus.DONE)
            .count();
    }

    public int getCountInProgressTasks() {
        return (int) this.assignedTasks.stream()
            .filter(e -> e.getStatus() == TaskStatus.IN_PROGRESS)
            .count();
    }
}