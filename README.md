
# Formation OpenID
Le but est de mettre en place:
- trois front-ends distincts 
  * une application `React Native` (Android) nommée `mobile-front-office` et acceptant des identitées Auth0
  * une **S**ingle **P**age **A**pplication `Next.js` nommée `web-back-office` et acceptant des identitées Keycloak
  * une SPA `Next.js` nommée `web-front-office` et acceptant des identitées Auth0
- deux APIs REST Spring distinctes: `greetings-api` et `users-api`
- deux OpenID Providers: Auth0 et Keycloak

Les roles des utilisateurs seront gérés par la `users-api` (pas par Auth0 ou Keycloak). Avant d'émettre un access token, Auth0 interrogera la `users-api` pour récupérer les roles d'un utilisateur et les insérer dans les private-claims.

Voici les URLs de "prod" :
- https://web.front-office.openid-training.c4-soft.com/ui : application Next.js "front-office"
- https://mobile.front-office.openid-training.c4-soft.com/ui utilisée comme deep-link Android (pourrait aussi être utilisé comme universal-link iOS en y hébergeant un fichier `apple-app-site-association`)
- https://web.back-office.openid-training.c4-soft.com/ui : application Next.js "front-office"
- /bff/v1/greetings : accès à l'API `greetings` pour les frontends web & mobile (requêtes avec session)
- /bff/v1/users : accès à l'API `users` pour les frontends web & mobile (requêtes avec session)
- /api/v1/greetings : accès à l'API `greetings` pour les clients OAuth2 (requêtes avec access token)
- /api/v1/users : accès à l'API `users` pour les clients OAuth2 (requêtes avec access token)
- /login/options : endpoint exposant les URIs possibles pour initier l'authenticafication d'un utilisateur
- /logout : endpoint pour terminer une session utilisateur

## 1. Backend Spring Boot
Projet Maven comprenant des modules d'API REST configurés en tant que resource server OAuth2 et un **B**ackends **F**or **F**rontend faisant l'interface entre ces resource server et les front-ends qui, étant des SPAs ou une application mobile, ne seraient pas des client OAuth2 fiables.

