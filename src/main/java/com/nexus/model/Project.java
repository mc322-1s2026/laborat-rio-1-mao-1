package com.nexus.model;

import java.util.ArrayList;
import java.util.List;
import com.nexus.exception.NexusValidationException;

/**
 * Representa um projeto no sistema Nexus.
 * 
 * Um projeto agrupa tarefas e possui um orçamento de horas limitado.
 * Não é possível adicionar tarefas que excedam o orçamento total.
 */
public class Project {

    private String name;
    private List<Task> taskList;
    private int totalBudget;
    private int currentBudget;

    /**
     * Cria um novo projeto com orçamento definido.
     * 
     * @param name o nome do projeto (não pode ser vazio)
     * @param totalBudget o orçamento total em horas (deve ser maior que 0)
     * @throws IllegalArgumentException se o nome for vazio ou orçamento inválido
     */
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

    /**
     * Retorna o nome do projeto.
     * @return o nome
     */
    public String consultName() { return name; }
    
    /**
     * Retorna o orçamento total em horas do projeto.
     * @return horas totais disponíveis
     */
    public int consultTotalBudget() { return totalBudget; }
    
    /**
     * Retorna o orçamento já utilizado em horas.
     * @return horas já alocadas
     */
    public int consultCurrentBudget() { return this.currentBudget; }


    /**
     * Adiciona uma nova tarefa ao projeto.
     * Verifica se o orçamento permite adicionar a tarefa.
     * 
     * @param task a tarefa a ser adicionada
     * @throws IllegalArgumentException se a tarefa for nula
     * @throws NexusValidationException se adicionar esta tarefa exceder o orçamento
     */
    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Tarefa não pode ser nula.");
        }

        int taskEffort = task.getEstimatedEffort();

        if (!this.taskList.contains(task)) {
            if (this.currentBudget + taskEffort > this.totalBudget) {
                Task.totalValidationErrors++;
                throw new NexusValidationException("A adição da tarefa supera o orçamento do projeto.");
            }
            else {
                currentBudget += taskEffort;
                this.taskList.add(task);
            }
        }
    }

    /**
     * Retorna uma cópia da lista de tarefas do projeto.
     * 
     * @return lista imutável de tarefas
     */
    public List<Task> getAllTasks() {
        return List.copyOf(this.taskList);
    }
}