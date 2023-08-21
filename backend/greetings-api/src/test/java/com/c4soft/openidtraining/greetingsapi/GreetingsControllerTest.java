package com.c4soft.openidtraining.greetingsapi;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import com.c4_soft.springaddons.security.oauth2.test.webmvc.AutoConfigureAddonsWebmvcResourceServerSecurity;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.MockMvcSupport;
import com.c4soft.openidtraining.greetingsapi.GreetingsController.GreetingDto;

@WebMvcTest(controllers = GreetingsController.class)
@AutoConfigureAddonsWebmvcResourceServerSecurity
@Import(SecurityConfig.class)
class GreetingsControllerTest {
	
	@Autowired
	MockMvcSupport api;

	@Test
	void givenRequestIsAnonymous_whenGetGreeting_thenReturnDefaultMessage() throws Exception {
		api.get("/greetings").andExpect(status().isOk()).andExpect(jsonPath("$.message", is(GreetingDto.DEFAULT.message())));
	}

	@Test
	void givenUserIsAuthenticated_whenGetGreeting_thenReturnCustomizedMessage() throws Exception {
		api.get("/greetings").andExpect(status().isOk()).andExpect(jsonPath("$.message", allOf(containsString("ch4mp"), containsString("USER_ROLES_EDITOR"), containsString("AUTHOR"))));
	}

}
