package com.rainbowforest.userservice.service;

import java.util.List;
import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.entity.UserRole;

public interface UserService {
    // User
    List<User> getAllUsers();

    User getUserById(Long id);

    User getUserByName(String userName);

    User saveUser(User user);

    User saveUserWithRole(User user, String roleName);

    User updateUser(Long id, User user);

    void deleteUser(Long id);

    List<User> getUsersByRole(String roleName);

    // Role
    UserRole saveRole(UserRole role);

    List<UserRole> getAllRoles();

    UserRole getRoleByName(String roleName);
}