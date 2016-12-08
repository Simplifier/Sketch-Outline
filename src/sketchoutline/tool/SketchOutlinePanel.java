/**
 * Sketch Outline provides a tree view of a sketch and its 
 * members functions, variables and inner classes. Clicking on any 
 * node moves the cursor to its definition.
 *
 * Copyright (c) 2012 Manindra Moharana
 *
 * ##copyright##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 *
 *
 * @author	Manindra Moharana	##author##
 * @modified 25-12-2012			##date##
 * @version 0.1.7 (beta)		##version##
 */

package sketchoutline.tool;

import processing.app.Base;
import processing.app.ui.Editor;
import sketchoutline.tool.TreeMaker.TmNode;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * The Main GUI of the Sketch Outline Tool.
 */
@SuppressWarnings("serial")
public class SketchOutlinePanel extends JPanel {

	private JScrollPane scrollPane;
	public JTree tree;
	private ThreadedTreeMaker thTreeMaker = null;
	private Editor editor;
	private TreePath lastpath;
	public JButton btnClose;
	public boolean okToShowFrame = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
				SketchOutlinePanel frame = new SketchOutlinePanel(null);
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Constructor for SketchOutlinePanel
	 */
	public SketchOutlinePanel(Editor edt) {
		this.editor = edt;

		if (thTreeMaker == null) {

			if (editor != null) {

				thTreeMaker = new ThreadedTreeMaker(this, editor);

			} else {
				System.err.println("Editor is null");
				thTreeMaker = new ThreadedTreeMaker(this, null);
			}
		}

		prepareFrame();
	}

	/**
	 * Create the JFrame.
	 */
	public void prepareFrame() {
		if (thTreeMaker.treeMaker.treeCreated) {

			tree = new JTree(thTreeMaker.getTree());
			tree.setCellRenderer(new TehRenderer());
			tree.setExpandsSelectedPaths(true);
			if (editor != null)
				thTreeMaker.treeMaker.sourceString = editor.getSketch()
						.getCurrentCode().getProgram();
			else
				thTreeMaker.treeMaker.sourceString = "";

			okToShowFrame = true;
		} else {
			System.err.println("Outline couldn't be created :( "
					+ "\nThe sketch may have compilation errors.");
			if (TreeMaker.debugMode)
				System.err.println("Parsing error - SketchOutlinePanel.");
			okToShowFrame = false;
		}

		setBounds(new Rectangle(100, 100, 260, 420));
		if (editor != null) {

			setLocation(new Point(editor.getLocation().x + editor.getWidth(),
					editor.getLocation().y));
		}
		setMinimumSize(new Dimension(260, 420));
		setFocusable(true);

		// TODO: Use a proper swing layout like Box, etc. Absolute layout is a
		// messy technique
		setBounds(new Rectangle(0, 0, 244, 382));
		//add(contentPane);
		setLayout(null);

		scrollPane = new JScrollPane();
		scrollPane.setViewportView(tree);
		scrollPane.setBounds(0, 37, 244, 345);
		add(scrollPane);

		String iconPath = "data" + File.separator + "icons" + File.separator;
		/*
		 * data folder of the tool doesn't seem to import the required resource
		 * files(icons). So retrieving it from
		 * sketchbook\tools\SketchOutline\src.
		 *
		 * I know it ain't text book style, but just works.
		 */
		if (editor != null) {
			iconPath = (editor.getBase().getSketchbookFolder()
					.getAbsolutePath())

					+ File.separator
					+ "tools"
					+ File.separator
					+ "SketchOutline"
					+ File.separator
					+ "data"
					+ File.separator
					+ "icons"
					+ File.separator;
		}

		ImageIcon ic;

		btnClose = new JButton("");
		btnClose.setToolTipText("Close Outline");
		ic = new ImageIcon((iconPath + "close_icon.png"));
		btnClose.setIcon(ic);

		btnClose.setBounds(6, 4, 33, 29);
		add(btnClose);

		btnSortTree = new JToggleButton("");
		btnSortTree.setToolTipText("Display Alphabetically");
		ic = new ImageIcon((iconPath + "sort_icon.png"));
		btnSortTree.setIcon(ic);
		btnSortTree.setBounds(134, 4, 33, 29);
		add(btnSortTree);

		btnAbout = new JButton("");
		btnAbout.setToolTipText("About Sketch Outline");
		btnAbout.setBounds(208, 4, 33, 29);
		btnAbout.setIcon(new ImageIcon((iconPath + "info_icon.png")));
		add(btnAbout);

		btnShowFields = new JToggleButton("");
		btnShowFields.setToolTipText("Hide/Show Fields");
		btnShowFields.setBounds(171, 4, 33, 29);
		add(btnShowFields);
		btnShowFields.setIcon(new ImageIcon(iconPath + "field_icon.png"));

		// iListen
		addListeners();

		// Start the auto update thread.
		thTreeMaker.start();
	}

