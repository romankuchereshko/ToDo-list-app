package com.softserve.itacademy.controller;

import com.softserve.itacademy.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping({"/", "home"})
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
//    @PreAuthorize("hasAuthority('ADMIN')")
    public String home(Model model, Authentication auth) {
        if(authenticationContainsAuthority(auth, "USER"))
            return "redirect:/todos/all/users/" + userService.readByEmail(auth.getName()).getId();
        model.addAttribute("users", userService.getAll());
        if (isAuthenticated()) {
            return "home";
        }
        return "login-page";
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.
                isAssignableFrom(authentication.getClass())) {
            return false;
        }
        return authentication.isAuthenticated();
    }
    private boolean authenticationContainsAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream().filter(auth -> auth.getAuthority().equalsIgnoreCase(authority)).findAny().orElse(null) != null;
    }
}
