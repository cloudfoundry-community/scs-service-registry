package com.starkandwayne.serviceregistry;

import java.util.Properties;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

import com.netflix.eureka.EurekaServerContext;
import com.starkandwayne.serviceregistry.ssl.DynamicCertificateTruster;


@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {

	public static CloudFoundrySessionData SessionData;
	public static SpringApplication Application;
	public static ConfigurableApplicationContext ApplicationContext;
	public static Peers CurrentLoadedPeers;
	public static EurekaServerContext eContext;
	public static String ServerPort;

	public static void main(String[] args) {
		DynamicCertificateTruster.instantiateAndInstall();
		Application = new SpringApplication(ServiceRegistryApplication.class);
		setupConfiguration();
		SetApplicationConfigruation();
		ApplicationContext = Application.run(args);
	    
	}

	@Bean public ServiceRegistryProperties serviceRegistryProperties(){ return new ServiceRegistryProperties(); }
	@Bean
	@Lazy
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
	   // Do any additional configuration here
	   return builder.build();
	}
	
	public static void RestartApplication() {
		ApplicationArguments args = ApplicationContext.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            ApplicationContext.close();
			SetApplicationConfigruation();
            ApplicationContext =Application.run(args.getSourceArgs());
        });

        thread.setDaemon(false);
        thread.start();
	}

	public static void setupConfiguration() {
		SessionData = CloudFoundrySessionData.GetEnvironment();
		CurrentLoadedPeers = Peers.LoadPeersFromENV();
	}

	public static void SetApplicationConfigruation() {
		Properties properties = new Properties();

		if (CurrentLoadedPeers.CurrentPeers.size() > 0) {
			System.out.println("Peers Detected");
			properties.setProperty("eureka.client.serviceUrl.defaultZone", CurrentLoadedPeers.GetPeersAsString());
			properties.setProperty("eureka.client.registerWithEureka", "true");
			properties.setProperty("eureka.client.fetchRegistry", "true");
			System.out.println(properties);
		}
		else
		{
			System.out.println("No Peers Detected");
			properties.setProperty("eureka.client.serviceUrl.defaultZone", "http://localhost:8080/eureka");
			properties.setProperty("eureka.client.registerWithEureka", "false");
			properties.setProperty("eureka.client.fetchRegistry", "false");
		}
		String index = "0";
		if (SessionData.INSTANCE_INDEX != null) {
			index = SessionData.INSTANCE_INDEX;
		}
		else
		{
			System.out.println("Value  INDEX " + SessionData.INSTANCE_INDEX);
		}
		ServerPort = "808"+index;
		System.out.println("Attempting to host on port " + ServerPort);
		properties.setProperty("server.port", ServerPort);
		Application.setDefaultProperties(properties);
	}
}
