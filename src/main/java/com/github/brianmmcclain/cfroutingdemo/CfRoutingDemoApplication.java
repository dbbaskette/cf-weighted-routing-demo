package com.github.brianmmcclain.cfroutingdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@SpringBootApplication
public class CfRoutingDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CfRoutingDemoApplication.class, args);
	}

	@Configuration
	public class BasicSecurityConfig extends WebSecurityConfigurerAdapter {
		public void configure(HttpSecurity http) throws Exception {
			http.authorizeRequests()
				.antMatchers("/actuator/**").hasRole("ACTUATOR")
				.anyRequest().permitAll()
			.and().httpBasic();
		}
	}

}
