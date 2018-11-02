# funcade

A Clojure library designed to grab and auto renew tokens from OAuth 2.0

[![<! release](https://img.shields.io/badge/dynamic/json.svg?label=release&url=https%3A%2F%2Fclojars.org%2Ffuncade%2Flatest-version.json&query=version&colorB=blue)](https://github.com/shvetsm/funcade/releases)
[![<! clojars](https://img.shields.io/clojars/v/funcade.svg)](https://clojars.org/funcade)

## Usage

```clojure
user=> (require '[funcade.core :as f])

user=> (def conf {:client-id "planet-earth"
                  :scope "solar-system"
                  :token-headers {:Content-Type "application/x-www-form-urlencoded"}
                  :token-url "https://milky-way-galaxy/token.oauth2"
                  :grant-type "client_credentials"
                  :client-secret "super-hexidecimal-secret"})
#'user/conf
```

```clojure
user=> (def token-repo (f/wake-token-master :serpens conf))
#'user/token-repo

user=> (f/current-token token-repo)

;; just an example token, it would look something like this (but a bit longer):

"dXNlcj0+IChyZXF1aXJlICdbZnVuY2FkZS5jb3JlIDphcyBmXSkgdXNlcj0+IChkZWYgY29uZiB7OmNsaWVudC1pZCAicGxhbmV0LWVhcnRoIiA6c2NvcGUgInNvbGFyLXN5c3RlbSIgOnRva2VuLWhlYWRlcnMgezpDb250ZW50LVR5cGUgImFwcGxpY2F0aW9uL3gtd3d3LWZvcm0tdXJsZW5jb2RlZCJ9IDp0b2tlbi11cmwgImh0dHBzOi8vbWlsa3ktd2F5LWdhbGF4eS90b2tlbi5vYXV0aDIiIDpncmFudC10eXBlICJjbGllbnRfY3JlZGVudGlhbHMiIDpjbGllbnQtc2VjcmV0ICJzdXBlci1oZXhpZGVjaW1hbC1zZWNyZXQifSkgIyd1c2VyL2NvbmY="
```

```clojure
user=> (f/stop token-repo)
true
```

where config is:

```clojure
  {:token-url OAuth 2.0 server url
   :grant-type OAuth 2.0 grant type (client_crdentials, implicit, etc)
   :client-id OAuth 2.0 client id
   :client-secret OAuth 2.0 secret (a hex string)
   :scope OAuth 2.0 Scope
   :token-headers a map of headers {"Cookie" "foo=bar"}
   :refresh-percent (when less than x% of time between expires-at and issued-at remains, refresh the token)}
```

## License

Copyright © 2018 shvetsm

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
