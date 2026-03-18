package com.nexus.service;

import com.nexus.model.Task;
import com.nexus.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Workspace {
    private final List<Task> tasks = new ArrayList<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

    public List<Task> getTasks() {
        // Retorna uma visão não modificável para garantir encapsulamento
        return Collections.unmodifiableList(tasks);
    }

    public List<User> topPerformers(List<User> users) {
        return users.stream()
            .sorted(Comparator.comparing(User::getCountDoneTasks))
            .limit(3)
            .collect(Collectors.toList());
    }

    public List<User> overloadedUsers(List<User> users) {
        return users.stream()
            .filter(e -> e.getCountInProgressTasks() > 10)
            .collect(Collectors.toList());
    }

    public float projectHealth() { 
        return (float) 1;
    }

    public String globalBottlenecks() {
        return "TO-DO";
    }
}