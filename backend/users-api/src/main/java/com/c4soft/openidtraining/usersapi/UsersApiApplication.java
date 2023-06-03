package com.c4soft.openidtraining.usersapi;

import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.c4soft.openidtraining.usersapi.domain.UserRoles;
import com.c4soft.openidtraining.usersapi.domain.UserRolesRepository;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
public class UsersApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsersApiApplication.class, args);
	}

	@Component
	@RequiredArgsConstructor
	public static class UserDorlesDatabaseInitilizer implements ApplicationListener<ContextRefreshedEvent> {
		private final UserRolesRepository userRolesRepo;

	    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
	    	userRolesRepo.save(InitialUserRoles.CH4MP);
	    	userRolesRepo.save(InitialUserRoles.JWACONGNE);
	    }
	    
	    public static class InitialUserRoles {
	    	public static final UserRoles CH4MP = new UserRoles("ch4mp@c4-soft.com", Set.of("roles:read", "roles:write"));
	    	public static final UserRoles JWACONGNE = new UserRoles("jwacongne@c4-soft.com", Set.of("NICE", "AUTHOR"));
	    }
	}
}
