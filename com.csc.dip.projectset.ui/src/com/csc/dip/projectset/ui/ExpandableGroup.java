/*******************************************************************************
 * Copyright (C) 2018 DXC Technology
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.csc.dip.projectset.ui;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Dirk Baumann
 * 
 * This class creates an expandable composite with a header line
 * and a button for expanding the area
 *
 */
public class ExpandableGroup extends Composite {

	private static final int ICON_WIDTH = 16;
	private static final int ICON_HEIGHT = 16;
	private static final int LABEL_HEIGHT = 20;

	// cached images
	private static Image expandedLightImage;
	private static Image expandedDarkImage;
	private static Image collapsedLightImage;
	private static Image collapsedDarkImage;

	boolean expanded = false;

	protected Composite buttonAndLabelComposite;
	protected Label expandButton;
	protected Label groupLabel;
	protected Composite layoutComposite;
	protected Composite resizeComposite;

	public ExpandableGroup(
		Composite parent,
		String labelText,
		Color bgColor,
		boolean expanded,
		Composite layoutComposite,
		Composite resizeComposite) {

		super(parent, SWT.NONE);

		initializeImages();
		this.expanded = expanded;
		this.layoutComposite = layoutComposite;
		this.resizeComposite = resizeComposite;

		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout mainLayout = new GridLayout(1, false);
		mainLayout.marginWidth = 0;
		mainLayout.marginHeight = 0;
		setLayout(mainLayout);
		setBackground(bgColor);

		// create composite for button and label
		buttonAndLabelComposite = new Composite(this, SWT.NONE);
		GridLayout buttonAndLabelCompositeLayout = new GridLayout(2, false);
		buttonAndLabelCompositeLayout.marginWidth = 0;
		buttonAndLabelCompositeLayout.marginHeight = 0;
		buttonAndLabelComposite.setLayout(buttonAndLabelCompositeLayout);
		buttonAndLabelComposite.setBackground(bgColor);
		GridData buttonAndLabelGD = new GridData(GridData.FILL_HORIZONTAL);
		buttonAndLabelComposite.setLayoutData(buttonAndLabelGD);

		// create expand button
		expandButton = new Label(buttonAndLabelComposite, SWT.NONE);
		expandButton.setBackground(bgColor);
		expandButton.setForeground(bgColor);
		GridData buttonGD =
			new GridData(GridData.BEGINNING | GridData.VERTICAL_ALIGN_CENTER);
		buttonGD.widthHint = ICON_WIDTH;
		buttonGD.heightHint = ICON_HEIGHT;
		expandButton.setLayoutData(buttonGD);
		expandButton.setImage(expandedLightImage);
		expandButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				expandButtonPressed(true);
			}
		});
		expandButton.addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseEnter(MouseEvent e) {
				mouseEnterExpandButton(e);
			}
			public void mouseExit(MouseEvent e) {
				mouseExitExpandButton(e);
			}
		});

		// create group label
		groupLabel = new Label(buttonAndLabelComposite, SWT.NONE);
		groupLabel.setText(labelText);
		groupLabel.setBackground(bgColor);
		groupLabel.setFont(
			JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));
		groupLabel.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				expandButtonPressed(true);
			}
		});
		
		// set height and image
		this.expanded = !expanded;
		expandButtonPressed(false);
	}

	protected void expandButtonPressed(boolean updateView) {

		expanded = !expanded;

		GridData gridData = (GridData) getLayoutData();
		if (expanded) {
			expandButton.setImage(expandedLightImage);
			gridData.heightHint = SWT.DEFAULT;
		} else {
			expandButton.setImage(collapsedLightImage);
			gridData.heightHint = LABEL_HEIGHT;
		}

		if (updateView) {
			layoutComposite.layout();

			// WORKAROUND: redraw, update,... seems not to work
			// or i use it in the wrong way !
			int x = resizeComposite.getSize().x;
			int y = resizeComposite.getSize().y;
			resizeComposite.setSize(new Point(x - 1, y));
			resizeComposite.setSize(new Point(x, y));
		}
	}

	protected void mouseEnterExpandButton(MouseEvent e) {
		if (expanded) {
			expandButton.setImage(expandedDarkImage);
		} else {
			expandButton.setImage(collapsedDarkImage);
		}
	}

	protected void mouseExitExpandButton(MouseEvent e) {
		if (expanded) {
			expandButton.setImage(expandedLightImage);
		} else {
			expandButton.setImage(collapsedLightImage);
		}
	}

	protected void initializeImages() {
		// initialize images
		expandedLightImage = ProjectSetUIPlugin.getDefault().getImage("icons/expandedlight.gif"); //$NON-NLS-1$
		expandedDarkImage = ProjectSetUIPlugin.getDefault().getImage("icons/expandeddark.gif"); //$NON-NLS-1$
		collapsedLightImage = ProjectSetUIPlugin.getDefault().getImage("icons/collapsedlight.gif"); //$NON-NLS-1$
		collapsedDarkImage = ProjectSetUIPlugin.getDefault().getImage("icons/collapseddark.gif"); //$NON-NLS-1$
	}

	public void dispose() {
		expandedLightImage.dispose();
		expandedDarkImage.dispose();
		collapsedLightImage.dispose();
		collapsedDarkImage.dispose();

		super.dispose();
	}

	public Composite getButtonAndLabelComposite() {
		return buttonAndLabelComposite;
	}

	public Label getExpandButton() {
		return expandButton;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public Label getGroupLabel() {
		return groupLabel;
	}

}
