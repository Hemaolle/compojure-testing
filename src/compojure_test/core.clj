(ns compojure-test.core
  (require [compojure.core]
           [compojure.response]
           [ring.util.response]
           [ring.mock.request]
           [compojure-test.my-protocol]
           [compojure-test.my-protocol-user]))

(defn my-map-render
  [map _]
  "Just return a Ring response, the map should be the value of the :body.
  Set a content type too."
  (-> (ring.util.response/response map)
    (ring.util.response/content-type "text/html; charset=utf-8")))

(defn override-map-rendering
  []
  "Try to override how Compojure renders maps"
  (extend-protocol compojure.response/Renderable
    clojure.lang.IPersistentMap
      (render [body _]
        (my-map-render body nil))))

(defn implement-vector-rendering
  []
  "Compojure doesn't implement rendering of vectors"
  (extend-protocol compojure.response/Renderable
    clojure.lang.PersistentVector
      (render [body _]
        (-> (ring.util.response/response body)
          (ring.util.response/content-type "text/html; charset=utf-8")))))

(compojure.core/defroutes my-routes
  (compojure.core/GET "/map" [] {:msg "Hello foo"})
  (compojure.core/GET "/vector" [] [{:msg "Hello foo"}]))

(defn override-my-protocol-implementation
[]
  "Overrides our own protocol"
  (extend-protocol compojure-test.my-protocol/my-protocol
    clojure.lang.IPersistentMap
    (execute [map]
      (assoc map :value-3 3))))

(defn -main
  []
  (println (my-map-render {:msg "Hello foo"} nil))
  ; prints {:status 200, :headers {Content-Type text/html; charset=utf-8}, :body {:msg Hello foo}}
  ; This is how we'd like to render maps now, the rendering function works as expected.

  (println (my-routes (ring.mock.request/request :get "/map")))
  ; prints {:status 200, :headers {}, :body , :msg Hello foo}
  ; By default Compojure renders map by merging with the response map.

  (override-map-rendering)

  (println (my-routes (ring.mock.request/request :get "/map")))
  ; prints {:status 200, :headers {}, :body , :msg Hello foo}
  ; Overriding the protocol implementation failed, same result as before.
  

  ; Ok, let's test if we extended the protocol correctly:

  (try
    (println (my-routes (ring.mock.request/request :get "/vector")))
    (catch java.lang.IllegalArgumentException e (println (.getMessage e))))
  ; prints No implementation of method: :render of protocol:
  ;   #'compojure.response/Renderable found for class: clojure.lang.PersistentVector
  ; We got the exception as expected. Can't render the vector response
  ; yet as Compojure doesn't implelement vector rendering.

  (implement-vector-rendering)

  (println (my-routes (ring.mock.request/request :get "/vector")))
  ; prints {:status 200, :headers {Content-Type text/html; charset=utf-8}, :body [{:msg Hello foo}]}
  ; Implementing rendering vectors worked.


  ; Let's try overriding an existing implementation with a protocol of
  ; our own:

  (println (compojure-test.my-protocol-user/use-protocol {:value 1}))
  ; prints {:value 1, :value-2 2}
  ; This is the original implementation in compojure-test.my-protocol

  (override-my-protocol-implementation)

  (println (compojure-test.my-protocol-user/use-protocol {:value 1}))
  ; prints {:value 1, :value-3 3}
  ; Successfully overrode the implementation!
  )
