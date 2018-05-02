package poc.dineesh.app;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import ai.api.AIConfiguration;
@Configuration
@EnableAutoConfiguration
@EnableAspectJAutoProxy
@ComponentScan("poc.dineesh.app")
public class Application extends SpringBootServletInitializer implements CommandLineRunner  {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    

	@Override
	public void run(String... args) throws Exception {
		
		
	}
}
