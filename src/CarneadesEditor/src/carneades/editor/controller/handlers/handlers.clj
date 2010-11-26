;;; Copyright © 2010 Fraunhofer Gesellschaft 
;;; Licensed under the EUPL V.1.1

(ns carneades.editor.controller.handlers.handlers
  (:use clojure.contrib.def
        clojure.contrib.pprint
        [clojure.contrib.swing-utils :only (do-swing do-swing-and-wait)]
        carneades.editor.controller.handlers.messages
        (carneades.editor.controller search documents)
        carneades.engine.lkif
        (carneades.engine argument argument-edit)
        [carneades.engine.statement :only (statement-formatted statement?)]
        carneades.editor.model.docmanager
        carneades.editor.utils.core
        ;; only the view.viewprotocol namespace is allowed to be imported
        carneades.editor.view.viewprotocol
        ;; no import of carneades.editor.view.editorapplication,
        ;; java.awt.*, javax.* are not allowed here
        )
  (:require [clojure.string :as str]
            [carneades.editor.model.lkif-utils :as lkif])
  (:import java.io.File))

;;; in this namespace we define the Swing independant listeners

(defn- create-lkifinfo [path]
  (sort-by second
           (map (fn [id] [id (:title (get-ag path id))])
                (get-ags-id path))))

(defn- close-all [view path]
  "closes all graphs without saving, removes LKIF from tree"
  (doseq [id (get-ags-id path)]
    (close-graph view path id))
  (hide-lkif-content view path)
  (remove-section *docmanager* [path]))

(defn on-select-graphid [view path graphid]
  (let [ag (get-ag path graphid)
        id (:id ag)
        title (:title ag)
        mainissue (statement-formatted (:main-issue ag))]
    ;; (pprint "ag = ")
    ;; (pprint ag)
    ;; (prn)
    (display-graph-property view path id title mainissue)))

(defn on-select-lkif-file [view path]
  (prn "on-select-lkif-file")
  (prn "kbs =")
  (let [importurls (get-kbs-locations path)]
    (display-lkif-property view path importurls)))

(defn on-open-graph [view path id]
  (prn "on-open-graph")
  (when-let [ag (get-ag path id)]
   (open-graph view path ag statement-formatted)
   (when-let [mainissue (:main-issue ag)]
     (display-statement view path ag mainissue statement-formatted))))

