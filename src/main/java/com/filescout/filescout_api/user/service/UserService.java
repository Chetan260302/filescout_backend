package com.filescout.filescout_api.user.service;

import java.util.List;

import com.filescout.filescout_api.user.entity.User;

public interface UserService {
    
    User createUser(User user);

    List<User> getAllUsers();

}
