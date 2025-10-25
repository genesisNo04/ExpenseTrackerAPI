package com.example.ExpenseTrackerAPI.Controller;

import com.example.ExpenseTrackerAPI.DTO.AuthResponseDTO;
import com.example.ExpenseTrackerAPI.DTO.AuthUserDTO;
import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import com.example.ExpenseTrackerAPI.Enum.Role;
import com.example.ExpenseTrackerAPI.Service.AppUserService;
import com.example.ExpenseTrackerAPI.Service.AuthUserService;
import com.example.ExpenseTrackerAPI.Config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/user")
public class AuthUserController {

    @Autowired
    private AuthUserService authUserService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerUser(@RequestBody AuthUserDTO authUserDTO) {
        AppUser newUser = new AppUser();
        newUser.setUsername(authUserDTO.getUsername());
        appUserService.saveUser(newUser);

        AuthUser authUser = new AuthUser();
        authUser.setUsername(authUserDTO.getUsername());
        authUser.setPassword(authUserDTO.getPassword());
        authUser.setEmail(authUserDTO.getEmail());
        authUser.setRole(Role.ROLE_USER);
        authUser.setAppUser(newUser);

        authUserService.registerUser(authUser);

        String accessToken = jwtUtil.generateToken(authUser.getUsername(), authUser.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDTO(accessToken));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(@RequestBody AuthUserDTO authUserDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authUserDTO.getUsername(), authUserDTO.getPassword()));

        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        String accessToken = jwtUtil.generateToken(authUser.getUsername(), authUser.getRole());
        return ResponseEntity.ok(new AuthResponseDTO(accessToken));
    }
}
