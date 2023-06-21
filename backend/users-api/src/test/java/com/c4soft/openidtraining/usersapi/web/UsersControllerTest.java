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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;

import com.c4_soft.springaddons.security.oauth2.OAuthentication;
import com.c4_soft.springaddons.security.oauth2.OpenidClaimSet;
import com.c4_soft.springaddons.security.oauth2.test.annotations.Claims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.NestedClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenId;
import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.StringClaim;
import com.c4_soft.springaddons.security.oauth2.test.annotations.parameterized.OpenIdAuthenticationSource;
import com.c4_soft.springaddons.security.oauth2.test.annotations.parameterized.ParameterizedOpenId;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.MockMvcSupport;
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

	@BeforeEach
	void setUp() {
		when(rolesRepo.findById(UserRolesFixtures.CH4MP.getEmail())).thenReturn(Optional.of(UserRolesFixtures.CH4MP));
		when(rolesRepo.save(any(UserRoles.class))).thenAnswer(invocation -> (UserRoles) invocation.getArgument(0));
	}

	@Test
	@WithAnonymousUser
	void givenRequestIsAnonymous_whenGetUserRoles_thenUnauthorized() throws Exception {
		api.get("/users/%s/roles".formatted(UserRolesFixtures.CH4MP.getEmail())).andExpect(status().isUnauthorized());
	}

	@ParameterizedTest
	@OpenIdAuthenticationSource({@OpenId("SCOPE_roles:read"), @OpenId("USER_ROLES_EDITOR")})
	void givenUserIsGrantedWithRolesReadScope_whenGetUserRolesOfExistingUser_thenRolesAreReturned(@ParameterizedOpenId OAuthentication<OpenidClaimSet> auth) throws Exception {
		// @formatter:off
		api.get("/users/%s/roles".formatted(UserRolesFixtures.CH4MP.getEmail()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*]", containsInAnyOrder(UserRolesFixtures.CH4MP.getRoles().toArray())));
		// @formatter:on
		
	}

	@Test
	@OpenId("SCOPE_roles:read")
	void givenUserIsGrantedWithRolesReadScope_whenGetUserRolesOfUnknownUser_thenReturnEmptyCollection() throws Exception {
		// @formatter:off
		api.get("/users/%s/roles".formatted("machin@truc"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*]").isEmpty());
		// @formatter:on
	}

	@Test
	@OpenId("SCOPE_roles:write")
	void givenUserIsNotGrantedWithRolesReadScope_whenGetUserRoles_thenForbidden() throws Exception {
		api.get("/users/%s/roles".formatted(UserRolesFixtures.CH4MP.getEmail())).andExpect(status().isForbidden());
	}

	@Test
	@WithAnonymousUser
	void givenRequestIsAnonymous_whenSetUserRoles_thenUnauthorized() throws Exception {
		api.get("/users/%s/roles".formatted(UserRolesFixtures.CH4MP.getEmail())).andExpect(status().isUnauthorized());
	}

	@Test
	@OpenId("USER_ROLES_EDITOR")
	void givenUserIsGrantedWithUserRolesEditor_whenSetUserRoles_thenRolesAreUpdated() throws Exception {
		api.put(List.of("MACHIN", "TRUC"), "/users/%s/roles".formatted(UserRolesFixtures.CH4MP.getEmail()))
				.andExpect(status().isAccepted());
		verify(rolesRepo).save(new UserRoles(UserRolesFixtures.CH4MP.getEmail(), Set.of("MACHIN", "TRUC")));
	}

	@Test
	@OpenId("SCOPE_roles:read")
	void givenUserIsNotGrantedWithRolesReadScope_whenSetUserRoles_thenForbidden() throws Exception {
		api.put(List.of("MACHIN", "TRUC"), "/users/%s/roles".formatted(UserRolesFixtures.CH4MP.getEmail()))
				.andExpect(status().isForbidden());
	}

	@Test
	@OpenId("USER_ROLES_EDITOR")
	void givenUserIsGrantedWithUserRolesEditor_whenSetNullRoles_thenBadRequest() throws Exception {
		api.put(null, "/users/%s/roles".formatted(UserRolesFixtures.CH4MP.getEmail()))
				.andExpect(status().isBadRequest());
	}

	@Test
	@OpenId("USER_ROLES_EDITOR")
	void givenUserIsGrantedWithUserRolesEditor_whenSetRolesWithNullOrEmptyEmail_thenNotFound() throws Exception {
		api.put(List.of("MACHIN", "TRUC"), "/users//roles").andExpect(status().isNotFound());
	}

	@Test
	@OpenId("USER_ROLES_EDITOR")
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
	@OpenId(authorities = { "MACHIN","TRUC" },
			claims = @OpenIdClaims(
				otherClaims = @Claims(
					nestedClaims = @NestedClaims(name = "https://c4-soft.com/user", stringClaims = {
						@StringClaim(name = "name", value = "ch4mpy"),
						@StringClaim(name = "email", value = "ch4mp@c4-soft.com")}))))
	void givenRequestIsAuthenticated_whenGetMe_thenReturnData() throws Exception {
		api.get("/users/me")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name", is("ch4mpy")))
			.andExpect(jsonPath("$.email", is("ch4mp@c4-soft.com")))
			.andExpect(jsonPath("$.roles", containsInAnyOrder("MACHIN", "TRUC")));
	}
	// @formatter:on

	static final class UserRolesFixtures {
		public static final UserRoles CH4MP = new UserRoles("ch4mp@c4-soft.com", Set.of("NICE", "AUTHOR"));
	}

}
