;;; Copyright © 2010 Fraunhofer Gesellschaft 
;;; Licensed under the EUPL V.1.1

(ns carneades.editor.view.editorapplication
  (:use clojure.contrib.def
        clojure.contrib.swing-utils
        [clojure.string :only (trim)]
        ;;
        ;; no imports of carneades.engine.* are allowed here
        ;;
        carneades.ui.diagram.graphvizviewer
        carneades.mapcomponent.map
        carneades.editor.view.viewprotocol
        carneades.editor.view.swinguiprotocol
        carneades.editor.view.menu.mainmenu
        carneades.editor.utils.swing
        carneades.editor.view.tabs
        carneades.editor.view.tree
        carneades.editor.view.search
        carneades.editor.view.properties.lkif
        carneades.editor.view.properties.statement
        carneades.editor.view.properties.argument
        carneades.editor.view.properties.premise
        carneades.editor.view.properties.graph
        carneades.editor.view.properties.properties
        carneades.editor.view.aboutbox
        carneades.editor.view.printpreview.preview)
  (:import java.io.File
           (carneades.editor.uicomponents EditorApplicationView)
           (java.awt EventQueue Cursor Color)
           (javax.swing UIManager JFrame JFileChooser JOptionPane)
           (carneades.mapcomponent.map StatementCell ArgumentCell PremiseCell)))

(defvar- *frame* (EditorApplicationView/instance))
(defvar- *dialog-current-directory* (atom nil))
(defvar- *application-name* "Carneades Editor")

(defvar *openFileButton* (.openFileButton *frame*))
(defvar *openFileMenuItem* (.openFileMenuItem *frame*))
(defvar *closeFileMenuItem* (.closeFileMenuItem *frame*))
(defvar *undoButton* (.undoButton *frame*))
(defvar *redoButton* (.redoButton *frame*))

(defvar *exportFileMenuItem* (.exportFileMenuItem *frame*))
(defvar *printPreviewFileMenuItem* (.printPreviewFileMenuItem *frame*))
(defvar *printFileMenuItem* (.printFileMenuItem *frame*))
(defvar *aboutHelpMenuItem* (.aboutHelpMenuItem *frame*))

(defvar *closeLkifFileMenuItem* (.closeLkifFileMenuItem *frame*))
(defvar *exportLkifFileMenuItem* (.exportLkifFileMenuItem *frame*))

(defvar *openGraphMenuItem* (.openGraphMenuItem *frame*))
(defvar *closeGraphMenuItem* (.closeGraphMenuItem *frame*))
(defvar *exportGraphMenuItem* (.exportGraphMenuItem *frame*))

(defvar- *statement-selection-listeners* (atom ()))
(defvar- *argument-selection-listeners* (atom ()))
(defvar- *premise-selection-listeners* (atom ()))

(defn- node-selection-listener [path id obj]
  (cond (instance? StatementCell obj)
        (doseq [{:keys [listener args]} (deref *statement-selection-listeners*)]
          (apply listener path id (:stmt obj) args))

        (instance? ArgumentCell obj)
        (doseq [{:keys [listener args]} (deref *argument-selection-listeners*)]
          (apply listener path id (:arg obj) args))

        (instance? PremiseCell obj)
        (doseq [{:keys [listener args]} (deref *premise-selection-listeners*)]
          (apply listener path id (:pm obj) args))))

(defn- on-zoom-in [event]
  (zoom-in (.getSelectedComponent *mapPanel*)))

(defn- on-zoom-out [event]
  (zoom-out (.getSelectedComponent *mapPanel*)))

(defn- on-zoom-reset [event]
  (zoom-reset (.getSelectedComponent *mapPanel*)))

(defn- select-all-listener [event this]
  (let [[path id] (current-graph this)]
    (when-let [component (get-component path id)]
      (select-all component))))

