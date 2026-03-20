package com.nexus.model;

import java.util.ArrayList;
import java.util.List;
import com.nexus.exception.NexusValidationException;


public class Project{

    private final String name;
    private final List<Task> taskList;
    private final int totalBudget;
    private int currentBudget;

    public Project(String name, int totalBudget) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome não pode ser vazio.");
        }
        if (totalBudget <= 0) {
            throw new IllegalArgumentException("Orçamento precisa ser maior que 0.");
        }

        this.name = name;
        this.totalBudget = totalBudget;
        this.taskList = new ArrayList<>();
        this.currentBudget = 0;
    }

    /** Retorna o nome do projeto. */
    public String consultName() { return name; }
    
    /** Retorna o orçamento total do projeto. */
    public int consultTotalBudget() { return totalBudget; }
    
    /** Retorna a quantidade atual de horas do projeto. */
    public int consultCurrentBudget() { return this.currentBudget; }


    /**
     * Adiciona uma nova tarefa ao projeto.
     * Regra: A soma das horas atuais do projeto mais as da tarefa devem ser inferiores
     * ao orçamento total.
     */
    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Tarefa não pode ser nula.");
        }

        int taskEffort = task.getEstimatedEffort();

        if (!this.taskList.contains(task)) {
            if (this.currentBudget + taskEffort > this.totalBudget) {
                throw new NexusValidationException("A adição da tarefa supera o orçamento do projeto.");
            }
            else {
                currentBudget += taskEffort;
                this.taskList.add(task);
            }
        }
    }

    /** 
     * Retorna a lista de tarefas do projeto.
     */
    public List<Task> getAllTasks() {
        return List.copyOf(this.taskList);
    }
}