package com.senpiper.voicetask.service;

import com.senpiper.voicetask.dto.AnalyticsDTO;
import com.senpiper.voicetask.dto.TaskUpdateRequest;
import com.senpiper.voicetask.model.Task;
import com.senpiper.voicetask.model.TaskStatus;
import com.senpiper.voicetask.model.User;
import com.senpiper.voicetask.repository.TaskRepository;
import com.senpiper.voicetask.repository.UserRepository;
import com.senpiper.voicetask.util.VoiceParser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final VoiceParser voiceParser;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Task createFromVoice(String voiceText, String username) {
        User user = getUser(username);
        Task task = voiceParser.parse(voiceText);
        task.setUser(user);
        return taskRepository.save(task);
    }

    public List<Task> getAllTasks(String username) {
        User user = getUser(username);
        List<Task> tasks = taskRepository.findByUser(user);
        // Auto-mark overdue pending tasks as DELAYED
        tasks.forEach(t -> {
            if (t.getStatus() == TaskStatus.PENDING &&
                    t.getDueDate() != null &&
                    t.getDueDate().isBefore(LocalDateTime.now())) {
                t.setStatus(TaskStatus.DELAYED);
                taskRepository.save(t);
            }
        });
        return tasks;
    }

    public Task updateStatus(Long taskId, TaskUpdateRequest request, String username) {
        User user = getUser(username);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized");

        task.setStatus(request.getStatus());
        if (request.getStatus() == TaskStatus.COMPLETED)
            task.setCompletedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId, String username) {
        User user = getUser(username);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized");
        taskRepository.delete(task);
    }

    public AnalyticsDTO getAnalytics(String username) {
        User user = getUser(username);
        List<Task> tasks = taskRepository.findByUser(user);

        long total = tasks.size();
        long completedOnTime = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED
                        && t.getCompletedAt() != null
                        && t.getDueDate() != null
                        && t.getCompletedAt().isBefore(t.getDueDate()))
                .count();
        long pending = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING).count();
        long delayed = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DELAYED).count();
        long cancelled = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.CANCELLED).count();

        double completionRate = total == 0 ? 0 :
                Math.round((completedOnTime * 100.0 / total) * 10.0) / 10.0;

        // Weekly created tasks (last 7 days)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE");
        Map<String, Long> weekly = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = LocalDateTime.now().minusDays(i);
            String label = day.format(fmt);
            long count = tasks.stream()
                    .filter(t -> t.getCreatedAt() != null &&
                            t.getCreatedAt().toLocalDate().equals(day.toLocalDate()))
                    .count();
            weekly.put(label, count);
        }

        return new AnalyticsDTO(total, completedOnTime, pending, delayed, cancelled,
                completionRate, weekly);
    }
}