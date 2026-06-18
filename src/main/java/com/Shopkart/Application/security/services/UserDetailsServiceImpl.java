package com.Shopkart.Application.security.services;

import com.Shopkart.Application.model.User;
import com.Shopkart.Application.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username)
                .orElseThrow(()->new UsernameNotFoundException("User not found with username: " + username));

        return UserDetailsImpl.build(user);
    }
}
