package com.example.userservice5.service;

import com.example.userservice5.dto.ResetPasswordDto;
import com.example.userservice5.dto.UserDto;
import com.example.userservice5.entity.PasswordResetTokenEntity;
import com.example.userservice5.entity.RoleEntity;
import com.example.userservice5.entity.UserEntity;
import com.example.userservice5.exception.ApiException;
import com.example.userservice5.model.response.ProfileResponse;
import com.example.userservice5.repository.PasswordResetRepository;
import com.example.userservice5.repository.RoleRepository;
import com.example.userservice5.repository.UserRepository;
import com.example.userservice5.security.UserPrincipal;
import com.example.userservice5.utils.Utils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordResetRepository passwordResetRepository;

    public UserDto createUser(UserDto dto) {
        return createUserWithRole(dto, "ROLE_USER");
    }

    public UserDto createPartner(UserDto dto) {
        return createUserWithRole(dto, "ROLE_PARTNER");
    }

    public UserDto createUserWithRole(UserDto dto, String role) {
        RoleEntity roleUser = createRole(role);
        ModelMapper mapper = new ModelMapper();
        String email = dto.getEmail();
        UserEntity existingUser = userRepository.findByEmail(email);
        if(existingUser != null){
            throw new ApiException(HttpStatus.BAD_REQUEST, "User already exists");
        }
        if(!dto.getPassword().equals(dto.getConfirmPassword())){{
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password does not match");
        }}
        String userId = Utils.generateUserId();
        String token = Utils.generateEmailVerificationToken(userId);
        UserEntity user = mapper.map(dto, UserEntity.class);
        user.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        user.setUserId(userId);
        user.setEmailVerificationToken(token);
        user.setEmailVerificationStatus(false);
        user.setRoles(Arrays.asList(roleUser));
        UserEntity createdUser = userRepository.save(user);
        UserDto returnValue = mapper.map(createdUser, UserDto.class);
        return returnValue;
    }
    public Boolean verifyEmail(String token) {
        UserEntity user = userRepository.findByEmailVerificationToken(token);
        if(Utils.hasTokenExpired(token)) throw new ApiException(HttpStatus.BAD_REQUEST, "Token has expired");
        if(user == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Token does not exist");
        user.setEmailVerificationStatus(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(username);
        if(user == null) throw new UsernameNotFoundException(username);
        Collection<GrantedAuthority> authorities = new HashSet<>();
        Collection<RoleEntity> roles = user.getRoles();
        if(roles.isEmpty()){
            throw new ApiException(HttpStatus.BAD_REQUEST,"User does not have roles");
        }

        roles.forEach((role)->{
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });

        return new User(user.getEmail(),user.getPassword(),true,true,true,true, authorities);
    }

    @Transactional
    RoleEntity createRole(
            String name
    ){
        RoleEntity role = roleRepository.findByName(name);

        if(role==null){
            role = new RoleEntity();
            role.setName(name);
            roleRepository.save(role);
        }
        return role;
    }

    public Boolean initiateResetPassword(String email) {
        try{
            UserEntity user = userRepository.findByEmail(email);
            if(user == null){
                throw new ApiException(HttpStatus.BAD_REQUEST,"User not found");
            }
            String token = Utils.generateEmailVerificationToken(user.getUserId());
            PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
            passwordResetTokenEntity.setToken(token);
            passwordResetTokenEntity.setUser(user);
            passwordResetRepository.save(passwordResetTokenEntity);
            return true;
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST,"You have a token generated already");
        }
    }

    public Boolean resetPassword(ResetPasswordDto dto) {
            PasswordResetTokenEntity token = passwordResetRepository.findByToken(dto.getResetPasswordToken());
            if(token == null){
                throw new ApiException(HttpStatus.BAD_REQUEST,"Token not found");
            }
            if(Utils.hasTokenExpired(token.getToken())){
                throw new ApiException(HttpStatus.BAD_REQUEST,"Token has expired, generate another one");
            }
            if(!dto.getPassword().equals(dto.getConfirmPassword())){
                throw new ApiException(HttpStatus.BAD_REQUEST,"Password and Confirm Password does not match");
            }
            UserEntity user = token.getUser();

            if(user == null){
                throw new ApiException(HttpStatus.BAD_REQUEST,"User not found");
            }

            user.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
            passwordResetRepository.delete(token);
            userRepository.save(user);
            return true;
    }

    public ProfileResponse getUser() {
        ModelMapper mapper = new ModelMapper();
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity existingUser = userPrincipal.getUserEntity();
        if(existingUser == null) throw new ApiException(HttpStatus.BAD_REQUEST,"User does not exist");
        return mapper.map(existingUser, ProfileResponse.class);
    }
}