(deftype SwingView [] View SwingUI
  (init
   [this]
   (System/setProperty "apple.laf.useScreenMenuBar" "true")
   (System/setProperty "com.apple.mrj.application.apple.menu.about.name"
                       *application-name*)

   ;; make a new instance (without listeners!)
   (set-look-and-feel "Nimbus")
   (EditorApplicationView/reset)
   (add-action-listener *zoomInButton* on-zoom-in)
   (add-action-listener *zoomOutButton* on-zoom-out)
   (add-action-listener *zoomResetButton* on-zoom-reset)
   (add-action-listener *selectAllEditMenuItem* select-all-listener this)
   
   (lkif-properties-init)
   (graph-properties-init)
   (init-statement-properties)
   (init-premise-properties)
   (init-menu)
   (init-tree)
   (init-tabs)
   (init-search)
   (.setDefaultCloseOperation *frame* JFrame/DISPOSE_ON_CLOSE))

  (display-error
   [this title content]
   (JOptionPane/showMessageDialog *frame* content title
                                  JOptionPane/ERROR_MESSAGE))

  (ask-confirmation
   [this title content]
   (= (JOptionPane/showOptionDialog *frame* content title
                                  JOptionPane/OK_CANCEL_OPTION
                                  JOptionPane/QUESTION_MESSAGE
                                  nil nil nil)
      JOptionPane/OK_OPTION))
  
  (show
   [this]
   (disable-diagram-buttons-and-menus)
   (disable-undo-button)
   (disable-redo-button)
   (disable-file-items)
   (disable-save-button)
   (EventQueue/invokeLater
    (proxy [Runnable] []
      (run []
           (.setVisible *frame* true)))))

  (display-lkif-content
   [this file graphinfos]
   (add-lkif-content file graphinfos)
   (enable-file-items))

  (hide-lkif-content
   [this path]
   (remove-lkif-content path)
   (when-not (tree-has-content)
     (disable-file-items))
   (hide-properties))

  (open-graph
   [this path ag stmt-fmt]
   (let [component (get-component path (:id ag))]
     (if component
       (select-component component)
       (try
         (set-busy this true)
         (let [component (create-graph-component ag stmt-fmt)]
           (add-node-selection-listener component #(node-selection-listener
                                                     path (:id ag) %))
           (add-component component path ag)
           (enable-diagram-buttons-and-menus))
         (finally
          (set-busy this false))))))

  (close-graph
   [this path id]
   (let [component (get-component path id)]
     (remove-component component))
   (when (tabs-empty?)
     (disable-save-button)   
     (disable-diagram-buttons-and-menus)))

  (current-graph
   [this]
   (get-graphinfo (.getSelectedComponent *mapPanel*)))

  (display-lkif-property
   [this path]
   (show-properties (get-lkif-properties-panel path)))

  (display-graph-property
   [this path title mainissue]
   (show-properties (get-graph-properties-panel path title mainissue)))

  (display-statement-property
   [this path id maptitle stmt stmt-fmt status proofstandard acceptable complement-acceptable]
   (show-properties (get-statement-properties-panel
                     path id maptitle
                     stmt stmt-fmt status proofstandard
                     acceptable complement-acceptable)))

  (display-premise-property
   [this path maptitle polarity type]
   (show-properties (get-premise-properties-panel path maptitle polarity type)))
    
  (display-argument-property
   [this path maptitle title applicable weight direction scheme]
   (show-properties (get-argument-properties-panel path maptitle title applicable
                                                   weight direction scheme)))
  (statement-content-changed
   [this path ag oldstmt newstmt]
   (when-let [component (get-component path (:id ag))]
     (change-statement-content component ag oldstmt newstmt)))

  (statement-status-changed
   [this path ag stmt]
   (when-let [component (get-component path (:id ag))]
    (change-statement-status component ag stmt)))

  (statement-proofstandard-changed
   [this path ag stmt]
   (when-let [component (get-component path (:id ag))]
    (change-statement-proofstandard component ag stmt)))
  
  (ask-file-to-save
   [this-view descriptions suggested]
   (letfn [(changeextension
            [file ext]
            (let [filename (.getName file)]
             (let [idxlastdot (.lastIndexOf filename ".")]
               (if (not= idxlastdot -1)
                 (File. (str (subs filename 0 idxlastdot) "." ext))
                 (File. (str filename "." ext))))))]
     (let [selectedfile (atom suggested)
           jc (proxy [JFileChooser] []
                (approveSelection
                 []
                 (when-let [selected (proxy-super getSelectedFile)]
                   (if (.exists selected)
                     (when (ask-confirmation this-view "Save"
                                             "Overwrite existing file?")
                       (proxy-super approveSelection))
                     (proxy-super approveSelection)))))]
       (doseq [[description extension] descriptions]
         (.addChoosableFileFilter jc (create-file-filter description (set [extension]))))
       (when-let [dir (deref *dialog-current-directory*)]
         (.setCurrentDirectory jc dir))
       (when suggested
         (.setSelectedFile jc suggested))
       (add-propertychange-listener jc (fn [event]
                                         (condp = (.getPropertyName event)
                                             
                                             JFileChooser/FILE_FILTER_CHANGED_PROPERTY
                                           (when-let [file (deref selectedfile)]
                                             (let [desc (.. jc getFileFilter getDescription)]
                                               (.setSelectedFile jc (changeextension file
                                                                                     (descriptions desc)))))
                                               
                                           JFileChooser/SELECTED_FILE_CHANGED_PROPERTY
                                           (do
                                             (when-let [file (.. event getNewValue)]
                                               (reset! selectedfile file)))

                                           nil)))
       (let [val (.showSaveDialog jc *frame*)]
         (when (= val JFileChooser/APPROVE_OPTION)
           (reset! *dialog-current-directory* (.getCurrentDirectory jc))
           [(.getSelectedFile jc) (.. jc getFileFilter getDescription)])))))

  (export-graph-to-svg
   [this ag stmt-fmt filename]
   (try
     (export-graph (create-graph-component ag stmt-fmt) filename)
     (catch java.io.IOException e
       (display-error this "Save error" (.getMessage e)))))

  (export-graph-to-dot
   [this ag statement-formatted filename]
   (try
     (spit filename (gen-graphvizcontent ag statement-formatted))
     (catch java.io.IOException e
       (display-error this "Save error" (.getMessage e)))))

  (export-graph-to-graphviz-svg
   [this ag statement-formatted filename]
   (prn "export to graphviz svg")
   (try
     (gen-image ag statement-formatted filename)
     (catch java.io.IOException e
       (display-error this "Save error" (.getMessage e)))))
  
  (ask-lkif-file-to-open
   [this]
   (let [jc (JFileChooser.)]
     (.setFileFilter jc (create-file-filter "LKIF files"  #{"xml" "lkif"} ))
     (when-let [dir (deref *dialog-current-directory*)]
       (.setCurrentDirectory jc dir))
     (let [val (.showOpenDialog jc *frame*)]
       (when (= val JFileChooser/APPROVE_OPTION)
         (reset! *dialog-current-directory* (.getCurrentDirectory jc))
         (.getSelectedFile jc)))))
  
  (print-graph
   [this path ag stmt-fmt]
   (let [component (or (get-component path (:id ag))
                       (create-graph-component ag stmt-fmt))]
     (print-document (:component component))))

  (print-preview
   [this path ag stmt-fmt]
   (let [component (or (get-component path (:id ag))
                       (create-graph-component ag stmt-fmt))]
     (printpreview *frame* (:component component))))

  (display-about
   [this]
   (show-about-box *frame*))

  ;; here below, implementation of the SwingUI protocol:
  (add-close-button-listener
   [this f args]
   (register-close-button-listener f args))

  (add-open-file-button-listener
   [this f args]
   (apply add-action-listener *openFileButton* f args))

  (add-mousepressed-tree-listener
   [this f args]
   (apply add-mousepressed-listener *lkifsTree* f args))

  (add-mousepressed-searchresult-listener
   [this f args]
   (apply add-mousepressed-listener *searchResultTable* f args))

  (add-keyenter-searchresult-listener
   [this f args]
   (register-keyenter-searchresult-listener f args))

  (add-open-file-menuitem-listener [this f args]
   (apply add-action-listener *openFileMenuItem* f args))

  (add-close-file-menuitem-listener
   [this f args]
   (apply add-action-listener *closeFileMenuItem* f args))

  (export-file-menuitem-listener
   [this f args]
   (apply add-action-listener *exportFileMenuItem* f args))

  (add-export-lkif-filemenuitem-listener
   [this f args]
   (apply add-action-listener *exportLkifFileMenuItem* f args))

  (add-export-graph-menuitem-listener
   [this f args]
   (apply add-action-listener *exportGraphMenuItem* f args))

  (add-about-helpmenuitem-listener
   [this f args]
   (apply add-action-listener *aboutHelpMenuItem* f args))

  (add-printpreview-filemenuitem-listener
   [this f args]
   (apply add-action-listener *printPreviewFileMenuItem* f args))

  (add-close-lkif-filemenuitem-listener
   [this f args]
   (apply add-action-listener *closeLkifFileMenuItem* f args))

  (add-export-filemenuitem-listener
   [this f args]
   (apply add-action-listener *exportFileMenuItem* f args))
  
  (add-open-graph-menuitem-listener
   [this f args]
   (apply add-action-listener *openGraphMenuItem* f args))

  (add-close-graph-menuitem-listener
   [this f args]
   (apply add-action-listener *closeGraphMenuItem* f args))

  (add-print-filemenuitem-listener
   [this f args]
   (apply add-action-listener *printFileMenuItem* f args))

  (add-searchresult-selection-listener
   [this f args]
   (register-searchresult-selection-listener f args))

  (add-statement-edit-listener
   [this f args]
   (register-statement-edit-listener f args))

  (add-statement-edit-status-listener
   [this f args]
   (apply add-action-listener *statementStatusComboBox* f args))

  (add-statement-edit-proofstandard-listener
   [this f args]
   (apply add-action-listener *statementProofstandardComboBox* f args))

  (add-undo-button-listener
   [this f args]
   (apply add-action-listener *undoButton* f args))
  
  (add-redo-button-listener
   [this f args]
   (apply add-action-listener *redoButton* f args))

  (add-save-button-listener
   [this f args]
   (apply add-action-listener *saveButton* f args))

  (add-copyclipboard-button-listener
   [this f args]
   (apply add-action-listener *copyClipboardEditMenuItem* f args))
  
  (edit-undone
   [this path id]
   (when-let [component (get-component path id)]
     (undo component)))

  (edit-redone
   [this path id]
   (when-let [component (get-component path id)]
     (redo component)))
  
  (get-statement-being-edited-info
   [this]
   (statement-being-edited-info))
  
  (register-search-listener
   [this l args]
   (register-search-button-listener l args))

  (display-statement-search-result
   [this path id stmt stmt-fmt]
   (add-stmt-search-result path id stmt stmt-fmt))

  (display-search-state
   [this inprogress]
   (set-search-state inprogress))

  (display-statement
   [this path ag stmt stmt-fmt]
   (prn "display statement")
   (open-graph this path ag stmt-fmt)
   (let [component (get-component path (:id ag))]
     (select-statement component stmt stmt-fmt)))

  (set-busy
   [this isbusy]
   (if isbusy
     (.setCursor *frame* Cursor/WAIT_CURSOR)
     (.setCursor *frame* (Cursor/getDefaultCursor))))

  (set-can-undo
   [this path id state]
   (set-component-can-undo path id state))

  (set-can-redo
   [this path id state]
   (set-component-can-redo path id state))

  (set-dirty
   [this path ag state]
   (when-let [component (get-component path (:id ag))]
    (set-component-dirty component ag state)
    (let [[currentpath id] (current-graph this)]
      (when (and (= path currentpath)
                 (= id (:id ag)))
        (if state
          (enable-save-button)
          (disable-save-button))))))
  
  (copyselection-clipboard
   [this path id]
   (when-let [component (get-component path id)]
     (copyselection-toclipboard component)))
  
  (get-selected-object-in-tree
   [this]
   (selected-object-in-tree))

  (get-selected-object-in-search-result
   [this]
   (selected-object-in-search-result))
  
  (get-graphinfo-being-closed
   [this event]
   (graphinfo-being-closed event))

  (register-statement-selection-listener
   [this l args]
   (swap! *statement-selection-listeners* conj {:listener l :args args}))
  
  (register-argument-selection-listener
   [this l args]
   (swap! *argument-selection-listeners* conj {:listener l :args args}))
  
  (register-premise-selection-listener
   [this l args]
   (swap! *premise-selection-listeners* conj {:listener l :args args})))
  
  
