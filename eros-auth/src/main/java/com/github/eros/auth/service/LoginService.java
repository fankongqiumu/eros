package com.github.eros.auth.service;

import com.github.eros.auth.manager.UserManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    private UserMnager userManager;
}
