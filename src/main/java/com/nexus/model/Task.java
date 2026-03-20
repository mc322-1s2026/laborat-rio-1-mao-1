package com.nexus.model;

import com.nexus.exception.NexusValidationException;
import java.time.LocalDate;

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
     * Define um usuário para a tarefa.
     * Regra: O usuário não pode ser nulo.
     */
    public void assignOwner(User owner) {
        if (owner == null) {
            incrementValidationErrors();
            throw new NexusValidationException("Owner não pode ser nulo.");
        }
        this.owner = owner;
    }

    /**
     * Move a tarefa para IN_PROGRESS.
     * Regra: Só é possível se houver um owner atribuído e não estiver BLOCKED.
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
     * Finaliza a tarefa.
     * Regra: Só pode ser movida para DONE se não estiver BLOCKED.
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
     * Bloqueia a tarefa.
     * Regra: A tarefa deve ser movida para BLOCKED se não estiver DONE.
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

    /** Retorna o ID da tarefa */
    public int getId() { return id; }
    /** Retorna o status atual */
    public TaskStatus getStatus() { return status; }
    /** Retorna o título */
    public String getTitle() { return title; }
    /** Retorna o prazo final */
    public LocalDate getDeadline() { return deadline; }
    /** Retorna o dono da tarefa */
    public User getOwner() { return owner; }
    /** Retorna o esforço estimado em horas */
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