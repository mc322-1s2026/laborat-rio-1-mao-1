package com.nexus.service;

import com.nexus.model.*;
import com.nexus.exception.NexusValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class LogProcessor {

    public void processLog(String fileName, Workspace workspace, List<User> users) {
        try {
            // Busca o arquivo dentro da pasta de recursos do projeto (target/classes)
            var resource = getClass().getClassLoader().getResourceAsStream(fileName);
            
            if (resource == null) {
                throw new IOException("Arquivo não encontrado no classpath: " + fileName);
            }

            try (java.util.Scanner s = new java.util.Scanner(resource).useDelimiter("\\A")) {
                String content = s.hasNext() ? s.next() : "";
                List<String> lines = List.of(content.split("\\R"));
                
                for (String line : lines) {
                    if (line.isBlank() || line.startsWith("#")) continue;

                    String[] p = line.split(";");
                    String action = p[0];

                    try {
                        switch (action) {
                            case "CREATE_USER" -> {
                                users.add(new User(p[1], p[2]));
                                System.out.println("[LOG] Usuário criado: " + p[1]);
                            }
                            case "CREATE_TASK" -> {
                                Task t = new Task(p[1], LocalDate.parse(p[2]), Integer.parseInt(p[3]));
                                workspace.addTask(t);
                                System.out.println("[LOG] Tarefa criada: " + p[1]);
                            }
                            case "ASSIGN_USER" -> {
                                int taskId = Integer.parseInt(p[1]);
                                String username = p[2];

                                Task task = workspace.getTasks().stream()
                                    .filter(t -> t.getId() == taskId)
                                    .findFirst()
                                    .orElseThrow(() -> new NexusValidationException("Tarefa não encontrada: " + taskId));

                                User owner = users.stream()
                                    .filter(u -> Objects.equals(u.consultUsername(), username))
                                    .findFirst()
                                    .orElseThrow(() -> new NexusValidationException("Usuário não encontrado: " + username));

                                task.assignOwner(owner);
                                owner.assignTask(task);
                                System.out.println("[LOG] Owner atribuído para task " + taskId + ": " + username);
                            }
                            case "CHANGE_STATUS" -> {
                                int taskId = Integer.parseInt(p[1]);
                                String status = p[2];

                                Task task = workspace.getTasks().stream()
                                    .filter(t -> t.getId() == taskId)
                                    .findFirst()
                                    .orElseThrow(() -> new NexusValidationException("Tarefa não encontrada: " + taskId));

                                switch(status) {
                                    case "IN_PROGRESS" -> { task.moveToInProgress(task.getOwner()); }
                                    case "DONE" -> { task.markAsDone(); }
                                    case "BLOCKED" -> { task.setBlocked(); }
                                    default -> System.err.println("[WARN] Status desconhecido: " + status);
                                }
                            }
                            case "REPORT_STATUS" -> {
                                System.out.println("Top Performers:");
                                List<User> top = workspace.topPerformers(users);
                                for(int i = 0; i < top.size(); i++) {
                                    User u = top.get(i);
                                    System.out.printf("%d - %s%n", i+1, u.consultUsername());
                                }

                                System.out.println("Overloaded Users:");
                                List<User> over = workspace.overloadedUsers(users);
                                for(int i = 0; i < over.size(); i++) {
                                    User u = over.get(i);
                                    System.out.printf("%d - %s%n", i+1, u.consultUsername());
                                }

                                System.out.println("Project Health:");
                                float health = workspace.projectHealth();
                                System.out.printf("%f%n", health);

                                System.out.println("Global Bottlenecks:");
                                String bottleneck = workspace.globalBottlenecks();
                                System.out.printf("%s%n", bottleneck);
                            }
                            default -> System.err.println("[WARN] Ação desconhecida: " + action);
                        }
                    } catch (NexusValidationException e) {
                        System.err.println("[ERRO DE REGRAS] Falha no comando '" + line + "': " + e.getMessage());
                    } catch (RuntimeException e) {
                        System.err.println("[ERRO DE FORMATO] Falha no comando '" + line + "': " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERRO FATAL] " + e.getMessage());
        }
    }
}