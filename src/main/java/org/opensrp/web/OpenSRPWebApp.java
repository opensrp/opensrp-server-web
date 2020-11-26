package org.opensrp.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by Vincent Karuri on 26/11/2020
 */

@SpringBootApplication
@ImportResource({"classpath:spring/applicationContext-opensrp-web.xml"})
public class OpenSRPWebApp {
	public static void main(String[] args) {
		SpringApplication.run(OpenSRPWebApp.class, args);
	}
}
