package com.rainbowforest.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.service.UserService;
import org.junit.jupiter.api.Test; // JUnit 5
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests { // JUnit 5 không cần public class và không cần @RunWith

    private final Long USER_ID = 2L;
    private final String USER_NAME = "test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Dùng trực tiếp ObjectMapper của Spring

    @MockBean
    private UserService userService;

    @Test
    void get_all_users_controller_should_return200_when_validRequest() throws Exception {
        //given
        User user = new User();
        user.setId(USER_ID);
        user.setUserName(USER_NAME);
        List<User> users = new ArrayList<>();
        users.add(user);

        //when
        when(userService.getAllUsers()).thenReturn(users);

        //then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Dùng chuẩn mới
                .andExpect(jsonPath("$[0].id").value(USER_ID))
                .andExpect(jsonPath("$[0].userName").value(USER_NAME));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void get_all_users_controller_should_return404_when_userList_isEmpty() throws Exception {
        //given
        List<User> emptyUsers = new ArrayList<>();
        
        //when
        when(userService.getAllUsers()).thenReturn(emptyUsers);

        //then
        mockMvc.perform(get("/users"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void get_user_by_name_controller_should_return200_when_users_isExist() throws Exception {
        //given
        User user = new User();
        user.setId(USER_ID);
        user.setUserName(USER_NAME);

        //when
        when(userService.getUserByName(USER_NAME)).thenReturn(user);

        //then
        mockMvc.perform(get("/users").param("name", USER_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.userName").value(USER_NAME));
    }

    @Test
    void get_user_by_id_controller_should_return200_when_users_isExist() throws Exception {
        //given
        User user = new User();
        user.setId(USER_ID);
        user.setUserName(USER_NAME);

        //when
        when(userService.getUserById(USER_ID)).thenReturn(user);

        //then
        mockMvc.perform(get("/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.userName").value(USER_NAME));
    }

    @Test
    void get_user_by_id_controller_should_return404_when_users_is_notExist() throws Exception {
        //when
        when(userService.getUserById(USER_ID)).thenReturn(null); // Fix logic: Phải trả về null mới ra 404

        //then
        mockMvc.perform(get("/users/{id}", USER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void add_user_controller_should_return201_when_user_is_saved() throws Exception {
        //given
        User user = new User();
        user.setUserName(USER_NAME);
        String requestJson = objectMapper.writeValueAsString(user);

        //when
        when(userService.saveUser(any(User.class))).thenReturn(user);

        //then
        mockMvc.perform(post("/users")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value(USER_NAME));
    }

    @Test
    void add_user_controller_should_return400_when_user_isNull() throws Exception {
        //then
        mockMvc.perform(post("/users")
                        .content("") // Gửi body trống
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}