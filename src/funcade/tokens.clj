(ns funcade.tokens
  (:require [clojure.core.async :as a]
            [org.httpkit.client :as http]
            [jsonista.core :as json]
            [cuerdas.core :as s]
            [funcade.codec :as codec]
            [com.rpl.specter :as sp]
            [funcade.state :refer [state]])
  (:import [java.time Instant]))

(defn in-open-interval? [begin end value] (< begin value end))

(defn token-in-interval? [{:keys [issued expires]} time-in-seconds]
  (when expires
    (< time-in-seconds expires)))

(defn renew-token? [{:keys [issued expires] :as m} now percentage]
  (assert (in-open-interval? 0 1 percentage) (str "percentage is not in (0,1) interval: " percentage))
  (let [diff  (- expires now)
        delta (Math/abs diff)
        p     (/ delta (Math/abs (- expires issued)))]
    (or (< delta 60)
        (neg? diff)
        (< p percentage))))

(defn token-valid? [m]
  (token-in-interval? m (.getEpochSecond (Instant/now))))

(defn parse-token-data [{:keys [access-token]}]
  (-> access-token
      (s/split ".")
      second
      codec/decode64
      String.
      codec/parse-json
      (select-keys [:iat :exp])
      (clojure.set/rename-keys {:iat :issued :exp :expires})))

(defn prepare-token [[token err :as r]]
  (if err
    r
    (let [data token
          t    (merge data (parse-token-data data))]
      (if-not (token-valid? t)
        [nil (ex-info "token has expired" t)]
        [t nil]))))

(defn new-token! [{:keys [token-url grant-type client-id client-secret scope token-headers]}]
  (let [xf (fn [{:keys [status body error]}]
             (if (and (= status 200) (not error))
               [(json/read-value body codec/underscore->kebab-mapper) nil]
               [nil {:status status :body body :error error}]))
        ch (a/promise-chan (map xf))
        payload (s/join "&" (map (fn [[k v]] (str (name k) "=" v)) {:grant_type grant-type
                                                                    :client_id client-id
                                                                    :client_secret client-secret
                                                                    :scope scope} ))]
    (http/request {:url token-url :method :post :headers (sp/transform [sp/MAP-KEYS] name token-headers) :body payload} #(a/put! ch %))
    ch))

(defn schedule-token-renewal [name-of-job token-key should-renew? new-token! stop-ch]
  (println "Starting token refresh poll" "name-of-job" name-of-job "token-key" token-key)
  (a/go-loop []
             (println "running" name-of-job "token renewal..." token-key)
             (let [now    (fn [] (.getEpochSecond (Instant/now)))
                   [_ ch] (a/alts! [stop-ch (a/timeout 60000)])]
               (cond (= ch stop-ch) (println "stopping" name-of-job "token poller...")
                     (should-renew? (get @state token-key)
                                    (now)) (let [[token err] (a/<! (new-token!))]
                                             (if err
                                               (println "couldn't renew token for" name-of-job err)
                                               (do
                                                 (println "renewing token:" token)
                                                 (swap! state (fn [s] (assoc s token-key token)))))
                                             (recur))
                     :else (recur)))))
