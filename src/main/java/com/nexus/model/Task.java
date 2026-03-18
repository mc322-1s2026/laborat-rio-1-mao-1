package com.nexus.model;

import com.nexus.exception.NexusValidationException;
import java.time.LocalDate;

public class Task {
    public static int totalTasksCreated = 0;
    public static int totalValidationErrors = 0;
    public static int activeWorkload = 0;

    private static int nextId = 1;

    private int id;
    private LocalDate deadline; 
    private String title;
    private TaskStatus status;
    private User owner;
    private int estimatedEffort;

    public Task(String title, LocalDate deadline, int effort) {
        this.id = nextId++;
        this.deadline = deadline;
        this.title = title;
        this.status = TaskStatus.TO_DO;
        this.owner = null;
        this.estimatedEffort = effort;
        
        totalTasksCreated++; 
    }

    public void assignOwner(User owner) {
        if (owner == null) {
            throw new NexusValidationException("Owner não pode ser nulo.");
        }
        this.owner = owner;
    }

    /**
     * Move a tarefa para IN_PROGRESS.
     * Regra: Só é possível se houver um owner atribuído e não estiver BLOCKED.
     */
    public void moveToInProgress(User user) {
        if(user != null && this.status != TaskStatus.BLOCKED) {
            this.status = TaskStatus.IN_PROGRESS;
            activeWorkload += this.estimatedEffort;
        }
        else {
            totalValidationErrors++;
            throw new NexusValidationException("Task bloqueada ou sem dono.");
        }
    }

    /**
     * Finaliza a tarefa.
     * Regra: Só pode ser movida para DONE se não estiver BLOCKED.
     */
    public void markAsDone() {
        if(this.status != TaskStatus.BLOCKED) {
            this.status = TaskStatus.DONE;
            activeWorkload -= this.estimatedEffort;
        }
        else {
            totalValidationErrors++;
            throw new NexusValidationException("Task bloqueada.");
        }
    }

    public void setBlocked() {
        if (this.status != TaskStatus.DONE) {
            if(this.status == TaskStatus.BLOCKED) {
                //this.status = TaskStatus.TO_DO;
            }
            else {
                if (this.status == TaskStatus.IN_PROGRESS) {
                    activeWorkload -= this.estimatedEffort;
                } 
                this.status = TaskStatus.BLOCKED; 
            }
        }
    }

    // Getters
    public int getId() { return id; }
    public TaskStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public LocalDate getDeadline() { return deadline; }
    public User getOwner() { return owner; }
    public int getEstimatedEffort() { return estimatedEffort; }
}