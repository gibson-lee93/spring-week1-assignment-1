package com.codesoom.assignment;

import com.codesoom.assignment.models.HttpMethod;
import com.codesoom.assignment.models.HttpStatus;
import com.codesoom.assignment.models.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.codesoom.assignment.models.HttpMethod.GET;
import static com.codesoom.assignment.models.HttpMethod.POST;
import static com.codesoom.assignment.models.HttpMethod.PUT;
import static com.codesoom.assignment.models.HttpMethod.PATCH;
import static com.codesoom.assignment.models.HttpMethod.DELETE;

public class TodoHttpHandler implements HttpHandler {
    private List<Task> tasks = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    public TodoHttpHandler() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Do nothing...");

        tasks.add(task);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpMethod method = HttpMethod.valueOf(exchange.getRequestMethod());
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        InputStream inputStream = exchange.getRequestBody();
        String body = new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));

        System.out.printf("%s %s %s", method, path, System.lineSeparator());

        if (!body.isBlank()) {
            System.out.println(body);
        }

        String content = "Hello, World!";

        if (GET.equals(method) && path.equals("/tasks")) {
            content = tasksToJSON();
        }

        if (GET.equals(method) && path.split("/")[1].equals("tasks") && path.split("/").length > 2) {
            Long id = Long.valueOf(path.split("/")[2]);

            content = taskToJSON(id);
        }

        if (POST.equals(method) && path.equals("/tasks")) {
            content = "Create a new task.";

            Task task = toTask(body);
            Long nextId = tasks.get(tasks.size() - 1).getId() + 1;
            task.setId(nextId);

            tasks.add(task);
            System.out.println(task);
        }

        if (PUT.equals(method) && path.split("/")[1].equals("tasks") && path.split("/").length > 2) {
            Long id = Long.valueOf(path.split("/")[2]);

            Task task = toTask(body);
            task.setId(id);

            content = taskToChange(task);
        }

        if (PATCH.equals(method) && path.split("/")[1].equals("tasks") && path.split("/").length > 2) {
            Long id = Long.valueOf(path.split("/")[2]);

            Task task = toTask(body);
            task.setId(id);

            content = taskToChange(task);
        }

        if (DELETE.equals(method) && path.split("/")[1].equals("tasks") && path.split("/").length > 2) {
            Long id = Long.valueOf(path.split("/")[2]);

            content = taskToRemove(id);
        }

        exchange.sendResponseHeaders(HttpStatus.OK.getCode(), content.getBytes().length);

        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(content.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    private String taskToRemove(Long id) {
        tasks.removeIf(task -> task.getId().equals(id));
        return "Delete a task.";
    }

    private String taskToChange(Task task) {
        tasks.get(task.getId().intValue() - 1).setTitle(task.getTitle());
        return "Update a task.";
    }

    private Task toTask(String content) throws JsonProcessingException {
        return objectMapper.readValue(content, Task.class);
    }

    private String taskToJSON(Long id) throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, tasks
                .stream()
                .filter(task -> task.getId().equals(id))
                .collect(Collectors.toList()));

        return outputStream.toString();
    }

    private String tasksToJSON() throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, tasks);

        return outputStream.toString();
    }
}