(ns meuse.db.token-test
  (:require [meuse.db :refer [database]]
            [meuse.db.token :as token-db]
            [meuse.helpers.fixtures :refer :all]
            [clj-time.core :as t]
            [crypto.password.bcrypt :as bcrypt]
            [clojure.test :refer :all])
  (:import org.joda.time.DateTime
           clojure.lang.ExceptionInfo))

(use-fixtures :once db-fixture)
(use-fixtures :each table-fixture)

(deftest create-get-token-test
  (testing "success"
    (let [user-name "user2"
          validity 10
          token-name "mytoken"
          token (token-db/create-token database {:user user-name
                                                 :validity validity
                                                 :name token-name})]
      (let [[db-token :as tokens] (token-db/get-user-tokens database "user2")]
        (is (= 1 (count tokens)))
        (is (uuid? (:token-id db-token)))
        (is (= token-name (:token-name db-token)))
        (is (t/within? (t/minus (t/now) (t/minutes 1))
                       (t/now)
                       (DateTime. (:token-created-at db-token))))
        (is (t/within? (t/plus (t/minus (t/now) (t/minutes 1)) (t/days validity))
                       (t/plus (t/now) (t/days validity))
                       (DateTime. (:token-expired-at db-token))))
        (is (bcrypt/check token (:token-token db-token))))))
  (testing "errors"
    (is (thrown-with-msg?
         ExceptionInfo
         #"the user toto does not exist"
         (token-db/create-token database {:user "toto"
                                          :validity 10
                                          :name "foo"})))))

(deftest delete-token-test
  (testing "success"
    (let [user-name "user2"
          validity 10
          token-name "mytoken"
          token (token-db/create-token database {:user user-name
                                                 :validity validity
                                                 :name token-name})]
      (token-db/delete-token database user-name token-name)
      (is (= 0 (count (token-db/get-user-tokens database "user2"))))))
  (testing "errors"
    (is (thrown-with-msg?
         ExceptionInfo
         #"the user toto does not exist"
         (token-db/delete-token database "toto" "foo")))
    (is (thrown-with-msg?
         ExceptionInfo
         #"the token foo does not exist for the user user2"
         (token-db/delete-token database "user2" "foo")))))