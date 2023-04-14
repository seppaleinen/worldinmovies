package se.worldinmovies.neo4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.tools.agent.ReactorDebugAgent;

@SpringBootApplication
public class Neo4jServiceApplication {
	public static void main(String[] args) {
		ReactorDebugAgent.init();
		SpringApplication.run(Neo4jServiceApplication.class, args);
	}
}
