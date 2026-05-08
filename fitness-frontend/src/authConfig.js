const keycloakUrl = import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8181'
const keycloakRealm = import.meta.env.VITE_KEYCLOAK_REALM || 'fitness'

export const authConfig = {
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'oauth2-pkce-client',
  authorizationEndpoint: `${keycloakUrl}/realms/${keycloakRealm}/protocol/openid-connect/auth`,
  tokenEndpoint: `${keycloakUrl}/realms/${keycloakRealm}/protocol/openid-connect/token`,
  redirectUri: import.meta.env.VITE_AUTH_REDIRECT_URI || window.location.origin,
  scope: import.meta.env.VITE_KEYCLOAK_SCOPE || 'openid profile email offline_access',
  onRefreshTokenExpire: (event) => event.logIn(),
}
