package com.c4soft.openidtraining.usersapi;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenId;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.AddonsWebmvcTestConf;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.MockMvcSupport;
import com.c4soft.openidtraining.usersapi.UsersApiApplication.UserDorlesDatabaseInitilizer;

@SpringBootTest
@AutoConfigureMockMvc
@ImportAutoConfiguration({ AddonsWebmvcTestConf.class })
class UsersApiApplicationTests {
	@Autowired
	MockMvcSupport api;

	@Test
	@OpenId({"USER_ROLES_EDITOR" })
	void givenUserIsGrantedWithRequiredRoles_whenSetCh4mpRoles_thenRolesAreUpdated() throws Exception {
		final var ch4mp = UserDorlesDatabaseInitilizer.InitialUserRoles.CH4MP;
		
		// @formatter:off
		api.get("/users/%s/roles".formatted(ch4mp.getEmail()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", containsInAnyOrder(ch4mp.getRoles().toArray())));

		api.put(List.of("TRUC"), "/users/%s/roles".formatted(ch4mp.getEmail()))
			.andExpect(status().isAccepted());

		api.get("/users/%s/roles".formatted(ch4mp.getEmail()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", containsInAnyOrder("TRUC")));
		// @formatter:on
	}

}
