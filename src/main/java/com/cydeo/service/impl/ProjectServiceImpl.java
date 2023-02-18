package com.cydeo.service.impl;

import com.cydeo.dto.ProjectDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.Project;
import com.cydeo.entity.User;
import com.cydeo.enums.Status;
import com.cydeo.mapper.MapperUtil;
import com.cydeo.repo.ProjectRepository;
import com.cydeo.service.ProjectService;
import com.cydeo.service.TaskService;
import com.cydeo.service.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final MapperUtil mapperUtil;
    private final UserService userService;
    private final TaskService taskService;

    public ProjectServiceImpl(ProjectRepository projectRepository, MapperUtil mapperUtil, UserService userService, TaskService taskService) {
        this.projectRepository = projectRepository;
        this.mapperUtil = mapperUtil;
        this.userService = userService;
        this.taskService = taskService;
    }

    @Override
    public List<ProjectDTO> listAllProject() {
        return projectRepository.findAll(Sort.by("projectCode")).stream()
                .map(project -> mapperUtil.convert(project, new ProjectDTO()))
                .collect(Collectors.toList());
    }

    @Override
    public ProjectDTO findByProjectCode(String projectCode) {
        return mapperUtil.convert(projectRepository.findByProjectCode(projectCode), new ProjectDTO());
    }

    @Override
    public void save(ProjectDTO projectDTO) {
        projectDTO.setProjectStatus(Status.OPEN);
        Project project = mapperUtil.convert(projectDTO, new Project());
        projectRepository.save(project);
    }

    @Override
    public void update(ProjectDTO projectDTO) {
        Project project = projectRepository.findByProjectCode(projectDTO.getProjectCode());
        Project convertedProject = mapperUtil.convert(projectDTO, new Project());
        convertedProject.setId(project.getId());
        convertedProject.setProjectStatus(project.getProjectStatus());
        projectRepository.save(convertedProject);
    }

    @Override
    public void delete(String projectCode) {
        Project project = projectRepository.findByProjectCode(projectCode);
        project.setIsDeleted(true);
        // if project is deleted, change the project code,
        // so that later that code could be used for other new projects. Ex: "SP01"
        project.setProjectCode(project.getProjectCode() + "-" + project.getId());
        //.. becomes "SP01-1" (it doesn't have to be + project.getId(), it's just have to be smth unique)

        projectRepository.save(project);

        taskService.deleteByProject(mapperUtil.convert(project, new ProjectDTO()));
    }

    @Override
    public void complete(String projectCode) {
        Project project = projectRepository.findByProjectCode(projectCode);
        project.setProjectStatus(Status.COMPLETE);
        projectRepository.save(project);
    }

    @Override
    public List<ProjectDTO> listAllProjectDetails() {
        UserDTO currentUserDto = userService.findByUserName("harold@manager.com");
        User user = mapperUtil.convert(currentUserDto, new User());
        return projectRepository.findProjectsByAssignedManager(user).stream()
                .map(project -> {
                    ProjectDTO dto = mapperUtil.convert(project, new ProjectDTO());
                    dto.setUnfinishedTaskCounts(taskService.totalNonCompletedTasks(project.getProjectCode()));
                    dto.setCompleteTaskCounts(taskService.totalCompletedTasks(project.getProjectCode()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
