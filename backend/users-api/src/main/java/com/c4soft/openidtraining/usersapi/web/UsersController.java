package com.c4soft.openidtraining.usersapi.web;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.c4_soft.springaddons.security.oauth2.OAuthentication;
import com.c4_soft.springaddons.security.oauth2.UnmodifiableClaimSet;
import com.c4soft.openidtraining.usersapi.domain.UserRoles;
import com.c4soft.openidtraining.usersapi.domain.UserRolesRepository;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@Tag(name = "Users")
public class UsersController {
    private final UserRolesRepository rolesRepo;

    @GetMapping("/{email}/roles")
    @PreAuthorize("hasAnyAuthority('SCOPE_roles:read', 'USER_ROLES_EDITOR')")
    @Transactional(readOnly = true)
    public Collection<String> getRoles(@Parameter(schema = @Schema(type = "string", format = "email")) @PathVariable("email") Optional<UserRoles> userRoles) {
        return userRoles.orElseThrow(() -> new EntityNotFoundException()).getRoles();
    }

    @PutMapping("/{email}/roles")
    @PreAuthorize("hasAuthority('USER_ROLES_EDITOR')")
    @Transactional(readOnly = false)
    public ResponseEntity<?> updateRoles(@PathVariable("email") @NotEmpty @Email String email, @RequestBody List<String> roles, HttpServletRequest request) {
        final var entity = rolesRepo.findById(email);
        if(entity.isEmpty()) {
            rolesRepo.save(new UserRoles(email, new HashSet<>(roles)));
        } else {
            final var userRoles = entity.get();
            userRoles.setRoles(new HashSet<>(roles));
            rolesRepo.save(userRoles);
        }
        return ResponseEntity.accepted().build();
    }
    
    @GetMapping("/me")
    public UserInfoDto getInfo(Authentication auth) {
        if(auth instanceof OAuthentication<?> oauth) {
            @SuppressWarnings("unchecked")
            var auth0UserData = new UnmodifiableClaimSet(Optional.ofNullable((Map<String, Object>) oauth.getAttribute("https://c4-soft.com/user")).orElse(Map.of()));
            return new UserInfoDto(auth0UserData.getAsString("name"), auth0UserData.getAsString("email"), oauth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        }
        return UserInfoDto.ANONYMOUS;
    }
    
    public static record UserInfoDto(@NotNull String name, @NotNull String email, @NotNull Collection<String> roles) {
        public static final UserInfoDto ANONYMOUS = new UserInfoDto("", "", List.of());
    }
}
