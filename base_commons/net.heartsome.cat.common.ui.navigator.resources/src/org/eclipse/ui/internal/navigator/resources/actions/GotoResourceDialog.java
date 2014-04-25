/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.internal.navigator.INavigatorHelpContextIds;
import org.eclipse.ui.internal.navigator.resources.resource.WorkbenchNavigatorMessages;

/**
 * Shows a list of resources to the user with a text entry field for a string
 * pattern used to filter the list of resources.
 * 
 */
/* package */class GotoResourceDialog extends FilteredResourcesSelectionDialog {

	/**
	 * Creates a new instance of the class.
	 */
	protected GotoResourceDialog(Shell parentShell, IContainer container,
			int typesMask) {
		super(parentShell, false, container, typesMask);
		setTitle(WorkbenchNavigatorMessages.actions_GotoResourceDialog_GoToTitle);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parentShell,
				INavigatorHelpContextIds.GOTO_RESOURCE_DIALOG);
	}
}
