/*
Carneades Argumentation Library and Tools.
Copyright (C) 2008 Matthias Grabmair

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License version 3 (GPL-3)
as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for details.
 
You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package Carneades.Graph;

import javafx.ext.swing.*;
import javafx.scene.paint.*;
import javafx.scene.geometry.*;
import javafx.scene.effect.*;
import javafx.scene.Font;
import javafx.scene.text.*;
import javafx.scene.FontStyle;
import javafx.scene.image.*;
import java.lang.System;
import javax.swing.JFileChooser;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import java.io.File;
import java.net.URL;
import java.util.Scanner;

// Model Classes
import Carneades.Argument.Argument;
import Carneades.Argument.Argument.*;

// Other View Classes
import Carneades.Graph.*;
import Carneades.Graph.Elements.Elements.*;

// Abstract Controller Class for Interaction
import Carneades.Control.GraphControl;

public class GraphFrame extends SwingFrame {

	private attribute version: String = "";
	
	public attribute graph: Graph;
	public attribute control: GraphControl;
	public attribute argumentGraph: ArgumentGraph;

	private attribute showCredits: Boolean = false;

	attribute chooser: JFileChooser = new JFileChooser();

	override attribute title = "Carneades";
	override attribute width = GC.appWidth on replace { if (width < GC.appWidth) width = GC.appWidth; };
	override attribute height = GC.appHeight on replace { if (height < GC.appHeight) height = GC.appHeight; };
	override attribute background = GC.panelBackground;

	public attribute view: GraphView = bind GraphView {
							width: bind viewCanvas.width
							height: bind viewCanvas.height
							graph: bind graph
							visible: true
							control: bind control
					}
	
	public attribute viewCanvas: Canvas = Canvas {
		preferredSize: bind [this.width-GC.editWidth - 5, this.height - GC.toolBarHeight]
		content: bind view
	}

	public attribute edit: GraphEdit = GraphEdit {
		background: GC.panelBackground
		visible: true
		control: bind control
		argumentGraph: bind argumentGraph
		preferredSize: bind [GC.editWidth, (this.height / 2) + 60]
	}

	public attribute list: GraphList = GraphList {
		background: GC.panelBackground
		visible: true
		control: bind control
		argumentGraph: bind argumentGraph
		preferredSize: bind [GC.editWidth, (this.height /3) - 40]
	}


	override attribute closeAction = function(): Void {
		quit();
	}

	// content
	private attribute rightPanel: BorderPanel = bind BorderPanel {
		background: GC.panelBackground
		preferredSize: bind [ GC.editWidth, this.height ]
		top: bind list
		bottom: bind edit
		visible: true
	}

	private attribute creditsFrame: SwingFrame = SwingFrame {
		visible: bind this.showCredits
		title: "About Carneades"
		height: 300
		width: 620
		resizable: false
		content: bind Canvas {
			visible: true
			background: Color.WHITE
			width: creditsFrame.width
			height: creditsFrame.height
			content: [
				Rectangle {
					x: 10
					y: 10
					width: creditsFrame.width - 20
					height: creditsFrame.height - 45
					fill: Color.WHITE
					stroke: Color.BLACK
					strokeWidth: 1
					effect: { 
						if (GC.drawShadows) {
							DropShadow {
								color: bind GC.shadowColor
								offsetX: bind GC.xShadowShift
								offsetY: bind GC.yShadowShift
								radius: bind GC.shadowBlurRadius
							}
						} else null
					}
				},
				ImageView {
					x: 20
					y: 20
					width: bind creditsFrame.width
					height: bind creditsFrame.height
					image: Image {
						url: "{__DIR__}images/carneades.jpg"
						size: creditsFrame.height - 65
					},
				},
				Text {
					x: 200
					y: 40
					font: Font {
						size: 16
					}
					content: "Carneades"
				},
				Text { x: 200, y: 70, content: bind "Build {version}" },
				Text { x: 200, y: 100, content: "License: GPL v3" },
				Text { x: 200, y: 130, content: "Copyright � 2008" },
				Text { x: 200, y: 150, content: "Thomas F. Gordon" },
				Text { x: 200, y: 165, content: "Fraunhofer Institute for Open Communication Systems (FOKUS), Berlin" },
				Text { x: 200, y: 185, content: "Matthias Grabmair" },
				Text { x: 200, y: 200, content: "Intelligent Systems Program, University of Pittsburgh" },
				Text { x: 200, y: 230, content: "http://carneades.berlios.de" },
			]
		}

		closeAction: function() {
			showCredits = false;
		}
	}

	public function alert(message: String): Void { 
		JOptionPane.showMessageDialog(null,
    		message,
    		"Error!",
    		JOptionPane.ERROR_MESSAGE
		);
	}

	private attribute toolPanel: FlowPanel = FlowPanel {
		preferredSize: [ this.width, GC.toolBarHeight ]
		background: GC.toolPanelBackground
		visible: true
		content: [
			Label {
				text: bind {"Zoom Factor: {(view.zoomFactor * 100) as Integer}%"}
			}
		]
	}

	override attribute content = bind BorderPanel {
		top: bind toolPanel
		left: bind viewCanvas
		right: bind rightPanel
		center: null
	}
	
	// Menus
	override attribute menus = [
				Menu {
					text: "File"
					items: [
						MenuItem {
							text: "About Carneades"
								action: function() {
								this.showCredits = true;
							}
						},
						MenuItem {
							enabled: true;
							text: "New"
							action: function() {
								if (control.fileChanged) {
									var choice = JOptionPane.showOptionDialog(
										null, "All changes to the graph will be lost.\nSave it now?" , "Save Changes?", 
										JOptionPane.YES_NO_CANCEL_OPTION, 
										JOptionPane.QUESTION_MESSAGE, null, 
										["Save", "Don't Save", "Cancel"], null
									);
									if (choice == JOptionPane.YES_OPTION) {
										saveAs();	
									} else if (choice == JOptionPane.NO_OPTION) {
										control.newGraph();
									}
								} else {
									control.newGraph();
								}
							} // action
						}
						, MenuItem {
							text: "Open"
							action: function() {
								if (control.fileChanged) {
									var choice = JOptionPane.showOptionDialog(
										null, "All changes to the graph will be lost.\nSave it now?" , "Save Changes?", 
										JOptionPane.YES_NO_CANCEL_OPTION, 
										JOptionPane.QUESTION_MESSAGE, null, 
										["Save", "Don't Save", "Cancel"], null
									);
									if (choice == JOptionPane.YES_OPTION) {
										saveAs();	
									} else if (choice == JOptionPane.NO_OPTION) {
										var returnval = chooser.showOpenDialog(null);
										if (returnval == JFileChooser.APPROVE_OPTION) {
											control.loadGraphFromFile(chooser.getSelectedFile());
										}
									}
								} else {
									var returnval = chooser.showOpenDialog(null);
									if (returnval == JFileChooser.APPROVE_OPTION) {
										control.loadGraphFromFile(chooser.getSelectedFile());
									}
								}
							}
						}
						, MenuItem {
							enabled: bind control.fileChanged
							text: "Save"
							action: function() {
								save();
							}
						}
						, MenuItem {
							text: "Save as"
							action: function() {
								saveAs();	
							}
						}
						, MenuItem {
							text: "Quit Carneades"
							action: function() {
								quit();
							}
						}
						] // content
					} // menu
				, Menu {
					text: "Edit"
					items: [
						MenuItem {
							text: "Undo"
							enabled: bind control.possibleToUndo;
							action: function() {
								control.undo();
							}
						} // menuitem
						, MenuItem {
							text: "Redo"
							enabled: bind control.possibleToRedo;
							action: function() {
								control.redo();
							}
						} // menuitem
						, MenuItem {
							text: "Remove"
							enabled: bind control.possibleToRemove;
							action: function() {
								control.removeSelected();
							}
						}
					] // items
				} // menu
				, Menu {
					text: "Insert"
					items: [
						MenuItem {
							text: "Statement"
							enabled: bind control.possibleToAddConclusion;
							action: function() {
								control.addStatement();
							}
						} // menuitem
						, MenuItem {
							text: "Premise"
							enabled: bind control.possibleToAddPremise;
							action: function() {
								control.addPremiseToSelected();
							}
						} // menuitem
						, MenuItem {
							text: "Argument"
							enabled: bind control.possibleToAddArgument;
							action: function() {
								control.addArgumentToSelected();
							}
						} // menuitem
					]
				} // menu
				, Menu {
					text: "debug"
					visible: bind GC.debug
					items: [
						MenuItem {
							text: "Print selection"
							action: function() {
								control.printSelected();
							}
						},
						MenuItem {
							text: "unselect all"
							action: function() {
								control.unSelectAll();
							}
						},
						MenuItem {
							text: "print sizes"
							action: function() {
								control.printSizes();
							}
						},
						MenuItem {
							text: "print vertices"
							action: function() {
								System.out.println("# of vertices: " + sizeof graph.vertices);
								graph.print();
							}
						}

					]
				}
	]; // override default

	postinit {
		loadVersionNumber();
	}

	private function quit(): Void {
		if (control.fileChanged) {
			var choice = JOptionPane.showOptionDialog(
				null, "All changes to the graph will be lost.\nSave it now?" , "Save Changes?", 
				JOptionPane.YES_NO_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE, null, 
				["Save", "Don't Save", "Cancel"], null
			);
			if (choice == JOptionPane.YES_OPTION) {
				saveAs();	
				} else if (choice == JOptionPane.NO_OPTION) {
					System.exit(0);
				}
			} else {
			System.exit(0);
		}
	}

	private function save(): Void {
		if (control.fileLoaded) {
			control.saveGraphToFile(control.currentFile);
		} else {
			// do the same as save-as
			saveAs();
		} // if loaded
	} // function

	private function saveAs(): Void {
		var returnval = chooser.showSaveDialog(null);
		if (returnval == JFileChooser.APPROVE_OPTION) {
			var file: File = chooser.getSelectedFile();
				if (file.exists()) {
					var overwrite = JOptionPane.showOptionDialog(
						null, "The file already exists.\nDo you want to overwrite it?" , "Overwrite existing file?", 
						JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE, null,
						["Yes", "No"], null
					);
				if (overwrite == JOptionPane.OK_OPTION) { control.saveAsGraphToFile(file); }
			} else { control.saveAsGraphToFile(file); }
		}		
	}

	private function loadVersionNumber(): Void {
		var file: File = new File ("version.txt");
		if (file.canRead()) {
			var s: Scanner = new Scanner(file);
			this.version = s.next();
			s.close();
		} else {
			System.out.println("Version file not found.");
		}
	}

}

class ToolBarButton extends SwingButton {
	override attribute preferredSize = [GC.toolBarHeight, GC.toolBarHeight];
}

