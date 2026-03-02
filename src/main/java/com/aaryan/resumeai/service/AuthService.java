package com.aaryan.resumeai.service;

import com.aaryan.resumeai.entity.User;
import com.aaryan.resumeai.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public String register(String username, String password, String role) {

        // check if user exists
        if (userRepository.findByUsername(username).isPresent()) {
            return "User already exists";
        }

        // create new user
        User user = new User();

        user.setUsername(username);

        // VERY IMPORTANT
        user.setPassword(passwordEncoder.encode(password));

        user.setRole(role);

        userRepository.save(user);

        return "User registered successfully";
    }

}