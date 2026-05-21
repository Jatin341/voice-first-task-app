package com.senpiper.voicetask.repository;

import com.senpiper.voicetask.model.Task;
import com.senpiper.voicetask.model.TaskStatus;
import com.senpiper.voicetask.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);
    List<Task> findByUserAndStatus(User user, TaskStatus status);
}