(defn on-open-file [view]
  (prn "ask-lkif-file-to-open...")
  (when-let* [file (ask-file-to-open view "LKIF files"  #{"xml" "lkif"})
              path (.getPath file)]
    (if (section-exists? *docmanager* [path])
      (display-error view *file-error* (format *file-already-opened* path))
      (try
        (set-busy view true)
        (when-let [content (lkif-import path)]
          (lkif/add-lkif-to-docmanager path content *docmanager*)
          (init-counters path)
          (let [infos (create-lkifinfo path)]
            (display-lkif-content view file infos)
            (when-let [[id _] (first infos)]
              (on-open-graph view path id))))
        ;; (catch IllegalArgumentException
        ;;     e (display-error view *open-error* (str *invalid-content* ".")))
        (catch java.io.IOException
            e (display-error view *open-error* (str *invalid-content* ": " (.getMessage e))))
        (catch org.xml.sax.SAXException
            e (display-error view *open-error* (str *invalid-content* ".")))
        (finally
         (set-busy view false))))))

(defvar- *dot-description* "DOT Files")
(defvar- *svg-description* "SVG Files")
(defvar- *graphviz-svg-description* "Graphviz SVG Files")

(defn- suggested-filename [title id ext]
  (if (nil? title)
    (File. (str id "." ext))
    (File. (str (str/join "_" (str/split (str/trim title) #"\s")) "." ext))))

(defn- get-extension [filename]
  (let [idx (.lastIndexOf filename ".")]
    (when (not= idx -1)
     (subs filename (inc idx)))))

(defn on-export-graph [view path id]
  (when-let [ag (get-ag path id)]
    (when-let [[file desc] (ask-file-to-save view {*dot-description* "dot"
                                                   ;; *graphviz-svg-description* "svg"
                                                   *svg-description* "svg"}
                                             (suggested-filename (:title ag) id "svg"))]
      (let [filename (.getPath file)
            extension (get-extension filename)]
        (prn "desc = ")
        (prn desc)
        (condp = desc
            *svg-description*
          (export-graph-to-svg view ag statement-formatted filename)

          *dot-description*
          (export-graph-to-dot view ag statement-formatted filename)

          *graphviz-svg-description*
          (do
            (prn "export graphviz")
            (export-graph-to-graphviz-svg view ag statement-formatted filename))

          ;; for the "All files filter":
          (condp = extension
              "dot"
            (export-graph-to-dot view ag statement-formatted filename)

            "svg"
            (do
              (prn "here")
              (export-graph-to-svg view ag statement-formatted filename))

            (display-error view *file-error* *file-format-not-supported*)))))))

(defn- save-lkif [view path]
  "returns false if an error occured, true otherwise"
  (try
    (set-busy view true)
    (let [lkifdata (lkif/extract-lkif-from-docmanager path *docmanager*
                                                      (get-fresh-ag-ids path))]
      (lkif-export lkifdata path)
      true)
    (catch java.io.IOException e
      (display-error view *save-error* (str *error-saving* ": " (.getMessage e)))
      false)
    (finally
     (set-busy view false))))

(defn- do-close-graph [view path id]
  "close graph without saving it"
  (cancel-updates-section *docmanager* [path :ags id])
  (update-dirty-state view path (get-ag path id) false)
  (update-undo-redo-statuses view path id)
  (close-graph view path id))

(defn on-save [view path id]
  "returns false if an error occured, true otherwise"
  (prn "on-save")
  (delete-section-history *docmanager* [path :ags id])
  (remove-fresh-ag path id)
  (update-undo-redo-statuses view path id)
  (if (save-lkif view path)
    (do
     (update-dirty-state view path (get-ag path id) false)
     true)
    false))

(defn on-saveas [view path id]
  (when-let [title (read-sentence view *save-as* "Graph title:")]
    (cond
     
     (empty? title)
     (do
       (display-error view *save-as* "Name is empty.")
       (on-saveas view path id))
     
     (contains? (get-graphs-titles path) title)
     (do
       (display-error view *save-as* (format "Title '%s' is already used." title))
       (on-saveas view path id))

     :else
     (do
       (when-let [ag (get-ag path id)]
        (let [newag (assoc ag :title title)
              newid (gen-graph-id path)
              newag (assoc newag :id newid)]
          (add-section *docmanager* [path :ags newid] newag)
          (init-stmt-counter path (:id newag))
          (let [success (on-save view path newid)]
            (new-graph-added view path newag statement-formatted)
            (open-graph view path newag statement-formatted)
            (when (not success)
              (update-dirty-state view path ag true))
            (display-graph-property view path (:id newag) (:title newag)
                                    (:main-issue newag)))))))))

(defn on-close-graph [view path id]
  (prn "on-close-graph")
  (if (is-ag-dirty path id)
    (case (ask-yesnocancel-question view "Close" "Save graph before closing?")
          :yes (when (on-save view path id)
                 (close-graph view path id))
          
          :no (do-close-graph view path id)
          
          :cancel nil)
    (close-graph view path id)))

(defn on-export-file [view path]
  (when (ask-confirmation view "Export" "Export all the argument graphs?")
    (doseq [id (get-ags-id path)]
      (on-export-graph view path id))))

(defn on-about [view]
  (display-about view))

(defn on-printpreview-graph [view path id]
  (let [ag (get-ag path id)]
    (print-preview view path ag statement-formatted)))

(defn on-print-graph [view path id]
  (let [ag (get-ag path id)]
    (print-graph view path ag statement-formatted)))

(defn on-search-begins [view searchinfo]
  (let [text (first searchinfo)
        options (second searchinfo)
        [path id] (current-graph view)]
    ;; start a separate thread so we can wait for futures to finish
    ;; but not on the swing ui thread (it would cause a deadlock with the
    ;; wait + access from the do-search function)
    (.start (Thread. #(do-search view path id text options)))))

(defn on-search-ends [view]
  (prn "search ends")
  (reset! *end-search* true))

(defn do-display-statement-property
   [view path id maptitle stmt stmt-fmt status proofstandard acceptable complement-acceptable]
   (display-statement-property
    view path id maptitle stmt stmt-fmt status
    proofstandard acceptable complement-acceptable)
   (set-current-statement-property
    view path id maptitle stmt stmt-fmt status
    proofstandard acceptable complement-acceptable))

(defn on-select-statement [path id stmt view]
  (prn "on select statement")
  (when-let* [ag (get-ag path id)
              node (get-node ag stmt)
              status (:status node)
              proofstandard (:standard node)
              acceptable (:acceptable node)
              complement-acceptable (:complement-acceptable node)]
    (do-display-statement-property view path id (:title ag)
                                   (str stmt) statement-formatted status
                                   proofstandard acceptable complement-acceptable)))

(defn on-select-argument [path id arg view]
  (prn "on select argument")
  (when-let* [ag (get-ag path id)
             arg (get-argument ag (:id arg))]
    (prn arg)
    (display-argument-property
     view
     path
     id
     (:title (get-ag path id))
     (:id arg)
     (:title arg)
     (:applicable arg)
     (:weight arg)
     (:direction arg)
     (:scheme arg))))

(defn on-select-premise [path id arg pm view]
  (prn "on select premise")
  (prn "arg")
  (prn arg)
  (prn "premise")
  (prn pm)
  (let [type (:type pm)]
    (display-premise-property view path id (:title (get-ag path id))
                              arg
                              (:polarity pm) type (:role pm) (:atom pm))))

(defn on-open-statement [view path id stmt]
  (when-let [ag (get-ag path id)]
    (display-statement view path ag stmt statement-formatted)))

(defn on-open-argument [view path id arg]
  (when-let [ag (get-ag path id)]
    (display-argument view path ag arg statement-formatted)))

(defn str-to-stmt [s]
  (letfn [(sexp? [s] (= \( (first s)))
          (check-stmt [s] (when (and (statement? s) (not (empty? s))) s))]
    (when (not (nil? s))
      (let [s (str/trim s)]
        (if (sexp? s)
          (try
            (let [sexp (read-string s)]
              (check-stmt sexp))
            (catch Exception e
              nil))
          (check-stmt s))))))

(defn- do-edit-statement [view path id previous-content-as-obj newcontent oldag]
  (prn "do edit statement")
  (when-let* [ag (update-statement-content oldag previous-content-as-obj newcontent)
              node (get-node ag previous-content-as-obj)
              status (:status node)
              proofstandard (:standard node)
              acceptable (:acceptable node)
              complement-acceptable (:complement-acceptable node)]
    (do-update-section view [path :ags (:id ag)] ag)
    (do-display-statement-property view path id (:title ag)
                                   (str newcontent) statement-formatted status
                                   proofstandard acceptable complement-acceptable)
    (statement-content-changed view path ag previous-content-as-obj newcontent)
    (display-statement view path ag newcontent statement-formatted)))

(defn on-edit-statement [view path id stmt-info]
  (prn "on-edit-statement")
  (prn stmt-info)
  (let [{:keys [content previous-content]} stmt-info
        oldag (get-ag path id)
        previous-content-as-obj (str-to-stmt previous-content) 
        newcontent (str-to-stmt content)
        isnil (nil? newcontent)
        exists (statement-node oldag newcontent)]
    (if (or isnil exists)
      (loop [msg (cond isnil *invalid-content* exists *statement-already-exists*)]
        (display-error view *edit-error* msg)
        (let [content (read-statement view content)
              newcontent (str-to-stmt content)]
          (cond (nil? content)
                nil

                (nil? newcontent)
                (recur msg)

                :else
                (if (statement-node oldag newcontent)
                  (recur *statement-already-exists*)
                  (do-edit-statement view path id previous-content-as-obj newcontent oldag)))))
      (do-edit-statement view path id previous-content-as-obj newcontent oldag))))

(defn on-edit-statement-status [view path id stmt-info]
  (prn "on-edit-statement-status")
  (prn "info = ")
  (prn stmt-info)
  (let [{:keys [status previous-content previous-status]} stmt-info
        oldag (get-ag path id)
        content (str-to-stmt previous-content)]
    (when (and (not= status previous-status)
               (statement-node oldag content))
      (let [ag (update-statement oldag content status)
            node (get-node ag content)
            status (:status node)
            proofstandard (:standard node)
            acceptable (:acceptable node)
            complement-acceptable (:complement-acceptable node)]
        (do-update-section view [path :ags (:id oldag)] ag)
        (do-display-statement-property view path id (:title ag)
                                       (str content) statement-formatted status
                                       proofstandard acceptable complement-acceptable)
        (statement-status-changed view path ag content)
        (display-statement view path ag content statement-formatted)))))

(defn on-edit-statement-proofstandard [view path id stmt-info]
  (prn "on-edit-statement-proofstandard")
  (prn "stmt-info")
  (prn stmt-info)
  (let [{:keys [proofstandard content previous-proofstandard]} stmt-info
        content (str-to-stmt content)]
    (when (not= proofstandard previous-proofstandard)
      (when-let* [ag (update-statement-proofstandard (get-ag path id)
                                                     content proofstandard)
                  node (get-node ag content)
                  status (:status node)
                  proofstandard (:standard node)
                  acceptable (:acceptable node)
                  complement-acceptable (:complement-acceptable node)]
        (do-update-section view [path :ags (:id ag)] ag)
        (do-display-statement-property view path id (:title ag)
                                       (str content) statement-formatted status
                                       proofstandard acceptable complement-acceptable)
        (statement-proofstandard-changed view path ag content)
        (display-statement view path ag content statement-formatted)))))

(defn on-undo [view path id]
  (prn "on undo")
  (undo-section *docmanager* [path :ags id])
  (update-undo-redo-statuses view path id)
  (update-dirty-state view path (get-ag path id) true)
  (edit-undone view path id))

(defn on-redo [view path id]
  (prn "on redo")
  (redo-section *docmanager* [path :ags id])
  (update-undo-redo-statuses view path id)
  (update-dirty-state view path (get-ag path id) true)
  (edit-redone view path id))

(defn on-copyclipboard [view path id]
  (copyselection-clipboard view path id))

(defn on-title-edit [view path id ag-info]
  (let [{:keys [previous-title title]} ag-info]
    (when (not= previous-title title)
      (when-let [ag (get-ag path id)]
        (if (is-ag-dirty path id)
          (display-error view *edit-error* "Please save the graph first.")
          (let [ag (assoc ag :title title)]
            (update-section *docmanager* [path :ags id] ag)
            (delete-section-history *docmanager* [path :ags id])
            ;; bug here: if the graph can't be saved, the title won't be changed
            ;; on disk and the user won't be able to force a second save
            ;; since the save button is only for the graph editor
            ;; as a workaround we open the graph, to allow a second save!
            (title-changed view path ag title)
            (when (not (save-lkif view path))
              (open-graph view path ag statement-formatted)
              (update-dirty-state view path ag true))
            (display-graph-property view path id title (:main-issue ag))))))))

(defn on-premise-edit-polarity [view path id pm-info]
  (when-let* [ag (get-ag path id)
              {:keys [atom previous-polarity polarity]} pm-info]
    (when (not= previous-polarity polarity)
      (let [oldarg (:arg pm-info)
            ag (update-premise-polarity ag oldarg atom polarity)
            arg (get-argument ag (:id oldarg))
            title (:title ag)]
        (do-update-section view [path :ags (:id ag)] ag)
        (premise-polarity-changed view path ag oldarg arg (get-premise arg atom))
        (display-premise-property view path id title
                                  arg
                                  polarity
                                  (:previous-type pm-info)
                                  (:previous-role pm-info) atom)))))

(defn on-premise-edit-type [view path id pm-info]
  (prn "on premise edit type")
  (when-let* [ag (get-ag path id)
              {:keys [previous-type type previous-role arg atom]} pm-info]
    (when (not= previous-type type)
      (let [ag (update-premise-type ag arg atom type)
            newarg (get-argument ag (:id arg))
            pm (get-premise newarg atom)]
        (do-update-section view [path :ags (:id ag)] ag)
        (premise-type-changed view path ag arg newarg (get-premise newarg atom))
        (display-premise-property view path id (:title ag) arg
                                  (:polarity pm) type (:previous-role pm) atom)))))

(defn on-premise-edit-role [view path id pm-info]
  (prn "on premise edit role")
  (when-let* [ag (get-ag path id)
              {:keys [previous-role previous-type role arg atom]} pm-info]
    (when (not= previous-role role)
      (let [ag (update-premise-role ag arg atom role)
            newarg (get-argument ag (:id arg))
            pm (get-premise newarg atom)]
        (do-update-section view [path :ags (:id ag)] ag)
        (premise-role-changed view path ag arg newarg (get-premise newarg atom))
        (display-premise-property view path id (:title ag) arg (:polarity pm)
                                  previous-type role atom)))))

(defn on-argument-edit-title [view path id arg-info]
  (prn "on argument edit")
  (when-let* [ag (get-ag path id)
              {:keys [argid previous-title title]} arg-info]
    (when (not= previous-title title)
      (let [arg (get-argument ag argid)
            ag (update-argument-title ag arg title)
            arg (get-argument ag argid)]
        (do-update-section view [path :ags (:id ag)] ag)
        (argument-title-changed view path ag arg title)
        (display-argument-property
         view
         path
         id
         (:title ag)
         argid
         (:title arg)
         (:applicable arg)
         (:weight arg)
         (:direction arg)
         (:scheme arg))
        (display-argument view path ag arg statement-formatted)))))

(defn on-argument-edit-weight [view path id arg-info]
  (prn "on argument edit weight")
  (prn arg-info)
  (when-let* [ag (get-ag path id)
              {:keys [previous-weight weight argid]} arg-info]
    (when (not= previous-weight weight)
      (let [arg (get-argument ag argid)
            newag (update-argument-weight ag arg weight)
            arg (get-argument newag argid)]
        (do-update-section view [path :ags (:id ag)] newag)
        (argument-weight-changed view path newag arg weight)
        (display-argument-property
         view
         path
         id
         (:title newag)
         argid
         (:title arg)
         (:applicable arg)
         (:weight arg)
         (:direction arg)
         (:scheme arg))
        (display-argument view path ag arg statement-formatted)))))

(defn on-argument-edit-direction [view path id arg-info]
  (prn "on-argument-edit-direction")
  (prn arg-info)
  (when-let* [ag (get-ag path id)
              {:keys [previous-direction direction argid]} arg-info]
    (when (not= previous-direction direction)
      (let [arg (get-argument ag argid)
            ag (update-argument-direction ag arg direction)
            arg (get-argument ag argid)]
        (do-update-section view [path :ags (:id ag)] ag)
        (argument-direction-changed view path ag arg direction)
        (display-argument-property
         view
         path
         id
         (:title ag)
         argid
         (:title arg)
         (:applicable arg)
         (:weight arg)
         (:direction arg)
         (:scheme arg))
        (display-argument view path ag arg statement-formatted)))))

(defn on-add-existing-premise [view path id arg stmt]
  (when-let [ag (get-ag path id)]
    (when (nil? (get-premise arg stmt))
      ;; premise does not already exists!
      (let [arg (get-argument ag (:id arg))]
        (if-let [ag (add-premise ag arg stmt)]
          (let [arg (get-argument ag (:id arg))]
            (do-update-section view [path :ags (:id ag)] ag)
            (premise-added view path ag arg stmt))
          (display-error view *edit-error* *cycle-error*))))))

(defn on-refresh [view path id]
  (when-let [ag (get-ag path id)]
    (do-update-section view [path :ags (:id ag)] ag)
    (prn "on-refresh")
    (pprint ag)
    (redisplay-graph view path ag statement-formatted)))

(defn on-delete-premise [view path id arg pm]
  (prn "pm =")
  (prn pm)
  (when-let* [ag (get-ag path id)
              arg (get-argument ag (:id arg))
              ag (delete-premise ag arg pm)
              arg (get-argument ag (:id arg))]
    (do-update-section view [path :ags (:id ag)] ag)
    (premise-deleted view path ag arg pm)))

(defn- prompt-statement-content [view ag suggestion]
  (let [stmt (read-statement view suggestion)
        stmt-as-obj (str-to-stmt stmt)]
    (cond (nil? stmt)
          nil

          (nil? stmt-as-obj)
          (do
            (display-error view *edit-error* *invalid-content*)
            (prompt-statement-content view ag stmt))

          (statement-node ag stmt-as-obj)
          (do
            (display-error view *edit-error* *statement-already-exists*)
            (prompt-statement-content view ag stmt))

          :else
          stmt-as-obj)))

(defn on-new-premise [view path id arg]
  (prn "on new premise")
  (when-let* [ag (get-ag path id)
              arg (get-argument ag (:id arg))
              stmt (prompt-statement-content view ag "")
              ag (update-statement ag stmt)
              arg (get-argument ag (:id arg))]
    (if-let [ag (add-premise ag arg stmt)]
      (do
        (do-update-section view [path :ags (:id ag)] ag)
        (new-premise-added view path ag arg stmt statement-formatted)
        (display-statement view path ag stmt statement-formatted))
      (display-error view *edit-error* *cycle-error*))))

(defn on-delete-statement [view path id stmt]
  (prn "on delete stmt")
  (prn "stmt =")
  (prn stmt)
  (when-let* [ag (get-ag path id)
              ag (delete-statement ag stmt)]
    (do-update-section view [path :ags (:id ag)] ag)
    ;; (prn "after delete stmt =")
    ;; (pprint ag)
    ;; (prn)
    (statement-deleted view path ag stmt)))

(defn on-delete-argument [view path id arg]
  (prn "on-delete-argument")
  (when-let* [ag (get-ag path id)
              arg (get-argument ag (:id arg))
              ag (delete-argument ag arg)]
    (do-update-section view [path :ags (:id ag)] ag)
    (prn "ag after delete argument = ")
    (pprint ag)
    (prn)
    (argument-deleted view path ag arg)))

(defn on-change-mainissue [view path id stmt]
  (prn "on change mainissue")
  (prn "stmt =")
  (prn stmt)
  (when-let [ag (get-ag path id)]
    (when (or (nil? stmt) (statement-node ag stmt))
      (let [ag (change-mainissue ag stmt)]
        (do-update-section view [path :ags (:id ag)] ag)
        (mainissue-changed view path ag stmt)))))

(defn- do-on-new-statement [view path ag stmt]
  (let [ag (update-statement ag stmt)]
    (do-update-section view [path :ags (:id ag)] ag)
    (new-statement-added view path ag stmt statement-formatted)
    (display-statement view path ag stmt statement-formatted)
    stmt))

(defn on-new-statement [view path id]
  "creates a new statements and returns it"
  (when-let [ag (get-ag path id)]
    (let [stmt (gen-statement-content path ag)]
      (when-let [stmt (prompt-statement-content view ag "")]
        (do-on-new-statement view path ag stmt)))))

(defn on-new-argument [view path id stmt]
  "creates a new argument and returns it"
  (prn "on new argument")
  (prn "stmt = ")
  (prn stmt)
  (when-let* [ag (get-ag path id)
              arg (pro (gen-argument-id ag) stmt ())
              ag (assert-argument ag arg)]
    (do-update-section view [path :ags (:id ag)] ag)
    (new-argument-added view path ag arg)
    (display-argument view path ag arg statement-formatted)
    arg))

(defn on-new-graph [view path]
  "creates a new graph and returns its id"
  (prn "on new graph")
  (let [title (gen-graph-title path)
        id (gen-graph-id path)
        ag (argument-graph id title nil)
        ag (assoc ag :title title)]
    (add-section *docmanager* [path :ags (:id ag)] ag)
    (init-stmt-counter path (:id ag))
    (add-fresh-ag path (:id ag))
    (new-graph-added view path ag statement-formatted)
    (open-graph view path ag statement-formatted)
    (update-dirty-state view path ag true)
    (display-graph-property view path (:id ag) (:title ag) (:main-issue ag))
    id))

(defn on-delete-graph [view path id]
  (when (ask-confirmation view "Delete" "Permanently delete the graph?")
    (close-graph view path id)
    (remove-section *docmanager* [path :ags id])
    (save-lkif view path)
    ;; here if an error occurs the graph won't be deleted
    ;; on disk and and forcing a save cannot be done
    (graph-deleted view path id)))

(defn- create-template [view path]
  (let [id (on-new-graph view path)
        ag (get-ag path id)
        stmt (do-on-new-statement view path ag "Here is the conclusion...")
        stmt2 (do-on-new-statement view path ag "... and here a premise! Double-click to edit!")
        arg (on-new-argument view path id stmt)
        ag (get-ag path id)]
    (on-change-mainissue view path id stmt)
    (on-add-existing-premise view path id arg stmt2)
    (on-refresh view path id)
    (on-save view path id)
    (display-statement view path ag stmt statement-formatted)))

(defn on-new-file [view]
  (when-let* [[file desc] (ask-file-to-save view {"LKIF files (.xml)" "xml"}
                                            (File. "lkif1.xml"))
              path (.getPath file)]
    (when (section-exists? *docmanager* [path])
      (close-all view path))
    (lkif/add-lkif-to-docmanager path lkif/*empty-lkif-content* *docmanager*)
    (init-counters path)
    (save-lkif view path)
    (display-lkif-content view file (create-lkifinfo path))
    (create-template view path)))

(defn- exit [view]
  (letfn [(in-swank?
           []
           (try
             (require 'swank.core)
             true
             (catch Exception e
               false)))]
    (hide view)
    (if (not (in-swank?))
      (do
        (prn "not in swank")
        (System/exit 0))
      (prn "in swank"))))

(defn on-exit [view event]
  (prn "on exit")
  (let [unsavedgraphs (get-unsaved-graphs)]
    (if (empty? unsavedgraphs)
      (exit view)
      (case (ask-yesnocancel-question view "Close" "Save all graphs before closing?")
            :yes (loop [unsavedgraphs unsavedgraphs]
                   (if-let [[path id] (first unsavedgraphs)]
                     (when (on-save view path id)
                       ;; if we save successfully we continue
                       (recur (rest unsavedgraphs)))
                     ;; all saved, exit
                     (exit view)))
            :no (exit view)
            :cancel nil))))

(defn on-close-file [view path]
  (prn "on close file")
  (let [unsaved (get-unsaved-graphs path)]
    (if (not (empty? unsaved))
      (case (ask-yesnocancel-question view "Close" "Save all graphs before closing?")
            :yes (loop [unsaved unsaved]
                   (if-let [id (first unsaved)]
                     (when (on-save view path id)
                       ;; save without problem? yes, we continue
                       (recur (rest unsaved)))
                     (close-all view path)))
            :no (close-all view path)
            :cancel nil)
      (close-all view path))))

(defn on-import-theory [view path]
  (when-let* [info (ask-location-to-open view)
              url (:location info)
              lkif (add-import (get-lkif path) url)]
    (lkif/update-imports path *docmanager* lkif)
    (save-lkif view path)
    (let [importurls (get-kbs-locations path)]
      (display-lkif-property view path importurls))
    ))

(defn on-remove-imports [view path imports]
  (when (and (not (empty? imports)) (ask-confirmation view *imports* *remove-imports*))
    (let [lkif (get-lkif path)
          lkif (reduce (fn [lkif importurl]
                         (remove-import lkif importurl)) lkif imports)]
      (lkif/update-imports path *docmanager* lkif)
      (save-lkif view path)
      (let [importurls (get-kbs-locations path)]
        (display-lkif-property view path importurls)))))

