package com.nexus.model;

import com.nexus.exception.NexusValidationException;
import java.time.LocalDate;

/**
 * Representa uma tarefa no sistema Nexus.
 * 
 * Cada tarefa possui um ID único, título, prazo, status e um usuário responsável.
 * Uma tarefa pode estar em um de quatro estados: TO_DO, IN_PROGRESS, BLOCKED ou DONE.
 */
public class Task {
    private static int totalTasksCreated = 0;
    private static int totalValidationErrors = 0;
    private static int activeWorkload = 0;

    private static int nextId = 1;

    private final int id;
    private final LocalDate deadline; 
    private final String title;
    private TaskStatus status;
    private User owner;
    private int estimatedEffort;

    /**
     * Cria uma nova tarefa.
     * 
     * @param title o título da tarefa
     * @param deadline a data de vencimento da tarefa
     * @param effort o esforço estimado em horas
     */
    public Task(String title, LocalDate deadline, int effort) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("O título da tarefa não pode ser vazio.");
        }
        if (deadline == null) {
            throw new IllegalArgumentException("A data de vencimento não pode ser nula.");
        }
        if (effort <= 0) {
            throw new IllegalArgumentException("O esforço estimado deve ser um valor positivo.");
        }

        this.id = nextId++;
        this.deadline = deadline;
        this.title = title;
        this.status = TaskStatus.TO_DO;
        this.owner = null;
        this.estimatedEffort = effort;
        
        totalTasksCreated++; 
    }

    /**
     * Define um usuário como responsável pela tarefa.
     * 
     * @param owner o usuário responsável
     * @throws NexusValidationException se o owner for nulo
     */
    public void assignOwner(User owner) {
        if (owner == null) {
            incrementValidationErrors();
            throw new NexusValidationException("Owner não pode ser nulo.");
        }
        this.owner = owner;
    }

    /**
     * Move a tarefa para o estado IN_PROGRESS.
     * 
     * @throws NexusValidationException se não houver um responsável ou se estiver bloqueada
     */
    public void moveToInProgress() {
        if(this.status == TaskStatus.IN_PROGRESS) return;

        if(this.owner != null && this.status != TaskStatus.BLOCKED) {
            this.status = TaskStatus.IN_PROGRESS;
            activeWorkload += this.estimatedEffort;
        }
        else {
            incrementValidationErrors();
            throw new NexusValidationException("Task bloqueada ou sem dono.");
        }
    }

    /**
     * Marca a tarefa como finalizada (DONE).
     * 
     * @throws NexusValidationException se a tarefa estiver bloqueada
     */
    public void markAsDone() {
        if(this.status == TaskStatus.DONE) return;

        if(this.status != TaskStatus.BLOCKED) {
            if(this.status == TaskStatus.IN_PROGRESS) {
                activeWorkload -= this.estimatedEffort;    
            }
            this.status = TaskStatus.DONE;
        }
        else {
            incrementValidationErrors();
            throw new NexusValidationException("Task bloqueada.");
        }
    }

    /**
     * Bloqueia a tarefa, impedindo sua progressão.
     * 
     * @throws NexusValidationException se a tarefa já estiver finalizada
     */
    public void setBlocked() {
        if (this.status == TaskStatus.BLOCKED) return;

        if (this.status != TaskStatus.DONE) {
            if (this.status == TaskStatus.IN_PROGRESS) {
                activeWorkload -= this.estimatedEffort;
            } 
            this.status = TaskStatus.BLOCKED; 
            }
        else {
            incrementValidationErrors();
            throw new NexusValidationException("Task feita não pode ser bloqueada");
        }
    }

    /**
     * Retorna o ID único da tarefa.
     * @return o ID da tarefa
     */
    public int getId() { return id; }
    
    /**
     * Retorna o status atual da tarefa.
     * @return o status (TO_DO, IN_PROGRESS, BLOCKED ou DONE)
     */
    public TaskStatus getStatus() { return status; }
    
    /**
     * Retorna o título da tarefa.
     * @return o título
     */
    public String getTitle() { return title; }
    
    /**
     * Retorna a data de vencimento.
     * @return a data final
     */
    public LocalDate getDeadline() { return deadline; }
    
    /**
     * Retorna o usuário responsável pela tarefa.
     * @return o owner da tarefa, ou null se não atribuído
     */
    public User getOwner() { return owner; }
    
    /**
     * Retorna o esforço estimado em horas.
     * @return horas estimadas
     */
    public int getEstimatedEffort() { return estimatedEffort; }

    /**
     * Retorna o total de tarefas criadas no sistema.
     * @return número total de tarefas
     */
    public static int getTotalTasksCreated() {
        return totalTasksCreated;
    }

    /**
     * Retorna o total de erros de validação ocorridos.
     * @return número de erros
     */
    public static int getTotalValidationErrors() {
        return totalValidationErrors;
    }

    /**
     * Incrementa o total de erros de validação ocorridos no sistema.
     */
    public static void incrementValidationErrors() {
        totalValidationErrors++;
    }

    /**
     * Retorna a carga de trabalho ativa (em horas).
     * @return horas em progresso
     */
    public static int getActiveWorkload() {
        return activeWorkload;
    }
}