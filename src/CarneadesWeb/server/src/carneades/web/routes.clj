;; Copyright (c) 2014 Fraunhofer Gesellschaft
;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.

(ns carneades.web.routes
  ^{:doc "Definition of routing domains."}
  (:require [compojure.core :refer [defroutes context GET]]
            [ring.util.response :as resp]
            [ring.middleware.format-response
             :refer [wrap-restful-response]]
            [carneades.web.modules.project.routes
             :refer [carneades-projects-api-routes]]
            [carneades.web.modules.session.routes
             :refer [carneades-session-api-routes]]
            [carneades.web.modules.lican.routes
             :refer [carneades-lican-api-routes]]))

(defroutes carneades-web-routes
  (context "/carneades" []
           (GET "/" [] (resp/resource-response "index.html" {:root "public/carneades"}))
           (context "/api" []
                    (context "/session" [] carneades-session-api-routes)
                    (context "/projects" [] carneades-projects-api-routes)
                    (context "/lican" [] carneades-lican-api-routes))))

(defroutes tomcat-carneades-web-routes
  (GET "/" [] (resp/resource-response "index.html" {:root "public/carneades"}))
  (context "/api" []
           (context "/session" [] carneades-session-api-routes)
           (context "/projects" [] carneades-projects-api-routes)
           (context "/lican" [] carneades-lican-api-routes)))

(defroutes jar-carneades-web-routes
  (context "/carneades" []
      (GET "/" [] (resp/resource-response "index.html" {:root "public/carneades"})))
  (context "/carneades/api" []
           (context "/session" [] carneades-session-api-routes)
           (context "/projects" [] carneades-projects-api-routes)
           (context "/lican" [] carneades-lican-api-routes)))
