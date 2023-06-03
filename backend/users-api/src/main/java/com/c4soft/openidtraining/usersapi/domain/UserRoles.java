package com.c4soft.openidtraining.usersapi.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoles {

	@Id
	@Email
	private String email;
	
	@ElementCollection
	private Set<String> roles = new HashSet<>();

}
