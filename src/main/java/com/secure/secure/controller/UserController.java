package com.secure.secure.controller;

import com.secure.secure.config.JwtUtility;
import com.secure.secure.config.UserService;
import com.secure.secure.dto.Input;
import com.secure.secure.entity.User;
import com.secure.secure.repository.UserRepository;
import com.secure.secure.util.CustomException;
import com.secure.secure.util.ResponseMapper;
import com.secure.secure.util.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController implements ResponseMapper {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseObject login(@RequestBody Input.Login input) throws IOException, CustomException {
        //todo validate input and set max length
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.username(),
                            input.password()
                    )
            );
        }
        catch (Exception e){
            return errorResponse(new CustomException("BAD CREDENTIALS"));
        }

        final UserDetails userDetails= userService.loadUserByUsername(input.username());
        final String token = jwtUtility.generateToken(userDetails);
        return successResponse(token);




//        var user = userRepository.findByUsername(input.username());
//        if(!user.isPresent()){
//            return errorResponse(new CustomException("No user with username:" + input.username()));
//        }
//        if(!passwordEncoder.matches(input.password(),user.get().getPassword())){
//            return errorResponse(new CustomException("Password did not match"));
//        }
//        if(!input.password().equals(user.get().getPassword())){
//            return errorResponse(new CustomException("Password did not match"));
//        }
//        return successResponse("Successfully logged in user");
    }
    @PostMapping("/register")
    public ResponseObject save(@RequestBody Input.Register input) throws IOException, CustomException {
        if(!input.password().equals(input.confirmPassword())){
            return errorResponse(new CustomException("Password did not match"));
        }
        //todo validate each input and add limit to each input
        //todo check if user exist with same username
        var user = new User();
        user.setFirstName(input.firstName());
        user.setLastName(input.lastName());
        user.setUsername(input.username());
        user.setEmail(input.email());
//        user.setPassword(passwordEncoder.encode(input.password()));
        user.setPassword(input.password());
        user.setGender(input.Gender());
        user.setUploadLimit(50000L); //50 MB
        var saved = userRepository.save(user);

        return successResponse("Successfully saved user" + saved.getId());
    }


    @DeleteMapping("/delete/{id}")
    public ResponseObject deleteUser(@PathVariable(name ="id") String userId){
        //todo validate userId
        //todo delete all user posts
        userRepository.deleteById(userId);
        return successResponse("Successfully deleted the user");
    }

    @PutMapping("/update-limit")
    public ResponseObject updateLimit(@RequestBody Input.UpdateLimit input){
    //todo validate limit and check max size
        var user = userRepository.findById(input.userId());
        if(!user.isPresent()){
            return errorResponse(new CustomException("No user with userId:" + input.userId()));
        }
        user.get().setUploadLimit(input.limit());
        userRepository.save(user.get());
        return successResponse("Updated limit for User:"+ input.limit());
    }

    @PutMapping("/update-profile")
    public ResponseObject updateProfile(@RequestBody Input.UpdateUser input){
        var user = userRepository.findById(input.userId());
        if(!user.isPresent()){
            return errorResponse(new CustomException("User not Found!"));
        }
        //todo validate each input and add limit to each input
        user.get().setFirstName(input.firstName());
        user.get().setLastName(input.lastName());
        user.get().setUsername(input.username());
        user.get().setEmail(input.email());
//        user.get().setPassword(passwordEncoder.encode(input.password()));
        user.get().setPassword(input.password());
        user.get().setGender(input.Gender());
        var saved = userRepository.save(user.get());

        return successResponse("Successfully updated your profile user" + saved.getId());
    }
        //todo default admin creation is pending

    public void addAdmin(){
        if(!userRepository.findByUsername("defaultAdmin").isPresent()){
            var user = new User();
            user.setFirstName("default");
            user.setLastName("admin");
            user.setEmail("default@Admin.com");
            user.setUsername("defaultAdmin");
            user.setPassword(passwordEncoder.encode("defaultAdmin123"));
            user.setGender("M");
            user.setUploadLimit(500000L);
            userRepository.save(user);
        }
    }

}
