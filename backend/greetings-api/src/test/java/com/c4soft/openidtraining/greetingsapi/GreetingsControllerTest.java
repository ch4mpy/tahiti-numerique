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
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.test.context.support.WithAnonymousUser;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenId;
import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.MockMvcSupport;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.jwt.AutoConfigureAddonsWebSecurity;
import com.c4soft.openidtraining.greetingsapi.GreetingsController.GreetingDto;

@WebMvcTest(controllers = GreetingsController.class)
@AutoConfigureAddonsWebSecurity
@Import(SecurityConfig.class)
class GreetingsControllerTest {
	
	@Autowired
	MockMvcSupport api;

	@Test
	@WithAnonymousUser
	void givenRequestIsAnonymous_whenGetGreeting_thenReturnDefaultMessage() throws Exception {
		api.get("/greetings").andExpect(status().isOk()).andExpect(jsonPath("$.message", is(GreetingDto.DEFAULT.message())));
	}

	@Test
	@OpenId(claims = @OpenIdClaims(usernameClaim = StandardClaimNames.PREFERRED_USERNAME, preferredUsername = "Ch4mp"), authorities = {"NICE", "AUTHOR"})
	void givenUserIsAuthenticated_whenGetGreeting_thenReturnCustomizedMessage() throws Exception {
		api.get("/greetings").andExpect(status().isOk()).andExpect(jsonPath("$.message", allOf(containsString("Ch4mp"), containsString("NICE"), containsString("AUTHOR"))));
	}

}
