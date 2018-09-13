(ns funcade.codec
  (:require [jsonista.core :as json]
            [cuerdas.core :as s])
  (:import [java.util.zip Inflater GZIPOutputStream GZIPInputStream]
           [java.io ByteArrayOutputStream ByteArrayInputStream]
           [java.util Base64]))

(defn inflate! [^bytes bytes]
  (try
    (let [inflater (doto (Inflater.) (.setInput bytes))
          baos (ByteArrayOutputStream. (alength bytes))
          buf  (byte-array 1024)]
      (while (not (.finished inflater)) (.write baos buf 0 (.inflate inflater buf)))
      (.end inflater)
      {:funcade/ok (.toByteArray baos)})
    (catch Exception e
      {:funcade/error e})))

(defn gzip! [^bytes bytes]
  (let [baos (ByteArrayOutputStream. (alength bytes))]
    (with-open [gzos (GZIPOutputStream. baos)]
      (.write gzos bytes))
    {:funcade/ok (.toByteArray baos)}))

(defn gunzip! [^bytes bytes]
  (let [baos (ByteArrayOutputStream.)
        bais (ByteArrayInputStream. bytes)
        buf  (byte-array 512)]
    (try
      (with-open [gzis (GZIPInputStream. bais)]
        (loop [len (.read gzis buf)]
          (when-not (neg? len)
            (.write baos buf 0 len)
            (recur (.read gzis buf)))))
      {:funcade/ok (.toByteArray baos)}
      (catch Exception ex
        {:funcade/error ex}))))

(defn decode64 [^String s]
  (.decode (Base64/getDecoder) s))

(defn encode64 [^bytes bs]
  (.encodeToString (Base64/getEncoder) bs))

(def mapper (json/object-mapper {:decode-key-fn keyword}))

(defn underscore-case->kebab-case [s]
  (some-> s s/lower (s/replace "_" "-")))

(def underscore->kebab-mapper (json/object-mapper {:decode-key-fn (comp keyword underscore-case->kebab-case)}))

(defn parse-json [^String s]
  (json/read-value s mapper))


