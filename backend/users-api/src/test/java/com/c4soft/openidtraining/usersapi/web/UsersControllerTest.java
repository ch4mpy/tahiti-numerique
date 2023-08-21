package com.c4soft.openidtraining.usersapi.web;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.test.context.support.WithAnonymousUser;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockAuthentication;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.MockMvcSupport;
import com.c4soft.openidtraining.usersapi.EnableSpringDataWebSupportTestConf;
import com.c4soft.openidtraining.usersapi.SecuredTest;
import com.c4soft.openidtraining.usersapi.domain.UserRoles;
import com.c4soft.openidtraining.usersapi.domain.UserRolesRepository;

@WebMvcTest(controllers = UsersController.class)
@SecuredTest
@Import(EnableSpringDataWebSupportTestConf.class)
class UsersControllerTest {

	@MockBean
	UserRolesRepository rolesRepo;

	@Autowired
	MockMvcSupport api;
	
	@Autowired
	WithJwt.AuthenticationFactory authFactory;

	@BeforeEach
	void setUp() {
		when(rolesRepo.findById("ch4mp@c4-soft.com")).thenReturn(Optional.of(new UserRoles("ch4mp@c4-soft.com", Set.of("USER_ROLES_EDITOR", "AUTHOR"))));
		when(rolesRepo.save(any(UserRoles.class))).thenAnswer(invocation -> (UserRoles) invocation.getArgument(0));
	}

	@Test
	@WithAnonymousUser
	void givenRequestIsAnonymous_whenGetUserRoles_thenUnauthorized() throws Exception {
		api.get("/users/%s/roles".formatted("ch4mp@c4-soft.com")).andExpect(status().isUnauthorized());
	}
	
	Stream<AbstractAuthenticationToken> allRolesRead() {
		return authFactory.authenticationsFrom("ch4mp.json", "auth0-action.json");
	}

	@Test
	@WithMockAuthentication("SCOPE_roles:read")
	void givenUserIsGrantedWithRolesReadScope_whenGetUserRolesOfUnknownUser_thenReturnEmptyCollection() throws Exception {
		// @formatter:off
		api.get("/users/%s/roles".formatted("machin@truc"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*]").isEmpty());
		// @formatter:on
	}

	@Test
	@WithMockAuthentication("SCOPE_roles:write")
	void givenUserIsNotGrantedWithRolesReadScope_whenGetUserRoles_thenForbidden() throws Exception {
		api.get("/users/%s/roles".formatted("ch4mp@c4-soft.com")).andExpect(status().isForbidden());
	}

	@Test
	@WithAnonymousUser
	void givenRequestIsAnonymous_whenSetUserRoles_thenUnauthorized() throws Exception {
		api.get("/users/%s/roles".formatted("ch4mp@c4-soft.com")).andExpect(status().isUnauthorized());
	}

	@Test
	@WithJwt("ch4mp.json")
	void givenUserIsCh4mp_whenSetUserRoles_thenRolesAreUpdated() throws Exception {
		api.put(List.of("MACHIN", "TRUC"), "/users/%s/roles".formatted("ch4mp@c4-soft.com"))
				.andExpect(status().isAccepted());
		verify(rolesRepo).save(new UserRoles("ch4mp@c4-soft.com", Set.of("MACHIN", "TRUC")));
	}

	@Test
	@WithMockAuthentication("SCOPE_roles:read")
	void givenUserIsNotGrantedWithRolesReadScope_whenSetUserRoles_thenForbidden() throws Exception {
		api.put(List.of("MACHIN", "TRUC"), "/users/%s/roles".formatted("ch4mp@c4-soft.com"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockAuthentication("USER_ROLES_EDITOR")
	void givenUserIsGrantedWithUserRolesEditor_whenSetNullRoles_thenBadRequest() throws Exception {
		api.put(null, "/users/%s/roles".formatted("ch4mp@c4-soft.com"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockAuthentication("USER_ROLES_EDITOR")
	void givenUserIsGrantedWithUserRolesEditor_whenSetRolesWithNullOrEmptyEmail_thenNotFound() throws Exception {
		api.put(List.of("MACHIN", "TRUC"), "/users//roles").andExpect(status().isNotFound());
	}

	@Test
	@WithMockAuthentication("USER_ROLES_EDITOR")
	void givenUserIsGrantedWithUserRolesEditor_whenSetRolesWithInvalidEmail_thenBadRequest() throws Exception {
		api.put(List.of("MACHIN", "TRUC"), "/users/ch4mp/roles").andExpect(status().is4xxClientError());
	}

	@Test
	@WithAnonymousUser
	void givenRequestIsAnonymous_whenGetMe_thenReturnAnonymous() throws Exception {
		// @formatter:off
		api.get("/users/me")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name", is("")))
			.andExpect(jsonPath("$.email", is("")))
			.andExpect(jsonPath("$.roles").isEmpty());
		// @formatter:on
	}

	// @formatter:off
	@Test
	@WithJwt("ch4mp.json")
	void givenRequestIsCh4mp_whenGetMe_thenReturnData() throws Exception {
		api.get("/users/me")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name", is("ch4mp")))
			.andExpect(jsonPath("$.email", is("ch4mp@c4-soft.com")))
			.andExpect(jsonPath("$.roles", containsInAnyOrder("USER_ROLES_EDITOR", "AUTHOR")));
	}
	// @formatter:on

}
