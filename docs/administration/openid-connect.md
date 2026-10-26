# OpenID Connect Monitor login

OpenECPDS includes an optional OpenID Connect (OIDC) login provider. It is an
implementation of the [interactive login SPI](../concepts/plugin-infrastructure.md#interactive-login-extensions),
not an OAuth password grant or a replacement for Data Portal authentication.

The Master exchanges authorization codes, validates signed ID tokens and maps
the verified **issuer and subject** to an existing local account. The Monitor
handles browser redirects and creates its usual application session only after
the Master returns an authorized ECPDS session.

## Provider requirements

Register the Monitor as an OIDC authorization-code client with:

- An exact HTTPS callback URI, for example
  `https://monitor.example.org/do/interactiveCallback`.
- S256 PKCE support and query-mode authorization responses.
- RS256-signed ID tokens and an HTTPS JWKS endpoint.
- Public-client authentication, `client_secret_basic` or `client_secret_post`.

This initial implementation does not perform discovery or dynamic registration.
Configure the issuer and endpoints explicitly from your provider's trusted
metadata. Encrypted ID tokens, other signing algorithms, UserInfo-based identity
lookup, refresh-token storage and federated logout are not implemented.

## Master configuration

Add to the Master's local `ecmwf.properties` override:

```ini
[InteractiveLoginExtension]
class=ecmwf.ecpds.extension.oidc.OidcLoginProvider
displayName=Sign in with your organization
issuer=https://identity.example.org/realms/operations
authorizationEndpoint=https://identity.example.org/realms/operations/protocol/openid-connect/auth
tokenEndpoint=https://identity.example.org/realms/operations/protocol/openid-connect/token
jwksUri=https://identity.example.org/realms/operations/protocol/openid-connect/certs
clientId=ecpds-monitor
clientAuthentication=basic
clientSecret=${ecpds.oidc.clientSecret}
redirectUri=https://monitor.example.org/do/interactiveCallback

[InteractiveLoginMapping]
alice={"issuer":"https://identity.example.org/realms/operations","subject":"provider-stable-subject-for-alice"}
bob={"issuer":"https://identity.example.org/realms/operations","subject":"provider-stable-subject-for-bob"}
```

The secret substitution above requires a corresponding JVM system property;
arbitrary environment variables do not automatically become configuration values.
Supply secrets using your protected deployment configuration, not a committed
file. Do not put client secrets on the Monitor or in browser-visible settings.

| Option | Behavior |
|---|---|
| `class` | Blank/missing disables interactive login |
| `displayName` | Login button text; defaults to `Sign in with OpenID Connect` |
| `issuer` | Exact expected `iss` value, including any trailing slash |
| `authorizationEndpoint`, `tokenEndpoint`, `jwksUri` | Explicit trusted provider endpoints |
| `redirectUri` | Exact registered Monitor callback URI |
| `clientId` | Registered client identifier and expected ID-token audience |
| `clientAuthentication` | `basic`, `post` or `none`; defaults to `basic` when a nonblank secret exists, otherwise `none` |
| `clientSecret` | Required for `basic` and `post`; not needed for public clients |

All configured URLs must be absolute HTTPS URLs without user information,
fragments or query parameters. Provider requests do not follow redirects.
Connect/read timeouts are ten seconds and response bodies are limited to 512 KiB.
JWKS keys are cached using the JOSE library's cache and refresh/rate-limit handling.
The Master must trust the TLS certificates of the token and JWKS endpoints.

Restart the Master after configuration or mapping changes. The Master and Monitor
must use matching application versions for the interactive management methods.
The provider implementation runs on the Master application classpath; it is not
a `[PluginList]` server plugin.

## Account mapping and authorization

`alice` and `bob` above are existing local WebUser IDs, not provider email
addresses. Obtain the stable subject from the provider's administrative tooling.
Do not infer the identity from an unverified token or email claim.

Each account requires an existing ECUser and an active local WebUser. The local
anonymous account is excluded. Existing categories, URL permissions and
`[Restricted]` Monitor-origin restrictions continue to apply. Interactive login
does not create users, import directory users or assign privileges.

Mapping is exact and case-sensitive on issuer and subject. Unknown identities
are rejected. An identity assigned to multiple local users causes Master startup
to fail. To change identity assignment, edit the mapping and restart the Master.

OIDC is a separate login method: an interactive login does not additionally
require the local password or TOTP code. Enforce required MFA at the identity
provider. Existing local/TOTP/certificate login methods remain available; this
feature does not implement an SSO-only access policy.

## Browser flow and session behavior

The login page adds a button only when the Master reports an active interactive
provider. Clicking it sends a POST bound to the existing browser session.
The Master creates random state, nonce and a PKCE verifier; the browser receives
only the authorization request and S256 challenge.

Transactions expire after five minutes and are bound to the browser session and
the Monitor's management-connection origin. The Master holds at most 1,000
pending transactions. A callback consumes a matching transaction before exchanging
the code, so it cannot be retried or replayed. Start again after a failed callback.
Only one pending login is kept per browser session.

The ID token's signature, issuer, audience, expiry, issued-at time and nonce are
validated. Authorized-party (`azp`) is checked when present and required for
multiple audiences. Clock skew allowance is 60 seconds; keep the Master clock
synchronized.

On success, the Monitor replaces the pre-login HTTP session and redirects to
`/do/start`. It does not accept a callback-provided destination URL. Tokens and
client secrets are not stored in the browser session.

Use the normal HTTPS Monitor endpoint. Behind a proxy, ensure the Monitor sees
the request as secure using your trusted HTTPS forwarding setup. Callback
requests must return to the same Monitor/browser session; deployments with
multiple Monitor instances need session affinity. Master restarts invalidate
pending logins.

Local logout clears OpenECPDS login state but does not end the identity-provider
session. A subsequent sign-in may therefore complete without prompting for
credentials. Disable query-string logging for callback requests in reverse
proxies and access logs: the callback URL contains a short-lived authorization
code. Error messages deliberately do not include provider responses or tokens.
