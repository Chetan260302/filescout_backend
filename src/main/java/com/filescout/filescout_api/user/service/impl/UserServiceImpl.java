package com.filescout.filescout_api.user.service.impl;

import com.filescout.filescout_api.user.entity.User;
import com.filescout.filescout_api.user.repository.UserRepository;
import com.filescout.filescout_api.user.service.UserService;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
