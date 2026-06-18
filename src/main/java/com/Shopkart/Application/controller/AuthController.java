package com.Shopkart.Application.controller;

import com.Shopkart.Application.model.AppRole;
import com.Shopkart.Application.model.Role;
import com.Shopkart.Application.model.User;
import com.Shopkart.Application.repository.RoleRepository;
import com.Shopkart.Application.repository.UserRepository;
import com.Shopkart.Application.security.jwt.JwtUtils;
import com.Shopkart.Application.security.request.LoginRequest;
import com.Shopkart.Application.security.request.SignupRequest;
import com.Shopkart.Application.security.response.MessageResponse;
import com.Shopkart.Application.security.response.UserInfoResponse;
import com.Shopkart.Application.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.header.Header;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/auth")
public class    AuthController {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){

        Authentication authentication;

        try{
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword())
            );
        }
        catch (AuthenticationException exception){
            Map<String,Object> map = new HashMap<>();
            map.put("message","Bad Credentials");
            map.put("status",false);

            return new ResponseEntity<Object>(map, HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie  = jwtUtils.generateJwtCookie(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .toList();

        UserInfoResponse response = new UserInfoResponse(userDetails.getId(),userDetails.getUsername(),jwtCookie.toString(),roles);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
        if(userRepository.existsByUserName(signupRequest.getUsername()))
            return ResponseEntity.badRequest().body(new MessageResponse("Error : Username is already taken!"));

        if(userRepository.existsByEmail(signupRequest.getEmail()))
            return ResponseEntity.badRequest().body(new MessageResponse("Error : Email is already taken!"));

        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if(strRoles == null){
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(()-> new RuntimeException("Role not found"));
            roles.add(userRole);
        }
        else{
            for(String role : strRoles){
                switch (role.toLowerCase()){
                    case "admin":
                            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                    .orElseThrow(()-> new RuntimeException("Role not found"));
                            roles.add(adminRole);
                            break;
                    case "seller":
                            Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                    .orElseThrow(()-> new RuntimeException("Role not found"));
                            roles.add(sellerRole);
                            break;
                    default:
                            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                    .orElseThrow(()-> new RuntimeException("Role not found"));
                            roles.add(userRole);
                }
            }
        }


        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword())
        );

        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered succuessfully!"));
    }


    @GetMapping("/username")
    public String currentUserName(Authentication authentication){
        if(authentication != null)
            return authentication.getName();
        else
            return "";
    }

    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> getUserDetails(Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .toList();
        UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(),roles);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signoutUser(){
        ResponseCookie cookie = jwtUtils.getCleanCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,cookie.toString())
                .body(new MessageResponse("You have been signed out!!"));
    }
}
