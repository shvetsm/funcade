(ns funcade.core
  (:require [clojure.core.async :as a]
            [clojure.tools.logging :as log]
            [funcade.tokens :as t]))

(defn- stop-token-channel! [stop-chan]
  (a/put! stop-chan ::stop))

(defprotocol MasterTokens
  (current-token [_])
  (stop [_]))

(deftype TokenMaster [token-name tokens =stop=]
  MasterTokens
  (current-token [_]
    (-> @tokens token-name :access-token))
  (stop [_]
    (stop-token-channel! =stop=)))

(defn- init-token-channel!
  [token-key funcade-params token-store]
  (let [stop-chan (a/chan 10)
        [token err] (a/<!! (t/new-token! funcade-params))]
    (if err
      (do
        (log/error "can't acquire token!" err)
        (throw err))
      (do
        (swap! token-store (fn [s] (assoc s token-key token)))
        (t/schedule-token-renewal
          (name token-key)
          token-key
          (partial t/renew-token?  (/ (or (:refresh-percent funcade-params) 10) 100))
          (fn [] (t/new-token! funcade-params))
          stop-chan
          token-store)
        stop-chan))))

(defn wake-token-master [token-name config]
  "config is
  {:token-url OAuth 2.0 server url
   :grant-type OAuth 2.0 grant type (client_crdentials, implicit, etc)
   :client-id OAuth 2.0 client id
   :client-secret OAuth 2.0 secret (a hex string)
   :scope OAuth 2.0 Scope
   :token-headers a map of headers {\"Cookie\" \"foo=bar\"}
   :refresh-percent (when less than x% of time between expires-at and issued-at remains, refresh the token)}"

  (let [tokens (atom {})
        =stop= (init-token-channel! token-name config tokens)]
    (TokenMaster. token-name tokens =stop=)))

