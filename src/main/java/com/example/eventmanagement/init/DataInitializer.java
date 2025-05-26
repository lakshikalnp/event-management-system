package com.example.eventmanagement.init;

import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.enumeration.Role;
import com.example.eventmanagement.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init(){
        List<InitUser> initUsers = List.of(
                new InitUser("admin", "admin@gmail.com", "123", Role.ADMIN),
                new InitUser("lakshika", "lakshika@gmail.com", "123456", Role.USER)
        );

        for(InitUser user : initUsers){
            if(this.userRepository.findByEmail(user.getEmail()).isEmpty()){
                User userEntity = new User();
                userEntity.setEmail(user.getEmail());
                userEntity.setName(user.getName());
                userEntity.setRole(user.getRole());
                userEntity.setPassword(this.passwordEncoder.encode(user.getPassword()));
                this.userRepository.save(userEntity);
            }
        }
    }
}

@AllArgsConstructor
@Getter
class InitUser{
    private String name;
    private String email;
    private String password;
    private Role role;
}

