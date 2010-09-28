;;; Copyright © 2010 Fraunhofer Gesellschaft 
;;; Licensed under the EUPL V.1.1

(ns carneades.editor.controller.listeners
  (:use clojure.contrib.def
        clojure.contrib.pprint
        [clojure.contrib.swing-utils :only (do-swing do-swing-and-wait)]
        (carneades.editor.controller search documents)
        (carneades.engine.lkif import export)
        (carneades.engine argument argument-edit)
        [carneades.engine.statement :only (statement-formatted)]
        carneades.editor.model.docmanager
        ;; only the view.viewprotocol namespace is allowed to be imported
        carneades.editor.view.viewprotocol
        ;; no import of carneades.editor.view.editorapplication,
        ;; java.awt.*, javax.* are not allowed here
        )
  (:require [clojure.string :as str]
            [carneades.editor.model.lkif-utils :as lkif])
  (:import java.io.File))

;;; in this namespace we define the Swing independant listeners

(defvar- *file-error* "File Error")
(defvar- *edit-error* "Edit Error")
(defvar- *statement-already-exists* "Statement already exists.")
(defvar- *file-already-opened* "File %s is already opened.")
(defvar- *file-format-not-supported* "This file format is not supported.")

(defn on-open-file [view]
  (prn "ask-lkif-file-to-open...")
  (when-let [file (ask-lkif-file-to-open view)]
    (let [path (.getPath file)]
      (if (section-exists? *docmanager* [path])
        (display-error view *file-error* (format *file-already-opened* path))
        (try
          (set-busy view true)
          (when-let [content (lkif-import path)]
            (lkif/add-lkif-to-docmanager path content *docmanager*)
            (display-lkif-content view file
                                  (sort-by second
                                           (map (fn [id] [id (:title (get-ag path id))])
                                                (get-ags-id path))))
            (display-lkif-property view path))
          (finally
           (set-busy view false)))))))

(defn on-select-graphid [view path graphid]
  (let [ag (get-ag path graphid)
        id (:id ag)
        title (:title ag)
        mainissue (statement-formatted (:main-issue ag))]
    (display-graph-property view path id title mainissue)))

(defn on-edit-graphid [view path graphid]
  (prn "on-edit-graphid")
  (prn graphid)
  (when-let [ag (get-ag path graphid)]
    (open-graph view path ag statement-formatted)))

(defn on-select-lkif-file [view path]
  (prn "on-select-lkif-file")
  (display-lkif-property view path))

(defn on-close-file [view path]
  (prn "on close file")
  (letfn [(close-all
           [ids]
           (doseq [id ids]
             (close-graph view path id))
           (hide-lkif-content view path)
           (remove-section *docmanager* [path]))]
   (let [ids (get-ags-id path)]
     (if (not (empty? (filter #(is-ag-dirty path %) ids)))
       (when (ask-confirmation view "Close" "Close unsaved file?")
         (close-all ids))
       (close-all ids)))))

(defn on-open-graph [view path id]
  (open-graph view path (get-ag path id) statement-formatted))

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
                                                   *graphviz-svg-description* "svg"
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

(defn on-close-graph [view path id]
  (prn "on-close-graph")
  (if (is-ag-dirty path id)
    (when (ask-confirmation view "Close" "Close unsaved graph?")
      (cancel-updates-section *docmanager* [path :ags id])
      (update-dirty-state view path (get-ag path id) false)
      (update-undo-redo-statuses view path id)
      (close-graph view path id))
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

(defn on-select-statement [path id stmt view]
  (prn "on select statement")
  (prn stmt)
  (when-let [ag (get-ag path id)]
    (let [node (get-node ag stmt)
          status (:status node)
          proofstandard (:standard node)
          acceptable (:acceptable node)
          complement-acceptable (:complement-acceptable node)]
      (display-statement-property view path id (:title ag)
                                  stmt statement-formatted status
                                  proofstandard acceptable complement-acceptable))))

(defn on-select-argument [path id arg view]
  (prn "on select argument")
  (when-let [ag (get-ag path id)]
   (let [arg (get-argument ag (:id arg))]
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
      (:scheme arg)))))

(defn on-select-premise [path id arg pm view]
  (prn "on select premise")
  (prn "arg")
  (prn arg)
  (prn "premise")
  (prn pm)
  (let [type (:type pm)]
    (display-premise-property view path id (:title (get-ag path id))
                              arg
                              (:polarity pm) type (:atom pm))))

(defn on-open-statement [view path id stmt]
  (prn "on-open-statement")
  (when-let [ag (get-ag path id)]
    (display-statement view path ag stmt statement-formatted)))

(defn on-open-argument [view path id arg]
  (when-let [ag (get-ag path id)]
    (display-argument view path ag arg statement-formatted)))

(defn- do-update-section [view keys ag]
  ;; the first key is the path
  (let [path (first keys)]
    (update-section *docmanager* keys ag)
    (update-undo-redo-statuses view path (:id ag))
    (update-dirty-state view path ag true)))

(defn on-edit-statement [view path id stmt-info]
  (prn "on-edit-statement")
  (prn stmt-info)
  (let [{:keys [content previous-content]} stmt-info
        oldag (get-ag path id)]
    (if (statement-node oldag content)
      (display-error view *edit-error* *statement-already-exists*)
      (when-let [ag (update-statement-content oldag previous-content content)]
        (let [stmt (:content stmt-info)
              node (get-node ag stmt)
              status (:status node)
              proofstandard (:standard node)
              acceptable (:acceptable node)
              complement-acceptable (:complement-acceptable node)]
          (do-update-section view [path :ags (:id ag)] ag)
          (display-statement-property view path id (:title ag)
                                      stmt statement-formatted status
                                      proofstandard acceptable complement-acceptable)
          (statement-content-changed view path ag previous-content content)
          (display-statement view path ag stmt statement-formatted))))))

(defn on-edit-statement-status [view path id stmt-info]
  (prn "on-edit-statement-status")
  (let [{:keys [status content previous-status]} stmt-info
        oldag (get-ag path id)]
    (when (and (not= status previous-status)
               (statement-node oldag content))
      (let [ag (update-statement oldag content status)
            node (get-node ag content)
            status (:status node)
            proofstandard (:standard node)
            acceptable (:acceptable node)
            complement-acceptable (:complement-acceptable node)]
        (do-update-section view [path :ags (:id oldag)] ag)
        (display-statement-property view path id (:title ag)
                                    content statement-formatted status
                                    proofstandard acceptable complement-acceptable)
        (statement-status-changed view path ag content)
        (display-statement view path ag content statement-formatted)))))

(defn on-edit-statement-proofstandard [view path id stmt-info]
  (prn "on-edit-statement-proofstandard")
  (prn "stmt-info")
  (prn stmt-info)
  (let [{:keys [proofstandard content previous-proofstandard]} stmt-info]
    (when (not= proofstandard previous-proofstandard)
      (when-let [ag (update-statement-proofstandard (get-ag path id)
                                                    content proofstandard)]
        (let [node (get-node ag content)
              status (:status node)
              proofstandard (:standard node)
              acceptable (:acceptable node)
              complement-acceptable (:complement-acceptable node)]
          (do-update-section view [path :ags (:id ag)] ag)
          (display-statement-property view path id (:title ag)
                                      content statement-formatted status
                                      proofstandard acceptable complement-acceptable)
          (statement-proofstandard-changed view path ag content)
          (display-statement view path ag content statement-formatted))))))

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

(defn- save-lkif [view path]
  (try
    (set-busy view true)
    (let [lkifdata (lkif/extract-lkif-from-docmanager path *docmanager*)]
      (lkif-export lkifdata path))
    (finally
     (set-busy view false))))

(defn on-save [view path id]
  (prn "on-save")
  (delete-section-history *docmanager* [path :ags id])
  (update-dirty-state view path (get-ag path id) false)
  (update-undo-redo-statuses view path id)
  (save-lkif view path))

(defn on-saveas [view path id]
  (prn "on save as!"))

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
            (save-lkif view path)
            (display-graph-property view path id title (:main-issue ag))
            (title-changed view path ag title)))))))

