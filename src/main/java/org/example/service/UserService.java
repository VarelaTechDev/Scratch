//package org.example.service;
//
//import org.example.dto.UserDto;
//import org.example.entity.AppUser;
//import org.example.repository.AppUserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Optional;
//
//// A service to handle user registration and fetching user details.
//@Service
//public class UserService {
//    @Autowired
//    private AppUserRepository appUserRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    public UserDto registerUser(UserDto userDto) {
//        AppUser user = new AppUser();
//        user.setUsername(userDto.getUsername());
//        appUserRepository.save(user);
//        return userDto;
//    }
//
//    public Optional<AppUser> getUserByUsername(String username) {
//        return appUserRepository.findByUsername(username);
//    }
//}
