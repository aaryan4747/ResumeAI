package com.aaryan.resumeai.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/home")
@CrossOrigin(origins = "*")
public class HomeController {

    @GetMapping("/welcome")
    public String welcome() {
        return "Resume AI Backend is Running Successfully 🚀";
    }
}
