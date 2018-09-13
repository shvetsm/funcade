(ns funcade.core
  (:require [clojure.core.async :as a]
            [clojure.tools.logging :as log]
            [funcade.state :refer [state]]
            [funcade.tokens :as t]))

(defn init-token-channel!
  "funcade-params are
  {:token-url OAuth 2.0 server url
   :grant-type OAuth 2.0 grant type (client_crdentials, implicit, etc)
   :client-id OAuth 2.0 client id
   :client-secret OAuth 2.0 secret (a hex string)
   :scope OAuth 2.0 Scope
   :token-headers a map of headers {\"Cookie\" \"foo=bar\"}
  :expires-percentage percent of token left-to-live time before requesting a new token}"
  [token-key funcade-params]
  (let [stop-chan (a/chan 10)
        [token err] (a/<!! (t/new-token! funcade-params))]
    (if err
      (do
        (log/error "can't acquire token!" err)
        (throw err))
      (do
        (swap! state (fn [s] (assoc s token-key token)))
        (t/schedule-token-renewal
          (name token-key)
          token-key
          (partial t/renew-token? (:expires-percentage funcade-params))
          (fn [] (t/new-token! funcade-params))
          stop-chan)
        {:token-details (:body token) :stop-chan stop-chan}))))

(defn stop-token-channel! [{:keys [stop-chan]}]
  (a/put! stop-chan ::stop))