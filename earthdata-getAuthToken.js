/**
 * Bearer token refresh script for NASA Earthdata (URS) authentication.
 *
 * Paste this into the JavaScript editor of the ECtransSetup (Host) configuration.
 *
 * Required host options:
 *   http.login    - Earthdata username
 *   http.password - Earthdata password
 *
 * Optional host options:
 *   http.tokenLookahead - seconds before expiry to pre-refresh (default: 60)
 *
 * The function name must match http.tokenFunction (default: "getAuthToken").
 *
 * Virtual keys available via setup.get():
 *   http.login      - username from the host location
 *   http.password   - password from the host location
 *   http.basicAuth  - pre-computed "Basic <base64(login:password)>" header value
 */
function getAuthToken() {
    var r = http.post(
        "https://urs.earthdata.nasa.gov/api/users/find_or_create_token",
        {
            "Authorization": setup.get("http.basicAuth"),
            "User-Agent":    "Java-Earthdata-Client"
        },
        ""   // no body, same as BodyPublishers.noBody()
    );

    if (r.status !== 200) {
        throw new Error("Token request failed with HTTP " + r.status + ": " + r.body);
    }

    var json = JSON.parse(r.body);

    // expiration_date comes back as "MM/DD/YYYY" — convert to Unix ms timestamp
    var parts = json.expiration_date.split("/");
    var expiry = new Date(
        parseInt(parts[2]),
        parseInt(parts[0]) - 1,
        parseInt(parts[1])
    ).getTime();

    return {
        http: {
            tokenValue:  json.token_type + " " + json.access_token,
            tokenExpiry: expiry
        }
    };
}
