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
import processing.app.tools.Tool;
import processing.app.ui.Editor;

import javax.swing.*;
import java.awt.*;

/**
 * The main class for the tool.
 */
public class SketchOutline implements Tool {

	private Base base;

	public String getMenuTitle() {
		return "Sketch Outline";
	}

	@Override
	public void init(Base base) {
		this.base = base;
	}

	public void run() {
		final Editor editor = base.getActiveEditor();

		System.out.println("Sketch Outline 0.2.0 (beta)");
		System.out.println("By - Manindra Moharana | http://www.mkmoharana.com/ and Alexander Kravchenko");

		String mode = editor.getMode().getTitle();
		if (mode.equals("Android") || mode.equals("JavaScript")) {
			System.out
					.println("This tool is still in beta and hasn't been tested throughly in "
							+ mode
							+ " mode. Please report any bugs in the issues section of the project page.");
		}
		try {

			EventQueue.invokeLater(() -> {
				try {
					if (!hasEditorOutline(editor)) {
						Container panel = editor.getTextArea().getParent();

						SketchOutlinePanel outline = new SketchOutlinePanel(editor);
						JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editor.getTextArea(), outline);
						sp.setContinuousLayout(true);
						sp.setResizeWeight(1D);
						panel.add(sp);
						editor.rebuildHeader();

						outline.btnClose.addActionListener(e -> closeOutline(outline));
					}

				} catch (Exception e) {

					if (TreeMaker.debugMode) {
						System.err.println("Exception at Tool.run()");
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e2) {
			System.err.println("Exception at Tool.run() - invokeLater");
			if (TreeMaker.debugMode)
				e2.printStackTrace();
		}

	}

	private void closeOutline(SketchOutlinePanel outline){
		Editor editor = outline.getEditor();
		Container splitPane = editor.getTextArea().getParent();
		Container panel = splitPane.getParent();

		panel.remove(splitPane);
		panel.add(editor.getTextArea());
		editor.rebuildHeader();
	}

	private boolean hasEditorOutline(Editor editor) {
		for (Component child : editor.getTextArea().getParent().getComponents()) {
			if(child instanceof SketchOutlinePanel) {
				return true;
			}
		}
		return false;
	}

}
