package com.cydeo.service.impl;

import com.cydeo.dto.ProjectDTO;
import com.cydeo.dto.TaskDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.Project;
import com.cydeo.entity.Task;
import com.cydeo.entity.User;
import com.cydeo.enums.Status;
import com.cydeo.mapper.MapperUtil;
import com.cydeo.repo.TaskRepository;
import com.cydeo.service.TaskService;
import com.cydeo.service.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final MapperUtil mapperUtil;
    private final UserService userService;

    public TaskServiceImpl(TaskRepository taskRepository, MapperUtil mapperUtil, UserService userService) {
        this.taskRepository = taskRepository;
        this.mapperUtil = mapperUtil;
        this.userService = userService;
    }

    @Override
    public List<TaskDTO> listAllTasks() {
        return taskRepository.findAll(Sort.by("assignedEmployee")).stream()
                .map(task -> mapperUtil.convert(task, new TaskDTO()))
                .collect(Collectors.toList());
    }

    @Override
    public TaskDTO findById(Long taskId) {
        return mapperUtil.convert(taskRepository.findById(taskId),new TaskDTO());
    }

    @Override
    public void save(TaskDTO taskDTO) {
        taskDTO.setTaskStatus(Status.OPEN);
        taskDTO.setAssignedDate(LocalDate.now());
        Task task = mapperUtil.convert(taskDTO, new Task());
        taskRepository.save(task);
    }

    @Override
    public void update(TaskDTO taskDTO) {
        Optional<Task> task = taskRepository.findById(taskDTO.getId());
        Task convertedTask = mapperUtil.convert(taskDTO, new Task());
        if (task.isPresent()){
            convertedTask.setTaskStatus(
                    taskDTO.getTaskStatus()==null ? task.get().getTaskStatus() : taskDTO.getTaskStatus());
            convertedTask.setAssignedDate(task.get().getAssignedDate());
            taskRepository.save(convertedTask);
        }

    }

    @Override
    public void delete(Long taskId) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isPresent()){
            task.get().setIsDeleted(true);
            taskRepository.save(task.get());
        }
    }

    @Override
    public int totalNonCompletedTasks(String projectCode) {
        return taskRepository.totalNonCompletedTasks(projectCode);
    }

    @Override
    public int totalCompletedTasks(String projectCode) {
        return taskRepository.totalCompletedTasks(projectCode);
    }

    @Override
    public void deleteByProject(ProjectDTO projectDTO) {
        Project project = mapperUtil.convert(projectDTO, new Project());
        List<Task> tasks = taskRepository.findAllByProject(project);
        tasks.forEach(task -> delete(task.getId()));
    }

    @Override
    public void completeByProject(ProjectDTO projectDTO) {
        Project project = mapperUtil.convert(projectDTO, new Project());
        List<Task> tasks = taskRepository.findAllByProject(project);
        tasks.stream().map(task -> mapperUtil.convert(task, new TaskDTO())).forEach(taskDTO -> {
                taskDTO.setTaskStatus(Status.COMPLETE);
                update(taskDTO);
        });
    }

    @Override
    public List<TaskDTO> listAllTasksByStatusIsNot(Status status) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO loggedInUser = userService.findByUserName(username);
        List<Task> tasks = taskRepository.findAllByTaskStatusIsNotAndAssignedEmployee(status,
                mapperUtil.convert(loggedInUser, new User()));
        return tasks.stream()
                .map(task -> mapperUtil.convert(task, new TaskDTO()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> listAllTasksByStatus(Status status) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO loggedInUser = userService.findByUserName(username);
        List<Task> tasks = taskRepository.findAllByTaskStatusAndAssignedEmployee(status,
                mapperUtil.convert(loggedInUser, new User()));
        return tasks.stream()
                .map(task -> mapperUtil.convert(task, new TaskDTO()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> listAllNonCompletedByAssignedEmployee(UserDTO assignedEmployee) {
        List<Task> tasks = taskRepository
                .findAllByTaskStatusIsNotAndAssignedEmployee(Status.COMPLETE, mapperUtil.convert(assignedEmployee, new User()));
        return tasks.stream().map(task -> mapperUtil.convert(task, new TaskDTO())).collect(Collectors.toList());
    }
}
