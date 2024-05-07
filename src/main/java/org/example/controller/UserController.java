//package org.example.controller;
//
//import org.example.dto.UserDto;
//import org.example.service.AuthService;
//import org.example.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletResponse;
//
//// Endpoints for registering, logging in, and accessing the profile.
//
//@RestController
//@RequestMapping("/api")
//public class UserController {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private AuthService authService;
//
//    @PostMapping("/register")
//    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto, HttpServletResponse response) {
//        UserDto registeredUser = userService.registerUser(userDto);
//        String token = authService.generateToken(registeredUser.getUsername());
//        response.setHeader("aat", token);
//        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<UserDto> loginUser(@RequestBody UserDto userDto, HttpServletResponse response) {
//        userService.getUserByUsername(userDto.getUsername()).ifPresent(user -> {
//            String token = authService.generateToken(user.getUsername());
//            response.setHeader("aat", token);
//        });
//        return new ResponseEntity<>(userDto, HttpStatus.OK);
//    }
//
//    @GetMapping("/profile")
//    public ResponseEntity<String> getProfile(@RequestHeader("aat") String token) {
//        try {
//            String username = authService.validateTokenAndGetUsername(token);
//            return ResponseEntity.ok("Profile data for " + username);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token");
//        }
//    }
//}
