(ns meuse.auth.frontend
  (:require [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [crypto.random :as random])
  (:import java.util.Base64
           java.util.UUID
           javax.crypto.Cipher
           javax.crypto.spec.SecretKeySpec))

(def random-str-byte-size 15)
(def uuid-size 36)
(def timestamp-size 13)
(def expired-hours 24)

(defn secret-key-spec
  "Creates SecretKeySpec instance from a secret key."
  [secret-key]
  (SecretKeySpec. (.getBytes secret-key "UTF-8") "AES"))

(defn encrypt
  "Encrypt a string using the key spec."
  [value ^SecretKeySpec key-spec]
  (let [cipher (doto (Cipher/getInstance "AES/ECB/PKCS5Padding")
                     (.init Cipher/ENCRYPT_MODE key-spec))]
    (.encodeToString (Base64/getEncoder)
                     (.doFinal cipher (.getBytes value "UTF-8")))))

(defn decrypt
  "Decrypt a string using the key spec."
  [token ^SecretKeySpec key-spec]
  (let [cipher (doto (Cipher/getInstance "AES/ECB/PKCS5Padding")
                     (.init Cipher/DECRYPT_MODE key-spec))]
    (String. (.doFinal cipher (.decode (Base64/getDecoder) token)))))

(defn generate-token
  "Generate a token for an user."
  [user-id ^SecretKeySpec key-spec]
  (-> (str user-id (System/currentTimeMillis) (random/hex random-str-byte-size))
      (encrypt key-spec)))

(defn extract-data
  "Extract the data from a token"
  [decrypted-token]
  {:user/id (subs decrypted-token 0 uuid-size)
   :timestamp (-> (subs decrypted-token uuid-size (+ uuid-size timestamp-size))
                  (Long.)
                  (coerce/from-long))})

(defn expired?
  "Check if a token is expired"
  [{:keys [timestamp]}]
  (not (time/within? (time/interval (time/minus (time/now)
                                                (time/hours expired-hours))
                                    (time/now))
                     timestamp)))