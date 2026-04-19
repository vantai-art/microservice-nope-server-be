package com.rainbowforest.userservice.service;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.entity.UserRole;
import com.rainbowforest.userservice.repository.UserRepository;
import com.rainbowforest.userservice.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    // ==================== USER ====================

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public User saveUser(User user) {
        user.setActive(1);
        // FIX: Tự động tạo ROLE_USER nếu chưa tồn tại trong DB
        // (thay vì throw exception khiến /registration trả về 500)
        UserRole role = userRoleRepository.findUserRoleByRoleName("ROLE_USER");
        if (role == null) {
            role = new UserRole();
            role.setRoleName("ROLE_USER");
            role = userRoleRepository.save(role);
        }
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public User saveUserWithRole(User user, String roleName) {
        user.setActive(1);
        // FIX: Tự động tạo role nếu chưa tồn tại
        UserRole role = userRoleRepository.findUserRoleByRoleName(roleName);
        if (role == null) {
            role = new UserRole();
            role.setRoleName(roleName);
            role = userRoleRepository.save(role);
        }
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User newData) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user id: " + id));
        if (newData.getUserName() != null)
            existing.setUserName(newData.getUserName());
        if (newData.getUserPassword() != null)
            existing.setUserPassword(newData.getUserPassword());
        if (newData.getUserDetails() != null)
            existing.setUserDetails(newData.getUserDetails());
        return userRepository.save(existing);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new RuntimeException("Không tìm thấy user id: " + id);
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getUsersByRole(String roleName) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && roleName.equals(u.getRole().getRoleName()))
                .collect(Collectors.toList());
    }

    // ==================== ROLE ====================

    @Override
    public UserRole saveRole(UserRole role) {
        return userRoleRepository.save(role);
    }

    @Override
    public List<UserRole> getAllRoles() {
        return userRoleRepository.findAll();
    }

    @Override
    public UserRole getRoleByName(String roleName) {
        return userRoleRepository.findUserRoleByRoleName(roleName);
    }
}