Pour la configuration OpenID, nous utiliserons les starters Spring Boot de [spring-addons](https://github.com/ch4mpy/spring-addons) qui poussent un peu plus loin l'auto-configuration de `spring-boot-starter-oauth2-client` et `spring-boot-starter-oauth2-resource-server`. Pour n'utiliser que ces derniers (et écrire manuellement la conf générée par spring-addons), se référer [aux tutoriels](https://github.com/ch4mpy/spring-addons/tree/master/samples/tutorials) du même dépôt Github.

### 1.1. Resource Servers
Nous exposerons deux APIs REST distinctes :
- `users-api` : limitée à la l'exposition et la mise à jour des roles d'un utilisateur
- `greetings-api` : retourne un message personnalisé avec des éléments de l'identité associée à la requête (access token JWT)

#### 1.1.1. Greetings API
Cette API construit un message à partir d'informations contenues dans le `SecurityContext`. Une première implémentation est fournie, le TP porte sur l'écriture des tests unitaires et la personnalisation du type d'`Authentication` utilisé.
- compléter les tests unitaires en utilisant soit `@WithMockJwt`, soit `SecurityMockMvcRequestPostProcessors.jwt()` pour insérer et configurer un `JwtAuthenticationToken` dans le `TestSecurityContext`.
- ajouter un `@Bean` de type `OAuth2AuthenticationFactory` pour changer le type d'`Authentication` utilisé de `JwtAuthenticationToken` à `OAuthentication<OpenidClaimSet>`. [Exemple ici](https://github.com/ch4mpy/spring-addons/tree/master/samples/webmvc-jwt-oauthentication)
- mettre à jour le `@Controller` et les tests unitaires avec le nouveau type d'`Authentication`

#### 1.1.2. Users API
Cette API a pour but de retourner les roles d'un utilisateur donné. 

Voici les éléments de configuration à implémenter (utiliser la "Greetings API" ou [cet autre exemple](https://github.com/ch4mpy/spring-addons/tree/master/samples/webmvc-jwt-default)) :
- ajouter [`com.c4-soft.springaddons:spring-addons-webmvc-jwt-resource-server`](https://central.sonatype.com/artifact/com.c4-soft.springaddons/spring-addons-webmvc-jwt-resource-server/6.1.11) aux dépendances
- configuration `resource server` qui utilise les claims suivantes comme source pour les authorities Spring
  * `scope` en ajoutant le préfixe `SCOPE_`
  * `$['https://c4-soft.com/authorities']` sans préfixe
- Auht0 comme issuer

Il faut ensuite implémenter le endpoint qui expose en lecture les roles d'un utiisateur donné :
- exposer un endpoint REST GET pour le path `/v1/users/{email}/roles` qui retourne un DTO contenant une liste de roles
- rendre le endpoint accessible uniquement aux requêtes authorisées avec les authorities `SCOPE_roles:read` ou `USER_ROLES_EDITOR`
- jouer les tests unitaires pour valider votre l'implémentation.


### 1.2. BFFs
Les Backends For Frontends sont des middlewares sur le serveur configurés comme clients OAuth2 et faisant le pont entre une sécurité basée sur des sessions (front-ends web & mobile) et une basée sur des "access tokens" OAuth2 (resource serveurs).

Nous utiliserons `spring-cloud-gateway` avec le filtre `TokenRelay` et un starter Spring Boot pour la configuration OAuth2 "cliente". La même application Spring Boot sera instanciée trois fois avec des configurations légèrement différentes (une instance par front-end).

Se rendre sur https://start.spring.io pour générer un projet Maven / Java avec Spring Boot 3.1 et les dépendances suivantes :
- Gateway
- Lombok
- Spring Boot Actuator
- Spring Configuration Processor
- Spring Boot DevTools
- GraalVM Native Support

Une fois le projet dézippé, l'ajouter en tant que module du `backend` (le placer dans le répertoire `backend`, changer le parent et supprimer les ressources liées au Maven wrapper et à Git)

Spring-cloud-gateway étant une application réctive, ajouter des dépendances à :
- `com.c4-soft.springaddons:spring-addons-webflux-client`
- `com.c4-soft.springaddons:spring-addons-webflux-ressource-server`

Il faut ensuite fournir la configuration (changer le fichier properties en YAML) :
```yaml
scheme: http
oauth2-issuer: https://dev-ch4mpy.eu.auth0.com/
oauth2-client-id: change-me
oauth2-client-secret: change-me

gateway-uri: ${scheme}://localhost:${server.port}
greetings-api-uri: ${scheme}://localhost:7084
users-api-uri: ${scheme}://localhost:7085
# en dev: https://openid-training.c4-soft.com (pour le deep link Android), http://localhost:3002 ou http://localhost:3003
# en prod: https://openid-training.c4-soft.com
ui-host: http://localhost:3002
ui-path: /ui
# Rien pour le web (servi a travers la gateway) et "RedirectTo=301,${ui-host}${ui-path}" pour le mobile (ne pas oublier les guillemets)
ui-filters:
allowed-origins: http://localhost:3002, http://localhost:3003, https://localhost:3402, https://localhost:3403

server:
  port: 8080
  shutdown: graceful
  ssl:
    enabled: false

spring:
  config:
    import:
    - optional:configtree:/workspace/config/
    - optional:configtree:/workspace/secret/
  lifecycle:
    timeout-per-shutdown-phase: 30s
  security:
    oauth2:
      client:
        provider:
          oauth2:
            issuer-uri: ${oauth2-issuer}
        registration:
          authorization-code:
            authorization-grant-type: authorization_code
            client-id: ${oauth2-client-id}
            client-secret: ${oauth2-client-secret}
            provider: oauth2
            scope:
            - openid
            - profile
            - email
            - offline_access
  cloud:
    gateway:
      default-filters:
      - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin
      routes:
      - id: home
        uri: ${gateway-uri}
        predicates:
        - Path=/
        filters:
        - RedirectTo=301,${gateway-uri}${ui-path}
      - id: ui
        uri: ${ui-host}
        predicates:
        - Path=${ui-path}
        filters: ${ui-filters}
      - id: greetings-api
        uri: ${greetings-api-uri}
        predicates:
        - Path=/bff/v1/greetings/**
        filters:
        - TokenRelay=
        - SaveSession
        - StripPrefix=2
      - id: users-api
        uri: ${users-api-uri}
        predicates:
        - Path=/bff/v1/users/**
        filters:
        - TokenRelay=
        - SaveSession
        - StripPrefix=2
      - id: letsencrypt
        uri: https://cert-manager-webhook
        predicates:
        - Path=/.well-known/acme-challenge/**

com:
  c4-soft:
    springaddons:
      security:
        # Global OAuth2 configuration
        issuers:
        - location: ${oauth2-issuer}
          username-claim: $['https://c4-soft.com/user']['name']
          authorities:
          - path: $['https://c4-soft.com/authorities']
          - path: $.scope
            prefix: SCOPE_
        # OAuth2 client configuration
        client:
          client-uri: ${gateway-uri}
          security-matchers:
          - /login/**
          - /oauth2/**
          - /
          - /logout
          - /api/**
          - ${ui-path}/**
          permit-all:
          - /login/**
          - /oauth2/**
          - /
          - /api/**
          - ${ui-path}/**
          csrf: cookie-accessible-from-js
          post-login-redirect-path: ${ui-path}
          post-logout-redirect-path: ${ui-path}
          back-channel-logout-enabled: true
          oauth2-logout:
          - client-registration-id: authorization-code
            uri: ${oauth2-issuer}v2/logout
            client-id-request-param: client_id
            post-logout-uri-request-param: returnTo
        # OAuth2 resource server configuration
        csrf: disable
        statless-sessions: true
        cors:
        - path: /api/**
          allowed-origins: ${allowed-origins}
        permit-all:
        - /v3/api-docs/**
        - /actuator/health/readiness
        - /actuator/health/liveness
        - /.well-known/acme-challenge/**
            
management:
  endpoint:
    health:
      probes:
        enabled: true
  endpoints:
    web:
      exposure:
        include: '*'
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true

logging:
  level:
    root: INFO
    org:
      springframework:
        security: INFO
    
---
spring:
  config:
    activate:
      on-profile: ssl
  cloud:
    gateway:
      default-filters:
      - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin
      - SecureHeaders
server:
  ssl:
    enabled: true

scheme: https
```

### 1.3. Génération des Specs OpenAPI
Le `springdoc-openapi-maven-plugin` est configuré dans le pom parent. Il est activé, pour chaque projet, avec le profile Maven `openapi`.

Pour générer les fichiers JSON de spec OpenAPI, simplement éxécuter `mvn install -Popenapi`. Ils sont récupérés pendant la phase de tests d'intégration (au sens Maven) sur le endpoint `/v3/api-docs/` exposé par `springdoc-openapi-starter-webmvc-api` (ou `springdoc-openapi-starter-webflux-api` pour le BFF), qui n'est présent que lorsque le profile Maven `openapi` est activé.

### 1.4. Exécution Des Projets Spring Boot En Local
Préparer trois configurations d'exécution distinctes pour le BFF (port `7081` pour le front mobile, `7082` pour le back web et `7083` pour le front web)

Voici les properties à surcharger avant de lancer les BFFs en dev:
```properties
server.port=
oauth2-client-id=
oauth2-client-secret=
spring.cloud.gateway.default-filters="DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin"
ui-host=
ui-path=
ui-filters=
```

Pour les resource servers, seul le port est nécessaire (`7084` pour la greetings-api et `7085` pour celle des users)

Pour lancer sur `https`, [générer (et installer) un certificat auto-signé](https://github.com/ch4mpy/self-signed-certificate-generation), puis activer le profile Spring `ssl`.

## 2. Front-Ends
Concernant l'autorisation des requêtes, en applicant le pattern BFF, il n'y a rien d'autre à faire côté frontend que de maintenir une session et rediriger l'utilisateur vers les endpoints de login ou de logout sur le BFF qui s'occupe du reste.

### 2.1. SPAs Next.js
Nous allons créer deux applications distinctes:
- un "front-office" accessible uniquement aux utilisateurs déclarés dans Auth0 et qui doit permettre d'afficher un message personnalisé par le serveur avec l'identité (et le roles) de l'utilisateur
- un "back-office" accessible aux utilisateurs déclarés dans Keycloak et qui doit permettre de mettre à jour les rôles des utilisateurs

### 2.1.1. SPA Next.js Back-Office
- `npx create-next-app@latest web-back-office`
- `cd web-back-office`
- `basePath: '/back-office/web'` à la `nextConfig` (fichier `next.config.js`): l'application next sera servie à travers le BFF, à partir de `https://localhost:7082/back-office/web` (ou `https://openid-training.c4-soft.com/web-back-office/back-office/web` en prod)
- `npm i -D @openapitools/openapi-generator-cli`
- `npm i axios @mui/material @mui/icons-material @emotion/react @emotion/styled`
- dans le package.json, définir un port spécifique pour le back-office en dev: `"dev": "next dev -p 3002"`
- ajouter les scripts npm suivants au package.json: 
    * `"generate:bff-api": "npx openapi-generator-cli generate -i ../bff.openapi.json -g typescript-axios --type-mappings AnyType=any --type-mappings date=Date --type-mappings DateTime=Date --additional-properties=serviceSuffix=Api,npmName=@c4-soft/bff-api,npmVersion=0.0.1,stringEnums=true,enumPropertyNaming=camelCase,supportsES6=true,withInterfaces=true --remove-operation-id-prefix -o c4-soft/bff-api"`
    * `"generate:greetings-api": "npx openapi-generator-cli generate -i ../greetings-api.openapi.json -g typescript-axios --type-mappings AnyType=any --type-mappings date=Date --type-mappings DateTime=Date --additional-properties=serviceSuffix=Api,npmName=@c4-soft/greetings-api,npmVersion=0.0.1,stringEnums=true,enumPropertyNaming=camelCase,supportsES6=true,withInterfaces=true --remove-operation-id-prefix -o c4-soft/greetings-api"`
    * `"generate:users-api": "npx openapi-generator-cli generate -i ../users-api.openapi.json -g typescript-axios --type-mappings AnyType=any --type-mappings date=Date --type-mappings DateTime=Date --additional-properties=serviceSuffix=Api,npmName=@c4-soft/users-api,npmVersion=0.0.1,stringEnums=true,enumPropertyNaming=camelCase,supportsES6=true,withInterfaces=true --remove-operation-id-prefix -o c4-soft/users-api"`
    * `"api": "npm run generate:bff-api && npm run generate:greetings-api && npm run generate:users-api"`
- `npm i`
- créer un fichier `.env.development` contenant `NEXT_PUBLIC_BFF_BASE_PATH=https://localhost:7082/bff` et un autre `.env.production` contenant `NEXT_PUBLIC_BFF_BASE_PATH=https://openid-training.c4-soft.com/bff`
- créer un helper pour les trois libs clientes générées à partir des specs OpenAPI:
```ts
import { BFFApi, Configuration as BFFConfiguration } from "@/c4-soft/bff-api";
import {
  UsersApi,
  Configuration as UsersConfiguration,
} from "@/c4-soft/users-api";

const bffApiConf = new BFFConfiguration({
  basePath: process.env.NEXT_PUBLIC_BFF_BASE_PATH,
});
const usersApiConf = new UsersConfiguration({
  basePath: process.env.NEXT_PUBLIC_BFF_BASE_PATH + "/api/v1",
});

export class APIs {
  static readonly gateway = new BFFApi(bffApiConf);
  static readonly users = new UsersApi(usersApiConf);
}
```
- créer un composant "client" pour éditer les rôles d'un utilisateur donné
```tsx
"use client";

import Chip from "@mui/material/Chip";
import IconButton from "@mui/material/IconButton";
import Stack from "@mui/material/Stack";
import AddIcon from "@mui/icons-material/Add";
import { FormEvent, useState } from "react";
import { APIs } from "./apis";

export default function UserRolesManagement() {
  const [roles, setRoles] = useState([] as string[]);
  const [isUserUnkown, setUserUnknown] = useState(false);
  const [email, setEmail] = useState("");
  const [roleToAdd, setRoleToAdd] = useState("");

  function onEmailChanged(event: FormEvent<HTMLInputElement>) {
    const inputValue = event.currentTarget.value;
    setEmail(inputValue);
    APIs.users
      .getRoles(inputValue)
      .then((response) => {
        setRoles(response.data || []);
        setUserUnknown(false);
      })
      .catch(() => {
        setRoles([]);
        setUserUnknown(true);
      });
  }

  function updateRoles(newRoles: string[]) {
    setRoles(newRoles);
    APIs.users.updateRoles(email, newRoles).catch((reason) => {
      console.log(reason);
    });
  }

  function onAddRoleClicked() {
    if (!roleToAdd) {
      return;
    }
    updateRoles([...roles.filter((r) => r !== roleToAdd), roleToAdd]);
    setRoleToAdd("");
  }

  return (
    <div>
      <div>
        <label htmlFor="email">e-mail : </label>
        <input
          type="email"
          id="email"
          name="email"
          required
          value={email}
          onChange={onEmailChanged}
        />
      </div>
      <div>
        <label htmlFor="newRole">nouveau role : </label>
        <input
          type="text"
          id="newRole"
          name="newRole"
          required
          value={roleToAdd}
          disabled={isUserUnkown}
          onChange={(event: FormEvent<HTMLInputElement>) =>
            setRoleToAdd(event.currentTarget.value)
          }
          onKeyDown={(event) => {
            if (event.key === "Enter") {
              onAddRoleClicked();
            }
          }}
        />
        <IconButton
          color="primary"
          aria-label="add role"
          type="submit"
          disabled={isUserUnkown || !roleToAdd}
          onClick={onAddRoleClicked}
        >
          <AddIcon />
        </IconButton>
      </div>
      <Stack direction="row" spacing={1}>
        {roles.map((role) => (
          <Chip
            key={role}
            label={role}
            variant="outlined"
            onDelete={() => updateRoles(roles.filter((r) => r !== role))}
          />
        ))}
      </Stack>
    </div>
  );
}
```
- créer un composant client `Body` pour gérer un état `currentUser`, les login / logout et un contenu conditionnel (formulaire d'édition de roles d'un utilisateur et logout pour les utilisateurs identifiés ou juste login)
```tsx
"use client";

import { useEffect, useState } from "react";
import Button from "@mui/material/Button";
import { APIs } from "./apis";
import UserRolesManagement from "./user-roles-management";

export class User {
  constructor(
    public name: string,
    public email: string,
    public roles: string[]
  ) {}

  get isAuthenticated(): boolean {
    return !!this.email;
  }

  static readonly ANONYMOUS = new User("", "", []);
}

export default function UserSession() {
  const [currentUser, setCurrentUser] = useState(User.ANONYMOUS);

  async function login() {
    const response = await APIs.gateway.getLoginOptions();
    if (response.data?.length < 1) {
      return;
    }
    document.location = response.data[0].loginUri;
  }

  function logout() {
    setCurrentUser(User.ANONYMOUS);
    APIs.gateway.logout().then((response) => {
      document.location = response.headers.location;
    });
  }

  useEffect(() => {
    APIs.users.getInfo().then((response) => {
      const user =
        response.status === 200
          ? new User(
              response.data.name,
              response.data.email,
              response.data.roles
            )
          : User.ANONYMOUS;
      setCurrentUser(user);
    });
  }, []);

  const content = currentUser.isAuthenticated ? (
    <div>
      <UserRolesManagement />
      <Button variant="outlined" color="primary" onClick={logout}>
        Logout
      </Button>
    </div>
  ) : (
    <Button variant="outlined" color="primary" onClick={login}>
      Login
    </Button>
  );

  return (
    <div className="z-10 w-full max-w-5xl items-center justify-between font-mono text-sm lg:flex">
      {content}
    </div>
  );
}
```
- remplacer le contenu de la "page" par le `Body`
```tsx
import Body from "./body";

export default async function Home() {
  console.log("process.env: ", process.env);

  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <div>
        <h1>Formation OpenID</h1>
        <h2>Back-Office Next.js</h2>
      </div>
      <Body />
    </main>
  );
}
```

### 2.1.2. SPA Next.js Front-Office
- reproduire l'initialisation de l'application précédente pour le `web-front-office` (création du projet, dépendances npm et génération des libs clientes d'APIs). Attention, le port du BFF doit être `7083` pour le front web et il faut choisir un autre port pour l'exécution en dev si pour pouvoir lancer simultanément le front et le back: `"dev": "next dev -p 3003"`.
- copier également les fichiers d'env, ainsi que la classe helper pour les APIs (en ajoutant la `greetings-api`) et le composant gérant les login & logout
- ajouter un composant client pour afficher le "greeting" retourné par l'API pour l'utilisateur courant:
```tsx
"use client";

import { useEffect, useState } from "react";
import { APIs } from "./apis";

export default function Greeting() {
    const [greetingMessage, setGreetingMessage] = useState("");

    useEffect(() => {
        APIs.greetings.getGreeting().then(response => {
        setGreetingMessage(currentMessage => {
            return response.data?.message || ""
          });
        });
    });
    
    return (<h1>{greetingMessage}</h1>);
}
```
- `body.tsx` pour le front:
```tsx
"use client";

import { useEffect, useState } from "react";
import Button from "@mui/material/Button";
import { APIs } from "./apis";
import Greeting from "./greeting";

export class User {
  constructor(
    public name: string,
    public email: string,
    public roles: string[]
  ) {}

  get isAuthenticated(): boolean {
    return !!this.email;
  }

  static readonly ANONYMOUS = new User("", "", []);
}

export default function Body() {
  const [currentUser, setCurrentUser] = useState(User.ANONYMOUS);

  async function login() {
    const response = await APIs.gateway.getLoginOptions();
    if (response.data?.length < 1) {
      return;
    }
    document.location = response.data[0].loginUri;
  }

  function logout() {
    setCurrentUser(User.ANONYMOUS);
    APIs.gateway.logout().then((response) => {
      document.location = response.headers.location;
    });
  }

  useEffect(() => {
    APIs.users.getInfo().then((response) => {
      const user =
        response.status === 200
          ? new User(
              response.data.name,
              response.data.email,
              response.data.roles
            )
          : User.ANONYMOUS;
      setCurrentUser(user);
    });
  }, []);

  return (
    <div className="z-10 w-full max-w-5xl items-center justify-between font-mono text-sm lg:flex">
      <Greeting />
      {currentUser.isAuthenticated ? (
        <div>
          <Button variant="outlined" color="primary" onClick={logout}>
            Logout
          </Button>
        </div>
      ) : (
        <Button variant="outlined" color="primary" onClick={login}>
          Login
        </Button>
      )}
    </div>
  );
}
```
- la "page" est très similaire à celle du back:
```tsx
import Body from "./body";

export default async function Home() {
  console.log("process.env: ", process.env);

  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <div>
        <h1>Formation OpenID</h1>
        <h2>Front-Office Next.js</h2>
      </div>
      <Body />
    </main>
  );
}
```

### 2.2. Application Mobile React Native (Android)
- `npx react-native@latest init mobile_front_office`
- `cd mobile_front_office`
- `npm i -D @openapitools/openapi-generator-cli react-devtools`
- `npm i axios react-native-url-polyfill`
- ajouter `import 'react-native-url-polyfill/auto';` à l'App.tsx
- ajouter les scripts npm suivants:
```json
  {
    "api": "npm run generate:bff-api && npm run generate:greetings-api && npm run generate:users-api && cd ./c4-soft/bff-api && npm run build && cd ../greetings-api && npm run build && cd ../users-api && npm run build",
    "generate:bff-api": "npx openapi-generator-cli generate -i ../bff.openapi.json -g typescript-axios --type-mappings AnyType=any --type-mappings date=Date --type-mappings DateTime=Date --additional-properties=serviceSuffix=Api,npmName=@c4-soft/bff-api,npmVersion=0.0.1,stringEnums=true,enumPropertyNaming=camelCase,supportsES6=true,withInterfaces=true --remove-operation-id-prefix -o c4-soft/bff-api",
    "generate:greetings-api": "npx openapi-generator-cli generate -i ../greetings-api.openapi.json -g typescript-axios --type-mappings AnyType=any --type-mappings date=Date --type-mappings DateTime=Date --additional-properties=serviceSuffix=Api,npmName=@c4-soft/greetings-api,npmVersion=0.0.1,stringEnums=true,enumPropertyNaming=camelCase,supportsES6=true,withInterfaces=true --remove-operation-id-prefix -o c4-soft/greetings-api",
    "generate:users-api": "npx openapi-generator-cli generate -i ../users-api.openapi.json -g typescript-axios --type-mappings AnyType=any --type-mappings date=Date --type-mappings DateTime=Date --additional-properties=serviceSuffix=Api,npmName=@c4-soft/users-api,npmVersion=0.0.1,stringEnums=true,enumPropertyNaming=camelCase,supportsES6=true,withInterfaces=true --remove-operation-id-prefix -o c4-soft/users-api"
  }
```
- `npm i`
- `npx react-native start`
- `npm i react-native-app-auth jwt-decode` (configuration en tant que "public" client OAuth2, pas "frontend" d'un BFF)

- ajouter l'intent-filter suivant à la main-activity (`autoVerify` important, de même que l'enregistrement du domaine sur la [searcrh-console Google](https://search.google.com/search-console/welcome))
```xml
      <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <!-- Accepts URIs that begin with "https://mobile.front-office.openid-training.c4-soft.com/callback” -->
        <data android:scheme="https"
          android:host="mobile.front-office.openid-training.c4-soft.com"
          android:pathPrefix="/callback”" />
      </intent-filter>
```

```tsx
import React, {useEffect, useState} from 'react';
import {
  Button,
  Linking,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import 'react-native-url-polyfill/auto';
import {APIs, bffApiConf} from './apis';

class User {
  constructor(
    readonly email: string,
    readonly name: string,
    readonly roles: string[],
  ) {}

  get isAuthenticated(): boolean {
    return !!this.email;
  }
}
const ANONYMOUS = new User('', '', []);

function App(): JSX.Element {
  const [currentUser, setCurrentUser] = useState(ANONYMOUS);
  const [greeting, setGreeting] = useState('');

  function login() {
    console.log('Get login options at: ', bffApiConf.basePath);
    APIs.gateway.getLoginOptions().then(resp => {
      if (resp.data.length > 0) {
        console.log('Login at: ', resp.data[0]);
        Linking.openURL(resp.data[0].loginUri);
      } else {
        console.warn('No login option. Already logged-in?');
      }
    });
  }

  function logout() {
    console.log('logout');
    APIs.gateway.logout();
    setCurrentUser(ANONYMOUS);
  }

  function refresh() {
    console.log('Get user-info');
    return APIs.users
      .getInfo()
      .then(userInfoResp => {
        console.log('Set user with: ', userInfoResp.data);
        setCurrentUser(
          new User(
            userInfoResp.data.email,
            userInfoResp.data.name,
            userInfoResp.data.roles,
          ),
        );
      })
      .catch(error => {
        console.log('Failed to get userInfo: ', error);
        setCurrentUser(ANONYMOUS);
      })
      .then(() => {
        console.log('Get greeting');
        APIs.greetings
          .getGreeting()
          .then(greetingResp => {
            console.log('Got greeting: ', greetingResp.data);
            setGreeting(greetingResp.data.message);
          })
          .catch(error => {
            console.log('Failed to get greeting: ', error);
            setGreeting('Bug!!!');
          });
      });
  }

  useEffect(() => {
    refresh();
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar />
      <ScrollView contentInsetAdjustmentBehavior="automatic">
        <Text>Formation OpenID</Text>
        <Text>Front-End React Native</Text>
        <View>
          <Text>{greeting}</Text>
          {!currentUser.isAuthenticated ? (
            <Button onPress={login} title="Login" />
          ) : (
            <Button onPress={logout} title="Logout" />
          )}
          <Button onPress={refresh} title="Refresh" />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});

export default App;
```
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
  * ajouter une "API" nommée `OpenID Training users API` avec `https://web.back-office.openid-training.c4-soft.com/api/v1/users`
  * dans l'onglet `Permissions`, ajouter  `roles:read`
- déclarez les "applications" suivantes (ce sont en réalité des clients OAuth2 que nous configurons ici):
  * `OpenID Training BFF back-office` (Regular Web Application)
  * `OpenID Training BFF front-office` (Regular Web Application)
  * `OpenID Training BFF mobile` (Regular Web Application)
  * `OpenID Training users roles API` (Machine to Machine). Dans l'onglet `APIs`, activer `OpenID Training users API`, puis déplier le détail de cette API pour activer la permission `roles:read`
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
  * dans l'onglet `Applications`, n'activer que ce connecteur pour le client `OpenID Training BFF back-office`
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
- ajouter une seconde action `Read roles from OpenID-training users API` (également définir les secrets `M2M_ROLES_ID` et `M2M_ROLES_SECRET` dans le menu sur la gauche de l'éditeur, en utilisant les valeurs fournies pour le client "Machine to Machine")
```typescript
const axios = require('axios');

exports.onExecutePostLogin = async (event, api) => {
  // console.log(event.user.email)

  const namespace = 'https://c4-soft.com'
  const audience = 'https://web.back-office.openid-training.c4-soft.com'
  const tokenUri = 'https://dev-ch4mpy.eu.auth0.com/oauth/token'
  const rolesUri = `https://web.back-office.openid-training.c4-soft.com/api/v1/users/${event.user.email}/roles`
  

  //Request the access token
  const tokenRequest = {
    method: "POST",
    url: tokenUri,
    headers: { "content-type": "application/json" },
    data: `{
      "client_id":"${event.secrets.M2M_ROLES_ID}",
      "client_secret":"${event.secrets.M2M_ROLES_SECRET}",
      "audience":"${audience}",
      "grant_type":"client_credentials"
    }`,
  };
  const tokenRes = await axios(tokenRequest).catch((err) => {
    console.log(err);
    return err; // FIXME: remove for prod
  });
  const access_token = tokenRes.data.access_token;
  // console.log("GET token: ", tokenRes.status, " data: ", tokenRes.data);

  const userRolesRequest = {
    method: "GET",
    url: rolesUri,
    headers: {
      "content-type": "application/json",
      Authorization: `Bearer ${access_token}`,
    },
  };
  // console.log("GET roles at ", rolesUri)
  const rolesRes = await axios(userRolesRequest).catch((err) => {
    console.log(err);
    return err; // FIXME: remove for prod
  });
  // console.log("GET roles", rolesRes.status, " data: ", rolesRes.data);

  api.idToken.setCustomClaim(`${namespace}/authorities`, rolesRes.data);
};
```
