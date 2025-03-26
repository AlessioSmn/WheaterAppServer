package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.UserDTO;
import it.unipi.lsmsd.model.Role;
import it.unipi.lsmsd.model.User;
import it.unipi.lsmsd.repository.UserRepository;
import it.unipi.lsmsd.utility.JWTUtil;
import it.unipi.lsmsd.utility.PasswordHashUtil;
import redis.clients.jedis.exceptions.JedisConnectionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

@Service
public class UserService {
    
    // Dependency Injection (using @Autowired)
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionRedisService sessionRedisService;

    // Login 
    public String login(UserDTO userDTO) throws Exception{      
        try {

            // Get the User from MondoDB via Repository with username
            Optional<User> userOpt = userRepository.findByUsername(userDTO.getUsername());
            
            // Validate the User is found with given username and then the password matches (DTO vs Model)
            if (userOpt.isEmpty() || !PasswordHashUtil.verifyPassword(userDTO.getPassword(), userOpt.get().getPassword())) {
                throw new Exception("Invalid credentials");
            }
                    
            // Generate token
            String token = JWTUtil.generateToken(userDTO.getUsername(), userOpt.get().getRole());

            // Save the session in Redis
            sessionRedisService.saveSession(token, userDTO.getUsername());
            return token;
        } catch (JedisConnectionException e) {
            // TODO: LOG exception
            throw new JedisConnectionException("Reddis Server Error: " + e.getMessage(), e);
        } catch (Exception ex) {
            // TODO: LOG exception
            throw ex;
        }  
    }

    // Logout
    public void logout(String token) {
        try {
            // delete the session
            sessionRedisService.deleteSession(token);
        } catch (Exception ex) {
            // TODO: LOG Excpetion
            throw ex;
        }
        
    }

    // Register User 
    public void register(UserDTO userDTO){
        try {
            // Hash the password to be stored in DB
            String hashedPassword = PasswordHashUtil.hashPassword(userDTO.getPassword());
            // Create User model from UserDTO
            User user = new User(userDTO.getUsername(), hashedPassword, userDTO.getEmail(), Role.USER);
            userRepository.save(user);
        } catch (Exception ex) {
            if (!(ex instanceof DuplicateKeyException)) {
                // TODO: Log Exceptions other than DuplicateKeyException
            }
            throw ex;
        }
    }
        
}