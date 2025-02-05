package com.server.app.service;

import com.server.app.model.User;
import com.server.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RedisService redisService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to get user from Redis cache first
        User user = redisService.get(username, User.class);
        if (user != null) {
            log.debug("User found in cache: {}", username);
            return user;
        }

        // If not in cache, get from database
        log.debug("User not found in cache, querying database: {}", username);
        return userRepository.findByEmail(username)
                .map(foundUser -> {
                    // Cache the user for future requests
                    redisService.set(username, foundUser);
                    return foundUser;
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}