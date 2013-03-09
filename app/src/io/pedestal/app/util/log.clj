;; Copyright (c) 2012 Relevance, Inc. All rights reserved.

(ns io.pedestal.app.util.log
  (:require clojure.string)
  (:import [org.slf4j LoggerFactory]))

(defn- log-expr [form level keyvals]
  ;; Pull out :exception, otherwise preserve order
  (let [exception' (:exception (apply array-map keyvals))
        keyvals' (mapcat identity (remove #(= :exception (first %))
                                          (partition 2 keyvals)))
        logger' (gensym "logger")  ; for nested syntax-quote
        string' (gensym "string")
        enabled-method' (symbol (str ".is"
                                     (clojure.string/capitalize (name level))
                                     "Enabled"))
        log-method' (symbol (str "." (name level)))]
    `(let [~logger' (LoggerFactory/getLogger ~(name (ns-name *ns*)))]
       (when (~enabled-method' ~logger')
         (let [data# (array-map :line ~(:line (meta form)) ~@keyvals')
               ~string' (binding [*print-length* 80] (pr-str data#))]
           ~(if exception'
              `(~log-method' ~logger' ~string' ~exception')
              `(~log-method' ~logger' ~string')))))))

(defmacro trace [expr] (log-expr &form :trace [:expr expr]))

(defmacro debug [& keyvals] (log-expr &form :debug keyvals))

(defmacro info [& keyvals] (log-expr &form :info keyvals))

(defmacro warn [& keyvals] (log-expr &form :warn keyvals))

(defmacro error [& keyvals] (log-expr &form :error keyvals))
