package com.cydeo.service.impl;

import com.cydeo.dto.ProjectDTO;
import com.cydeo.dto.TaskDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.User;
import com.cydeo.mapper.MapperUtil;
import com.cydeo.repo.UserRepository;
import com.cydeo.service.ProjectService;
import com.cydeo.service.TaskService;
import com.cydeo.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final MapperUtil mapperUtil;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, MapperUtil mapperUtil, @Lazy ProjectService projectService, @Lazy TaskService taskService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mapperUtil = mapperUtil;
        this.projectService = projectService;
        this.taskService = taskService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserDTO> listAllUsers() {
        return userRepository.findAllByIsDeletedOrderByFirstNameDesc(false).stream()
                .map(user -> mapperUtil.convert(user, new UserDTO()))
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO findByUserName(String username) {
        return mapperUtil.convert(userRepository.findByUserNameAndIsDeleted(username, false), new UserDTO());
    }

    @Override
    public void save(UserDTO userDTO) {
        User user = mapperUtil.convert(userDTO, new User());
        user.setPassWord(passwordEncoder.encode(user.getPassWord()));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public UserDTO update(UserDTO user) {

        //Find current user
        User user1 = userRepository.findByUserNameAndIsDeleted(user.getUserName(), false);  //has id
        //Map update user dto to entity object
        User convertedUser = mapperUtil.convert(user, new User());   // has id?
        //set id to the converted object
        convertedUser.setId(user1.getId());
        //save the updated user in the db
        userRepository.save(convertedUser);

        return findByUserName(user.getUserName());

    }


    //just change the BaseEntity's field isDeleted to true, and save the user
    @Override
    public void delete(String username) {
        User user = userRepository.findByUserNameAndIsDeleted(username, false);
        if (checkIfUserCanBeDeleted(user)){
            user.setIsDeleted(true);
            user.setUserName(user.getUserName() + "-" + user.getId());          //harold@manager-2
            userRepository.save(user);
        }
    }

    private boolean checkIfUserCanBeDeleted(User user){
        switch (user.getRole().getDescription()){
            case "Manager":
                List<ProjectDTO> projectDTOList = projectService.listAllNonCompletedByAssignedManager(mapperUtil.convert(user, new UserDTO()));
                return projectDTOList.size() == 0;
            case "Employee":
                List<TaskDTO> taskDTOList = taskService.listAllNonCompletedByAssignedEmployee(mapperUtil.convert(user, new UserDTO()));
                return taskDTOList.size() == 0;
            default:
                return true;
        }
    }

    @Override
    public List<UserDTO> listAllByRole(String role) {
        return userRepository.findByRoleDescriptionIgnoreCaseAndIsDeleted(role, false).stream()
                .map(user -> mapperUtil.convert(user, new UserDTO()))
                .collect(Collectors.toList());
    }

}
