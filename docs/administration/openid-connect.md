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

The provider does not perform discovery or dynamic registration.
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
userIdClaim=preferred_username

[InteractiveLoginMapping]
alice={"issuer":"https://identity.example.org/realms/operations","subject":"provider-stable-subject-for-alice"}
bob={"issuer":"https://identity.example.org/realms/operations","subject":"provider-stable-subject-for-bob"}
```

The secret substitution above requires a corresponding JVM system property;
arbitrary environment variables do not automatically become configuration values.
Supply secrets using your protected deployment configuration, not a committed
file. Do not put client secrets on the Monitor or in browser-visible settings.

Choose one account-matching mode:

- **Dynamic claim matching:** Set `userIdClaim` to a trusted claim guaranteed
  unique and not reassigned by the identity provider. The claim's value must
  equal an existing local WebUser ID; its ECUser must also exist and the
  WebUser must be active. Account changes take effect on the next login without
  editing mappings or restarting the Master. The claim must be in the signed
  ID token, not only the access token or UserInfo response. Changing or
  reassigning a username can transfer access to another local account.
- **Explicit issuer/subject mapping:** Leave `userIdClaim` unset and configure
  `[InteractiveLoginMapping]`. This uses the stable OIDC issuer/subject pair
  and requires a configuration edit and Master restart when mappings change.

If both a claim and explicit mappings are configured, an exact issuer/subject
mapping takes precedence; otherwise, the configured claim is used. In either
mode, the Master checks the current local ECUser/WebUser records and active
status at login. A missing or invalid optional claim does not prevent an exact
issuer/subject mapping from working; an unmatched identity without a valid
claim is rejected. It never automatically provisions accounts or grants
permissions.

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
| `userIdClaim` | Optional claim from the verified ID token, such as `preferred_username`, used as the local ECPDS account ID. When set, the claim must be a non-empty string of at most 255 characters. |

All configured URLs must be absolute HTTPS URLs without user information,
fragments or query parameters. Provider requests do not follow redirects.
Connect/read timeouts are ten seconds and response bodies are limited to 512 KiB.
JWKS keys are cached using the JOSE library's cache and refresh/rate-limit handling.
The Master must trust the TLS certificates of the token and JWKS endpoints.

Restart the Master after configuration or mapping changes. The Master and Monitor
must use matching application versions for the interactive management methods.
The provider implementation runs on the Master application classpath; it is not
a `[PluginList]` server plugin.

Nimbus JOSE/JWT is required on the Master's runtime classpath, not the Monitor's.
The application packaging supplies this dependency. OIDC is independent of
ordinary password/passcode login and the optional ECCMD directory/gateway providers.

## Public and LAN Monitors sharing a DNS name

Two Monitor instances connected to one Master can share one OIDC client and one
callback URI when their browser-visible HTTPS URL is identical. With split DNS,
the same hostname can resolve to a public IP on the Internet and a private IP
on the LAN. The identity provider registers the URL, not those IP addresses.

For separate ECPDS service instances, configure one client on each Master:

| Service | Browser-visible Monitor URL | Registered callback URI |
|---|---|---|
| Service A | `https://monitor-a.example.org` | `https://monitor-a.example.org/do/interactiveCallback` |
| Service B | `https://monitor-b.example.org` | `https://monitor-b.example.org/do/interactiveCallback` |

Use the respective client ID, secret and `redirectUri` in each Master's
`[InteractiveLoginExtension]`; both services can use the same issuer if the
identity provider supplies it. Configure issuer/subject mappings separately on
each Master against that service's existing local accounts. Each Master supports
one interactive provider, client and callback URI.

Both Monitor instances must serve a trusted certificate for their shared DNS
name and expose `/do/interactiveLogin` and `/do/interactiveCallback` over HTTPS.
Each instance obtains its login button and provider configuration from its Master;
do not copy the OIDC client secret onto either Monitor.

The callback must reach **the same Monitor instance and browser session** that
started the login. Stable split-DNS routing satisfies this when a user stays on
the same network throughout sign-in. If a VIP/reverse proxy distributes requests
between Monitors, configure session affinity for the whole login flow. Switching
between LAN and Internet during sign-in, or failing over to the other Monitor,
loses the pending browser session; start sign-in again rather than sharing cookies
or accepting an unbound callback. The Monitor-to-Master management origin must
also remain stable for the transaction.

The browser must reach the authorization endpoint; the Master must reach the
token and JWKS endpoints. Ensure a browser returning from the identity provider
can resolve and reach the callback through the same public or LAN route.
Monitor HTTP sessions are independent; using the same OIDC client does not share
application sessions between instances.

## Account mapping and authorization

In the explicit-mapping example, `alice` and `bob` are existing local WebUser
IDs, not provider email addresses. Obtain each stable subject from the provider's
administrative tooling. For dynamic matching, the configured ID-token claim
must resolve directly to the local account ID. Do not infer identity from an
unverified token or email claim.

Each mapped account requires an existing ECUser and an active local WebUser. The local
anonymous account is excluded. Existing categories, URL permissions and
`[Restricted]` Monitor-origin restrictions continue to apply. Interactive login
does not create users, import directory users or assign privileges.

Explicit mappings are exact and case-sensitive on issuer and subject. An
identity assigned to multiple local users causes Master startup to fail. In
dynamic-claim mode, the claim value is matched exactly and case-sensitively to
the local account ID. Unknown, inactive or unauthorized accounts are rejected.
To change an explicit identity assignment, edit the mapping and restart the
Master; dynamic matching follows current local account state.

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
