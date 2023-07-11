package com.c4soft.openidtraining.greetingsapi;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.c4_soft.springaddons.security.oidc.OAuthentication;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;

@RestController
@RequestMapping(path = "/greetings", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Greetings")
public class GreetingsController {

	@GetMapping()
	public GreetingDto getGreeting(Authentication auth) {
		if(auth instanceof OAuthentication<?> oauth) {
			return new GreetingDto("Hello %s! You are granted with: %s".formatted(oauth.getName(), oauth.getAuthorities()) );
		}
		return GreetingDto.DEFAULT;
	}
	
	public static record GreetingDto(@NotEmpty String message) {
		public static final GreetingDto DEFAULT = new GreetingDto("I don't talk to strangers");
	}
	
}
