package com.nexus.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import com.nexus.exception.NexusValidationException;

public class User {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final String username;
    private final String email;
    
    private final List<Task> assignedTasks;

    public User(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email não pode ser vazio.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new NexusValidationException("Email inválido. Use o formato usuario@dominio.com");
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