(defn on-premise-edit-polarity [view path id pm-info]
  (when-let [ag (get-ag path id)]
    (let [{:keys [atom previous-polarity polarity]} pm-info]
      (when (not= previous-polarity polarity)
        (let [oldarg (:arg pm-info)
              ag (update-premise-polarity ag oldarg atom polarity)
              arg (get-argument ag (:id oldarg))
              title (:title ag)]
          (do-update-section view [path :ags (:id ag)] ag)
          (premise-polarity-changed view path ag oldarg arg (get-premise arg atom))
          (display-premise-property view path id title
                              arg
                              polarity (:previous-type pm-info) atom))))))

(defn on-premise-edit-type [view path id pm-info]
  (when-let [ag (get-ag path id)]
    (let [{:keys [previous-type type arg atom pm]} pm-info]
      (when (not= previous-type type)
        (let [ag (update-premise-type ag arg atom type)
              newarg (get-argument ag (:id arg))]
          (do-update-section view [path :ags (:id ag)] ag)
          (premise-type-changed view path ag arg newarg (get-premise newarg atom))
          (display-premise-property view path id (:title ag) arg (:polarity pm) type atom))))))

(defn on-argument-edit-title [view path id arg-info]
  (prn "on argument edit")
  (prn "info =")
  (prn arg-info)
  (when-let [ag (get-ag path id)]
    (let [{:keys [argid previous-title title]} arg-info]
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
           (:scheme arg)))))))

(defn on-argument-edit-weight [view path id arg-info]
  (prn "on argument edit weight")
  (prn arg-info)
  (when-let [ag (get-ag path id)]
    (let [{:keys [previous-weight weight argid]} arg-info]
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
           (:scheme arg)))
        ))))

(defn on-argument-edit-direction [view path id arg-info]
  (prn "on-argument-edit-direction")
  (prn arg-info)
  (when-let [ag (get-ag path id)]
    (let [{:keys [previous-direction direction argid]} arg-info]
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
          )))))

(defn on-add-existing-premise [view path id arg stmt]
  (prn "on-add-existing-premise")
  (prn "arg =")
  (prn arg)
  (prn "stmt =")
  (prn stmt)
  (when-let [ag (get-ag path id)]
    (when (nil? (get-premise arg stmt))
      ;; premise does not already exists!
      (let [arg (get-argument ag (:id arg))
            ag (add-premise ag arg stmt)
            newarg (get-argument ag (:id arg))]
        (do-update-section view [path :ags (:id ag)] ag)
        (premise-added view path ag newarg stmt)))))

(defn on-refresh [view path id]
  (when-let [ag (get-ag path id)]
   (redisplay-graph view path ag statement-formatted)))

