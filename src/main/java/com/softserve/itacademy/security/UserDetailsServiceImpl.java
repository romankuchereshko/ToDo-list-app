package com.softserve.itacademy.security;

import com.softserve.itacademy.model.User;
import com.softserve.itacademy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service("userDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) /*throws UsernameNotFoundException*/ {
        User user = userRepository.findByEmail(email);
//        if(user == null)
//            throw new UsernameNotFoundException("No such user with given email");
        return SecurityUser.frontUser(user);
    }
}
