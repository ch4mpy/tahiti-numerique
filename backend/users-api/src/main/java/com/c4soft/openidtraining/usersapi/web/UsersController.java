package com.c4soft.openidtraining.usersapi.web;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

import com.c4_soft.springaddons.security.oauth2.OAuthentication;
import com.c4_soft.springaddons.security.oauth2.UnmodifiableClaimSet;
import com.c4soft.openidtraining.usersapi.domain.UserRoles;
import com.c4soft.openidtraining.usersapi.domain.UserRolesRepository;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

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
    
@Service
public class ClientCredentialsService {
    
    private final WebClient tokenEndpoint;
    private final String spaceDelimitedScopes;
    
    private Map<String, AccessToken> cachedTokens = new ConcurrentHashMap<>();
    
    public ClientCredentialsService(ClientRegistrationRepository clientRegistrationRepo) {
        final var clientRegistration = clientRegistrationRepo.findByRegistrationId("machin");
        
        // According to your question, this should contain at least message.read
        this.spaceDelimitedScopes = clientRegistration.getScopes().stream().collect(Collectors.joining(" "));
        
        // In client_credentials flow, according to the spec,
        // the access token should be fetched by calling the token endpoint 
        // with client_id and client_secret passed as Basic Authorization header
        // See https://www.rfc-editor.org/rfc/rfc6749#section-4.4
        tokenEndpoint= WebClient.builder()
            .baseUrl(clientRegistration.getProviderDetails().getTokenUri())
            .filter(ExchangeFilterFunctions.basicAuthentication(clientRegistration.getClientId(), clientRegistration.getClientSecret()))
            .build();
    }

    // In production code, you should cache the returned access token
    // for this userId (use the returned validity (expires_in) as cache length)
    public Mono<String> getAccessToken(String userId) {
        final var now = Instant.now();
        this.cachedTokens = cachedTokens.entrySet().stream().filter(at -> at.getValue().expiryTime().isAfter(now)).collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
        
        final var cached = cachedTokens.get(userId);
        if(cached != null) {
            return Mono.just(cached.value);
        }
        
        // This is a call to an OAuth2 token endpoint,
        // using client_credentials flow and a custom header
        // See the spec linked above for the request body (content of the application/x-www-form-urlencoded body)
        return tokenEndpoint.post()
                .header("userId", userId)
                .body(BodyInserters
                        .fromFormData("grant_type", "client_credentials")
                        .with("scope", spaceDelimitedScopes))
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> {
                    final var token = (String) resp.get("access_token");
                    final var expiry = now.plusSeconds((Long) resp.get("expires_in"));
                    this.cachedTokens.put(userId, new AccessToken(token, expiry));
                    return token;
                });
    }
    
    public Mono<WebClient> getAuthorizedClient(String userId) {
        return getAccessToken(userId).map(accessToken ->  WebClient
                .builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken))
                .build());
    }
    
    static private record AccessToken(String value, Instant expiryTime) {}
}

@RestController
@RequiredArgsConstructor
public class MyController {
    private static final String OAUTH2_RESOURCE_URI = "https://other.service/some-resource";

    private final ClientCredentialsService clientCredentialsService;

    @GetMapping("/something-from-an-oauth2-resource-server")
    public String getSomething(HttpServletRequest request) {
        return clientCredentialsService
                .getAuthorizedClient(request.getHeader("UserId"))
                .flatMap(client -> client
                        .get()
                        .uri(OAUTH2_RESOURCE_URI)
                        .retrieve()
                        // This should be changed with the type of DTO returned by the OAuth2 delegate
                        .bodyToMono(String.class))
                .block();
    }
}
}
