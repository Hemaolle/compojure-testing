(ns compojure-test.core
  (require [compojure.core]
           [compojure.response]
           [ring.util.response]
           [ring.mock.request]))

(defn my-map-render [body _]
      (-> (ring.util.response/response body)
        (ring.util.response/content-type "text/html; charset=utf-8")))

; Trying to override protocol implementation for rendering maps.
(extend-protocol compojure.response/Renderable
  clojure.lang.IPersistentMap
    (render [body _]
      (my-map-render body nil)))

; Implmenting rending of a vector to test that protocol implmentation works like this.
(extend-protocol compojure.response/Renderable
  clojure.lang.PersistentVector
    (render [body _]
      (-> (ring.util.response/response body)
        (ring.util.response/content-type "text/html; charset=utf-8"))))

(compojure.core/defroutes my-routes
  (compojure.core/GET "/map" [] {:msg "Hello foo"})
  (compojure.core/GET "/vector" [] [{:msg "Hello foo"}]))

(defn -main
  []
  (println (my-map-render {:msg "Hello foo"} nil))
  ; prints {:status 200, :headers {Content-Type text/html; charset=utf-8}, :body {:msg Hello foo}}
  ; This is how I'd like to render maps now, the rendering function works as expected.

  (println (my-routes (ring.mock.request/request :get "/map")))
  ; prints {:status 200, :headers {}, :body , :msg Hello foo}
  ; Overriding the protocol implementation failed, by default compojure
  ; merges maps to the response map :(
  
  (println (my-routes (ring.mock.request/request :get "/vector")))
  ; prints {:status 200, :headers {Content-Type text/html; charset=utf-8}, :body [{:msg Hello foo}]}
  ; Implementing rendering vectors worked.
  )
