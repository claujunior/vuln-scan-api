package BCC.ES.CLP;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClpApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();

		dotenv.entries().forEach(e ->
				System.setProperty(e.getKey(), e.getValue())
		);
		SpringApplication.run(ClpApplication.class, args);
	}

}
