
# Formation OpenID
Le but est de mettre en place:
- deux front-ends distincts (une **S**ingle **P**age **A**pplication `Next.js` et une application mobile `React Native`)
- deux APIs REST Spring distinctes
- deux OpenID Providers: Auth0 et Keycloak

## 1. Backend Spring Boot
Projet Maven comprenant des modules d'API REST configurés en tant que resource server OAuth2 et de **B**ackends **F**or **F**rontends faisant l'interface entre ces resource server et les front-ends qui feraient pas des client OAuth2 sûrs (SPA et application mobile).

Nous pour la configuration OpenID, nous utiliserons les starters Spring Boot de [spring-addons](https://github.com/ch4mpy/spring-addons) qui poussent un peu plus loin l'auto-configuration de `spring-boot-starter-oauth2-client` et `spring-boot-starter-oauth2-resource-server`. Pour n'utiliser que ces derniers (et écrire manuellement la conf générée par spring-addons), se référer [aux tutoriels](https://github.com/ch4mpy/spring-addons/tree/master/samples/tutorials) du même dépôt Github.

### 1.1. Resource Servers
Nous exposerons deux APIs REST distinctes :
- `users-api` : limitée à l'exposition des roles d'un utilisateur dans le cadre de la formation
- `greetings-api` : retourne un message personnalisé avec des éléments de l'identité associée à la requête

#### 1.1.1. Greetings API
Cette API construit un message à partir d'informations contenues dans l'`Authentication` (`JwtAuthenticationToken` par défaut pour les resource servers). Une première implémentation est fournie, le TP porte sur l'écriture des tests unitaires et la personnalisation du type d'`Authentication` utilisé.
- compléter les tests unitaires en utilisant soit `@WithMockJwt`, soit `SecurityMockMvcRequestPostProcessors.jwt()` pour insérer et configurer un `JwtAuthenticationToken` dans le `TestSecurityContext`.
- ajouter un `@Bean` de type `OAuth2AuthenticationFactory` pour changer le type d'`Authentication` utilisé de `JwtAuthenticationToken` à `OAuthentication<OpenidClaimSet>`. [Exemple ici](https://github.com/ch4mpy/spring-addons/tree/master/samples/webmvc-jwt-oauthentication)
- mettre à jour le `@Controller` et les tests unitaires avec le nouveau type d'`Authentication`

#### 1.1.2. Users API
Cette API a pour but de retourner les roles d'un utilisateur donné. Voici les fonctionnalités à implémenter:
- configuration `resource server` qui utilise la claim `scope` du JWT comme source pour les authorities Spring et Auht0 comme issuer. Utiliser [`com.c4-soft.springaddons:spring-addons-webmvc-jwt-resource-server`](https://central.sonatype.com/artifact/com.c4-soft.springaddons/spring-addons-webmvc-jwt-resource-server/6.1.11). Utiliser la "Greetings API" ou [cet autre exemple](https://github.com/ch4mpy/spring-addons/tree/master/samples/webmvc-jwt-default) comme base.
- exposer un endpoint REST GET pour le path `/v1/users/{id}/roles` qui retourne un DTO contenant une liste de roles
- rendre le endpoint accessible uniquement aux requêtes authorisées avec l'Authority `roles:read`
- jouer les tests unitaires pour valider votre l'implémentation.

### 1.2. BFFs
Les Backends For Frontends sont des middlewares sur le serveur configurés comme clients OAuth2 et faisant le pont entre une sécurité basée sur des sessions (côté externe) et une basée sur des "access tokens" OAuth2.

Nous utiliserons des instances de `spring-cloud-gateway` avec le filtre `TokenRelay` et un starter Spring Boot pour la configuration OAuth2 "cliente".

#### 1.2.1. BFF Web Back-Office

#### 1.2.2. BFF Web Front-Office

#### 1.2.3. BFF Mobile Front-Office
Se rendre sur https://start.spring.io pour générer un projet Maven / Java avec Spring Boot 3.1 et les dépendances suivantes
- Gateway
- Lombok
- Spring Boot Actuator
- Spring Configuration Processor
- Spring Boot DevTools
- GraalVM Native Support

Une fois le projet dézippé, l'ajouter en tant que module du `backend` (le placer dans le répertoire `backend`, changer le parent et supprimer les ressources liées au Maven wrapper et à Git)

Ajouter une dépendance à [`com.c4-soft.springaddons:spring-addons-webflux-client`](https://central.sonatype.com/artifact/com.c4-soft.springaddons/spring-addons-webflux-client/6.1.11) (le version est managée par le parent).

S'inspirer des deux autres modules BFF pour la configuration.

## 2. Front-Ends

### 2.1. SPA Next.js

### 2.1.1. SPA Next.js Back-Office
- `npx create-next-app@latest` 

### 2.1.2. SPA Next.js Front-Office
- `npx create-next-app@latest`

### 2.2. Application Mobile React Native


## 3. OpenID Providers
Nous utiliserons Auth0 comme OP principal. Il aura pour responsabilité de fédérer les identités Keycloak.

### 3.1. Keycloak
- pour l'installation, suivre [ces intructions](https://github.com/ch4mpy/spring-addons/blob/master/samples/tutorials/keycloak.md)
- créer un realm `openid-training`
- créer un client `auth0` avec `Client authentication` et `Standard flow` activés (Auth0 utilisera ce client pour effectuer les "Login with c4-soft")
- créer un utilisateur pour les tests

### 3.2. Auth0
- créez un compte gratuit si vous n'en possédez pas déjà un
- dans `Applications` -> `APIs`
  * ajouter une "API" nommée `OpenID Training users API` avec `https://openid-training.c4-soft.com/api/users`
  * dans l'onglet `Permissions`, ajouter  `read:user-roles`
- déclarez les "applications" suivantes (ce sont en réalité des clients OAuth2 que nous configurons ici):
  * `OpenID Training BFF back-office` (Regular Web Application)
  * `OpenID Training BFF front-office` (Regular Web Application)
  * `OpenID Training BFF mobile` (Regular Web Application)
  * `OpenID Training users roles API` (Machine to Machine). Dans l'onglet `APIs`, activer `OpenID Training users API`, puis déplier le détail de cette API pour activer la permission `read:user-roles`
- dans `Authentication` -> `Social`, créer un connection "custom" (tout en bas). 
  * les endpoints importants sont fournis par le `.well-known/openid-configuration` de l'OP à fédérer
  * dans la section `Scope`, indiquer `openid profile email`
  * exemple de `Fetch User Profile Script` pour Keycloak (le userinfo endpoint et parsing de la réponse seront à adapter):
```typescript
async function(accessToken, ctx, cb) {
  request.get(
    {
      url: 'https://oidc.c4-soft.com/auth/realms/openid-training/protocol/openid-connect/userinfo',
      headers: {
        'Authorization': 'Bearer ' + accessToken,
      }
    },
    (err, resp, body) => {
      if (err) {
        return cb(err);
      }
      if (resp.statusCode !== 200) {
        return cb(new Error(body));
      }
      let bodyParsed;
      try {
        bodyParsed = JSON.parse(body);
      } catch (jsonError) {
        return cb(new Error(body));
      }
      const profile = {
        user_id: bodyParsed.sub,
        email: bodyParsed.email,
        name: bodyParsed.preferred_username,
        email_verified: bodyParsed.email_verified,
      };
      cb(null, profile);
    }
  );
}
```
  * dans l'onglet `Applications`, activer ce connecteur pour le client `OpenID Training BFF back-office`
- dans `Actions` -> `Flows` -> `Login`, ajouter une action `Add user data to access and ID tokens`
```typescript
exports.onExecutePostLogin = async (event, api) => {
  const namespace = 'https://c4-soft.com';
  const user = Object.assign({}, event.user);
  user.roles = event.authorization?.roles || [];
  api.accessToken.setCustomClaim(`${namespace}/user`, user);
  api.idToken.setCustomClaim(`${namespace}/user`, user);
  return; // success
};
```
