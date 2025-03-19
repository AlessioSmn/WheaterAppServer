package it.unipi.lsmsd;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// import it.unipi.lsmsd.model.User;
// import it.unipi.lsmsd.repository.UserRepository;
// import java.util.Optional;

@SpringBootApplication
public class Application {

// public class Application implements CommandLineRunner{
    
    // @Autowired
    // private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // @Override
    // public void run(String... args) throws Exception {
    //     Optional<User> userOpt = userRepository.findByUsername("johndoe");

    //     if (userOpt.isPresent()) {
    //         User user = userOpt.get();
    //         System.out.println("User found: " + user.getUsername() + ", ID: " + user.getId());
    //     } else {
    //         System.out.println("User not found.");
    //     }
    // }
}
