package com.rainbowforest.userservice.service;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Chuẩn JUnit 5 để dùng Mockito
class UserServiceTests {

    private final Long USER_ID = 2L;
    private final String USER_NAME = "test";
    private User user;
    private List<User> userList;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach // Thay cho @Before
    void setUp(){
        user = new User();
        user.setId(USER_ID);
        user.setUserName(USER_NAME);
        userList = new ArrayList<>();
        userList.add(user);
    }

    @Test
    void get_all_users_test(){
        // given
        when(userRepository.findAll()).thenReturn(userList);

        // when
        List<User> foundUsers = userService.getAllUsers();

        // then
        assertEquals(foundUsers.get(0).getUserName(), USER_NAME);
        Mockito.verify(userRepository, Mockito.times(1)).findAll();
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void get_user_by_id_test(){
        // Trong Spring Boot 3/JPA mới, getOne() đã bị thay thế bằng getReferenceById() hoặc dùng findById()
        // Ở đây mình dùng findById (trả về Optional) vì nó an toàn hơn
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // when
        User foundUser = userService.getUserById(USER_ID);

        // then
        assertEquals(foundUser.getUserName(), USER_NAME);
        Mockito.verify(userRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void get_user_by_name_test(){
        // given
        when(userRepository.findByUserName(anyString())).thenReturn(user);

        // when
        User foundUser = userService.getUserByName(USER_NAME);

        // then
        assertEquals(foundUser.getId(), USER_ID);
        Mockito.verify(userRepository, Mockito.times(1)).findByUserName(USER_NAME);
        Mockito.verifyNoMoreInteractions(userRepository);
    }
}