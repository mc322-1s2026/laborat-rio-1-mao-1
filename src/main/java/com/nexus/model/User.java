package com.nexus.model;

import java.util.ArrayList;
import java.util.List;
import com.nexus.exception.NexusValidationException;

public class User {
    private final String username;
    private final String email;
    
    private List<Task> assignedTasks;

    public User(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email não pode ser vazio.");
        }
        if (!email.contains("@")) {
            throw new NexusValidationException("Email deve conter '@'");
        }

        this.username = username;
        this.email = email;
        this.assignedTasks = new ArrayList<>();
    }

    /** Retorna o email do usuário. */
    public String consultEmail() { return email; }
    /** Retorna o email do usuário */
    public String consultUsername() { return username; }

    /**
     * Calcula a soma do esforço esperado do usuário.
     */
    public long calculateWorkload() {
        return this.assignedTasks.stream()
        .filter(e -> e.getStatus() == TaskStatus.IN_PROGRESS)
        .mapToLong(e ->e.getEstimatedEffort())
        .sum();
    }

    /**
     * Adiciona uma tarefa à lista de tarefas do usuário.
     */
    public void assignTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Tarefa não pode ser nula.");
        }

        if (!this.assignedTasks.contains(task)) {
            this.assignedTasks.add(task);
        }
    }

    /**
     * Retorna a lista de tarefas do usuário
     */
    public List<Task> getAllTasks() {
        return List.copyOf(this.assignedTasks);
    }

    /** 
     * Retorna a quantidade de tarefas feitas pelo usuário.
     */
    public int getCountDoneTasks() {
        return (int) this.assignedTasks.stream()
            .filter(e -> e.getStatus() == TaskStatus.DONE)
            .count();
    }

    /**
     * Retorna a quantidade de tarefas em progresso do usuário.
     */
    public int getCountInProgressTasks() {
        return (int) this.assignedTasks.stream()
            .filter(e -> e.getStatus() == TaskStatus.IN_PROGRESS)
            .count();
    }
}