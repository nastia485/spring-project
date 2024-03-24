package com.project.springproject.controller;

import com.project.springproject.models.*;
import com.project.springproject.services.AuthenticationService;
import com.project.springproject.services.JWTService;
import com.project.springproject.services.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//@RestController
@RequestMapping("/auth")
@Controller
//@CrossOrigin("*")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JWTService jwtService;

    @PostMapping("/register")
    public ApplicationUser registerUser(@RequestBody RegistrationDTO body) {
        return authenticationService.registerUser(body.getUsername(), body.getPassword());
    }

//    @PostMapping("/login")
//    public LoginResponseDTO loginUser(@RequestBody RegistrationDTO body){
//        return authenticationService.loginUser(body.getUsername(), body.getPassword());
//    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("authRequestDTO", new AuthRequestDTO());
        return "auth";
    }

//    @ResponseBody
//    @PostMapping("/token")
//    public AuthRequestDTO login(@ModelAttribute("authRequestDTO") AuthRequestDTO authRequestDTO){
//        System.out.println("Login submitted");
//        System.out.println(authRequestDTO);
//        return authRequestDTO;
//    }


    @ResponseBody
    @PostMapping("/token")
    public JwtResponseDTO AuthenticateAndGetToken(@ModelAttribute("authRequestDTO") AuthRequestDTO authRequestDTO, HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword()));
        if (authentication.isAuthenticated()) {
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
            String accessToken = jwtService.generateJwt(authentication);
            // set accessToken to cookie header
            ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(1800)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            JwtResponseDTO jwtResponseDTO = new JwtResponseDTO(accessToken, refreshToken.getToken());
            System.out.println(jwtResponseDTO);
            return jwtResponseDTO;
//            return JwtResponseDTO.builder()
//                    .accessToken(accessToken)
//                    .token(refreshToken.getToken()).build();
            //return "redirect:/home";

        } else {
            throw new UsernameNotFoundException("invalid user request..!!");
        }

    }


}
