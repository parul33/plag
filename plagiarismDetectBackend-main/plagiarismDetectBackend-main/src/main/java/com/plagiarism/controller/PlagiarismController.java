package com.plagiarism.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plagiarism.service.PlagiarismService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/plagiarism")
public class PlagiarismController {
    @Autowired
    private PlagiarismService plagiarismService;

    @PostMapping("/check")
    public Map<String, Object> checkPlagiarism(@RequestBody String text) {
        return plagiarismService.detectPlagiarism(text);
    }
}