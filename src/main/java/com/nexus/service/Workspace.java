package com.nexus.service;

import com.nexus.model.Task;
import com.nexus.model.TaskStatus;
import com.nexus.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Gerenciador central do workspace no sistema Nexus.
 * 
 * Responsável por armazenar tarefas e fornecer análises como
 * avaliação de performance dos usuários, carga de trabalho e gargalos.
 */
public class Workspace {
    private final List<Task> tasks = new ArrayList<>();

    /**
     * Adiciona uma tarefa ao workspace.
     * 
     * @param task a tarefa a ser adicionada
     */
    public void addTask(Task task) {
        tasks.add(task);
    }

    /**
     * Retorna uma lista não modificável de todas as tarefas do workspace.
     * 
     * @return lista imutável de tarefas
     */
    public List<Task> getTasks() {
        // Retorna uma visão não modificável para garantir encapsulamento
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Retorna os 3 usuários com mais tarefas concluídas.
     * 
     * @param users a lista de usuários para análise
     * @return até 3 usuários com melhor desempenho
     */
    public List<User> topPerformers(List<User> users) {
        return Collections.unmodifiableList(users.stream()
            .sorted(Comparator.comparingInt(User::getCountDoneTasks).reversed())
            .limit(3)
            .collect(Collectors.toList()));
    }

    /**
     * Retorna usuários com carga excessiva (mais de 10 tarefas em progresso).
     * 
     * @param users a lista de usuários para análise
     * @return usuários sobrecarregados
     */
    public List<User> overloadedUsers(List<User> users) {
        return Collections.unmodifiableList(users.stream()
            .filter(e -> e.getCountInProgressTasks() > 10)
            .collect(Collectors.toList()));
    }

    /**
     * Calcula a saúde do projeto (valor entre 0 e 1).
     * 
     * @param project o projeto a ser analisado (0.0 a 1.0)
     * @return indicador de saúde do projeto
     */
    public float projectHealth(Project project) {
        List<Task> projectTasks = project.getAllTasks();
        if (projectTasks.isEmpty()) return 1.0f;

        long doneCount = projectTasks.stream()
            .filter(e -> e.getStatus() == TaskStatus.DONE)
            .count();

        return (float) doneCount / projectTasks.size();
    }

    /**
     * Identifica o principal gargalo do projeto (estado com mais tarefas).
     * 
     * @return o estado com maior concentração de tarefas
     */
    public String globalBottlenecks() {
        int to_do = (int) tasks.stream()
            .filter(e -> e.getStatus() == TaskStatus.TO_DO)
            .count();

        int in_progress = (int) tasks.stream()
            .filter(e -> e.getStatus() == TaskStatus.IN_PROGRESS)
            .count();

        int blocked = (int) tasks.stream()
            .filter(e -> e.getStatus() == TaskStatus.BLOCKED)
            .count();

        return java.util.Map.of(
                "TO_DO", to_do,
                "IN_PROGRESS", in_progress,
                "BLOCKED", blocked
            ).entrySet().stream()
            .max(java.util.Map.Entry.comparingByValue())
            .map(java.util.Map.Entry::getKey)
            .orElse("NONE");
    }
}