# funcade

A Clojure library designed to grab tokens from OAuth 2.0

## Usage


```clojure
(:require [funcade/core :as f])

...
(def token-store (f/wake-token-master "my-token-key" config))

...
(def token ((:next-token token-store)))

...
((:stop token-store))

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
# funcade
