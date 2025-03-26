package it.unipi.lsmsd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        try {
            SpringApplication.run(Application.class, args);    
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("x");
        }
    }
}
