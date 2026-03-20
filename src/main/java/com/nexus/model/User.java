package com.nexus.model;

import java.util.ArrayList;
import java.util.List;
import com.nexus.exception.NexusValidationException;

/**
 * Representa um usuário no sistema Nexus.
 * 
 * Cada usuário possui um nome de usuário único e email validado.
 * Um usuário pode ter múltiplas tarefas atribuídas a ele.
 */
public class User {
    private final String username;
    private final String email;
    
    private List<Task> assignedTasks;

    /**
     * Cria um novo usuário com validações.
     * 
     * @param username o nome de usuário (não pode ser vazio)
     * @param email o endereço de email (deve conter '@')
     * @throws IllegalArgumentException se username ou email forem vazios
     * @throws NexusValidationException se o email não for válido
     */
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

    /**
     * Retorna o endereço de email do usuário.
     * @return o email
     */
    public String consultEmail() { return email; }
    
    /**
     * Retorna o nome de usuário.
     * @return o username
     */
    public String consultUsername() { return username; }

    /**
     * Calcula a carga de trabalho total (esforço em horas) do usuário.
     * Considera apenas tarefas em IN_PROGRESS.
     * 
     * @return o total de horas de tarefas em progresso
     */
    public long calculateWorkload() {
        return this.assignedTasks.stream()
        .filter(e -> e.getStatus() == TaskStatus.IN_PROGRESS)
        .mapToLong(e ->e.getEstimatedEffort())
        .sum();
    }

    /**
     * Atribui uma tarefa ao usuário.
     * 
     * @param task a tarefa a ser atribuída
     * @throws IllegalArgumentException se a tarefa for nula
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
     * Retorna uma cópia da lista de tarefas atribuídas ao usuário.
     * 
     * @return lista imutável de tarefas
     */
    public List<Task> getAllTasks() {
        return List.copyOf(this.assignedTasks);
    }

    /**
     * Retorna a quantidade de tarefas concluídas (DONE) pelo usuário.
     * 
     * @return número de tarefas finalizadas
     */
    public int getCountDoneTasks() {
        return (int) this.assignedTasks.stream()
            .filter(e -> e.getStatus() == TaskStatus.DONE)
            .count();
    }

    /**
     * Retorna a quantidade de tarefas em progresso (IN_PROGRESS) do usuário.
     * 
     * @return número de tarefas em andamento
     */
    public int getCountInProgressTasks() {
        return (int) this.assignedTasks.stream()
            .filter(e -> e.getStatus() == TaskStatus.IN_PROGRESS)
            .count();
    }
}