	private void scrollToLocation() {
		try {

			if (hasFocus())
				return;
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree
					.getLastSelectedPathComponent();
			if (n == null)
				return;
			if (lastpath != null)
				thTreeMaker.lastRow = (tree.getRowForPath(lastpath));
			TmNode tn = (TmNode) n.getUserObject();

			int offset = thTreeMaker.treeMaker.xyToOffset(tn.node.getBeginLine()
							- thTreeMaker.treeMaker.mainClassLineOffset,
					tn.node.getBeginColumn());

			if (editor.getCaretOffset() != offset) {
				editor.toFront();
				editor.setSelection(offset, offset);
				editor.getTextArea().repaint();
			}

		} catch (Exception ex) {
			System.out.println("Error positioning cursor." + ex.toString());
			if (TreeMaker.debugMode)
				ex.printStackTrace();
		}
	}

	private JToggleButton btnSortTree;
	private JButton btnAbout;
	private JToggleButton btnShowFields;
	public boolean listenForTreeExpand = true;

	/**
	 * Adds {@link ComponentListener} and {@link WindowListener} listeners to
	 * SketchOutline frame and the Editor frame. Used for implementing the
	 * docking feature. Adds other action listeners for all components like
	 * clicking on tree node scrolls to its definition, button presses, etc.
	 */
	private void addListeners() {

		btnSortTree.addActionListener(e -> {
			thTreeMaker.treeMaker.enableSortingCodeTree = btnSortTree
					.isSelected();
			if (!thTreeMaker.updateTree())
				System.out
						.println("Tree couldn't be rebuilt, the sketch may have compilation errors.");

		});

		btnShowFields.addActionListener(e -> {
			thTreeMaker.treeMaker.hideFields = !thTreeMaker.treeMaker.hideFields;
			if (!thTreeMaker.updateTree())
				System.out
						.println("Tree couldn't be rebuilt, the sketch may have compilation errors.");
		});

		btnClose.addActionListener(e -> {
			//thTreeMaker.updateTree();

			// Hold down shift key and click refresh to enable debug
			// messages.
			if (e.getModifiers() != KeyEvent.VK_SHIFT) {
				TreeMaker.debugMode = !TreeMaker.debugMode;
				if (TreeMaker.debugMode) {
					System.err.println("Sketch Outline Debug Mode ON");

				} else {
					System.out.println("Sketch Outline Debug Mode OFF");
				}
				System.out
						.println("Hold down shift key and click refresh to toggle debug messages.");
			}
		});

		addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {

			}

			@Override
			public void componentResized(ComponentEvent e) {

				Dimension d = e.getComponent().getSize();
				if (d.width >= 244 && d.height >= 382) {
					scrollPane.setSize(d.width, d.height - scrollPane.getY());
					scrollPane.validate();
				}
			}

			@Override
			public void componentMoved(ComponentEvent e) {

			}

			@Override
			public void componentHidden(ComponentEvent e) {

			}
		});

		btnAbout.addActionListener(e -> {
			// 'bout me.
			System.out
					.println("===========================================================");
			System.out.println();
			System.out
					.println("                Sketch Outline 0.2.0 (beta)                ");
			System.out
					.println("                  By - Manindra Moharana                   ");
			System.out
					.println("              http://www.mkmoharana.com/              ");
			System.out.println();
			System.out
					.println("===========================================================");
			System.out
					.println("<> Project page: https://github.com/Manindra29/Sketch-Outline");
			System.out
					.println("<> Processing Forum Thread: \nhttp://forum.processing.org/topic/sketch-outline-new-processing-tool-announcement");
			System.out
					.println("<> Please report any bugs in the issues section of the project page or forum discussion thread.");
			System.out
					.println("<> Sketch Outline Debug Mode - Hold down shift key and click refresh.");
		});

		tree.addTreeExpansionListener(new TreeExpansionListener() {

			@SuppressWarnings("rawtypes")
			@Override
			public void treeExpanded(final TreeExpansionEvent e) {
				SwingWorker worker = new SwingWorker() {

					@Override
					protected Object doInBackground() throws Exception {
						return null;
					}

					protected void done() {
						if (!listenForTreeExpand)
							return;
						if (tree.getRowForPath(e.getPath()) >= 0)
							thTreeMaker.lastExpandedRows.add(tree
									.getRowForPath(e.getPath()));
					}
				};
				worker.execute();
			}

			@SuppressWarnings("rawtypes")
			@Override
			public void treeCollapsed(final TreeExpansionEvent e) {
				SwingWorker worker = new SwingWorker() {

					@Override
					protected Object doInBackground() throws Exception {
						return null;
					}

					protected void done() {
						if (!listenForTreeExpand)
							return;
						if (tree.getRowForPath(e.getPath()) >= 0)
							thTreeMaker.lastExpandedRows.remove(tree
									.getRowForPath(e.getPath()));
					}
				};
				worker.execute();
			}
		});

		// The next set of listeners are needed only if a PDE is around
		if (editor == null)
			return;
		/**
		 * Scroll to definition happens here
		 */
		tree.addTreeSelectionListener(event -> {
			@SuppressWarnings("rawtypes")
			SwingWorker worker = new SwingWorker() {

				@Override
				protected Object doInBackground() throws Exception {
					return null;
				}

				protected void done() {
					lastpath = event.getPath();
					scrollToLocation();
				}
			};
			worker.execute();
		});
	}

	public void dispose() {
		thTreeMaker.halt();
		setVisible(false);
	}

	public Editor getEditor() {
		return editor;
	}

	private class TehRenderer extends DefaultTreeCellRenderer {

		private final ImageIcon classIcon, fieldIcon, methodIcon;

		public TehRenderer() {
			String iconPath = "data" + File.separator + "icons";
			if (editor != null) {
				iconPath = (Base.getSketchbookFolder().getAbsolutePath())

						+ File.separator + "tools" + File.separator + "SketchOutline"
						+ File.separator + "data" + File.separator + "icons";
			}

			classIcon = new ImageIcon(iconPath + File.separator
					+ "class_obj.png");
			methodIcon = new ImageIcon(iconPath + File.separator
					+ "methpub_obj.png");
			fieldIcon = new ImageIcon(iconPath + File.separator
					+ "field_default_obj.png");
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
													  boolean sel, boolean expanded, boolean leaf, int row,
													  boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			if (value instanceof DefaultMutableTreeNode)
				setIcon(getTreeIcon(value));
			else {
				System.out.println("Weird: "
						+ value.getClass().getCanonicalName());
			}

			return this;
		}

		public Icon getTreeIcon(Object o) {
			if (o instanceof DefaultMutableTreeNode) {

				TmNode tmnode = (TmNode) ((DefaultMutableTreeNode) o)
						.getUserObject();
				String type = TreeMaker.getType(tmnode.node);
				if (type.equals("MethodDeclaration")
						|| type.equals("ConstructorDeclaration"))
					return methodIcon;
				if (type.equals("ClassOrInterfaceDeclaration"))
					return classIcon;
				if (type.equals("FieldDeclaration"))
					return fieldIcon;
			}

			return null;

		}

	}

}
