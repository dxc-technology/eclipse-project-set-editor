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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Dirk Baumann
 * 
 * Editor for a project set file
 * 
 * Intended to be subclassed outside this package !
 */

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;

import com.csc.dip.projectset.NewProjectSetEntryException;
import com.csc.dip.projectset.NewProjectSetEntryInvTargetException;
import com.csc.dip.projectset.PreferredLocationsUtil;
import com.csc.dip.projectset.ProjectSet;
import com.csc.dip.projectset.ProjectSetEntry;
import com.csc.dip.projectset.ProjectSetNature;
import com.csc.dip.projectset.ProjectSetPlugin;
import com.csc.dip.projectset.ProjectSetUtil;

public class ProjectSetEditor extends EditorPart {

	protected static final String PSF_EDITOR_ID = "com.csc.dip.projectset.ui.ProjectSetEditor"; //$NON-NLS-1$
	protected static final String PSF_EDITOR_HELP_ID = "com.csc.dip.projectset.help.psfEditor"; //$NON-NLS-1$
	protected static final String PROJECT_NAME_PROPERTY = "projectname"; //$NON-NLS-1$
	protected static final String[] PROJECT_NAME_COLUMN = new String[] {PROJECT_NAME_PROPERTY};
	protected static final String[] COLUMN_PROPERTIES = new String[] {PROJECT_NAME_PROPERTY};
	
	protected static final int UNSORTED = -1;

	protected ProjectSet projectSet;

	protected boolean isDirty = false;
	protected boolean loadRecursive = Platform.getPreferencesService().getBoolean(ProjectSetUIPlugin.PLUGIN_ID, IProjectSetUIConstants.EDITOR_LOAD_RECURSIVE_PREFERENCE, true, null);

	// flag that is set to true when preferred text field content is changed
	// but this change should not set the dirty flag
	protected boolean noUserInput;

	protected Composite mainComposite;
	protected TreeViewer tableTreeViewer;

	protected IStructuredSelection selection;
	protected List<ProjectSetEntry> selectedRootEntries = new ArrayList<ProjectSetEntry>();
	protected List<ProjectSetEntry> selectedSubEntries = new ArrayList<ProjectSetEntry>();

	protected boolean isRemoveSelectedEnabled;
	protected boolean isSetSelectedToLoadedEnabled;
	protected boolean isLoadSelectedEnabled;
	protected boolean isLoadAllEnabled;	
	protected boolean isOpenIncludedPSFEnabled;
	protected boolean isSelectInNavigatorEnabled;
	protected boolean isPreferredLocationEditable;

	protected Button addButton;
	protected Button removeButton;
	protected Button setSelectedToLoadedButton;
	protected Button loadSelectedButton;
	protected Button selectAllButton;
	protected Button selectLoadedButton;
	protected Button selectUnloadedButton;
	protected Button loadAllButton;
	protected Button openIncludedPSFButton;

	protected Button loadRecursiveCheckbox;

	protected Text selectedProjectName;
	protected Text selectedTagConflicts;
	protected Text selectedPathConflicts;	
	protected Text selectedPreferredLocation;
	protected Button browseSelectedPreferredLocationButton;
	protected Button setCurrentSelectedPreferredLocationButton;
	protected Button addPathVariableButton;

	protected Text selectedProjectSetProvider;
	protected Text selectedProjectSetLocation;
	protected Text selectedProjectSetTag;
	protected Text selectedProjectSetLocalDirectory;
	protected Text selectedProjectLoadedProvider;
	protected Text selectedProjectLoadedLocation;
	protected Text selectedProjectLoadedVersion;
	protected Text selectedProjectLoadedLocalDirectory;

	protected Color bgColor;
	protected Color blackColor;
	protected Color warningColor;
	protected Color lightGreyColor;

	// listener for changes of projects
	protected IResourceChangeListener resourceChangeListener;

	// actions for table	
	protected Action addSharedProjectsAction;
	protected Action removeSelectedAction;
	protected Action setSelectedToLoadedAction;
	protected Action loadReplaceSelectedAction;
	protected Action loadReplaceAllAction;
	protected Action selectAllAction;
	protected Action selectLoadedAction;
	protected Action selectUnloadedAction;
	protected Action openIncludedPSFAction;
	protected Action selectInNavigatorAction;

	// action for all widgets
	protected Action saveAction;

	//The sourceDeleted flag makes sure that the receiver is not
	//dirty when shutting down
	boolean sourceDeleted = false;
	//The sourceChanged flag indicates whether or not the save from the ole component
	//can be used or if the input changed
	boolean sourceChanged = false;
	
	ProjectSetViewerComparator projectSetViewerComparator = new ProjectSetViewerComparator(); 
	
	protected class ProjectSetViewerComparator extends ViewerComparator {
		int projectSetEntryProperty = UNSORTED;
		boolean ascending = true;
		public int compare(org.eclipse.jface.viewers.Viewer viewer, Object object1, Object object2) {
			if(projectSetEntryProperty == UNSORTED) {
				return 0;
			} else {
				ProjectSetEntry projectSetEntry1 = (ProjectSetEntry)object1;
				ProjectSetEntry projectSetEntry2 = (ProjectSetEntry)object2;
				String propertyValueAsString1 = ProjectsetUIUtil.getPropertyValueAsString(projectSetEntry1, projectSetEntryProperty);
				String propertyValueAsString2 = ProjectsetUIUtil.getPropertyValueAsString(projectSetEntry2, projectSetEntryProperty);
				int compareResult = propertyValueAsString1.compareToIgnoreCase(propertyValueAsString2);
				// if property is equal sort by project name)
				if (compareResult == 0 && projectSetEntryProperty != IProjectSetUIConstants.PROJECT_SET_ENTRY_PROJECT_NAME) {
					String projectName1 = ProjectsetUIUtil.getPropertyValueAsString(projectSetEntry1, IProjectSetUIConstants.PROJECT_SET_ENTRY_PROJECT_NAME);
					String projectName2 = ProjectsetUIUtil.getPropertyValueAsString(projectSetEntry2, IProjectSetUIConstants.PROJECT_SET_ENTRY_PROJECT_NAME);	
					return projectName1.compareToIgnoreCase(projectName2);
				}
				return ascending?compareResult:-compareResult;
			}
		}
		public int getProjectSetEntryProperty() {
			return projectSetEntryProperty;
		}
		public void setProjectSetEntryProperty(int projectSetEntryProperty) {
			this.projectSetEntryProperty = projectSetEntryProperty;
		}
		public boolean isAscending() {
			return ascending;
		}
		public void setAscending(boolean ascending) {
			this.ascending = ascending;
		};
		public void setUnsorted() {
			projectSetEntryProperty = UNSORTED;
		}
	};

	/**
	 * The resource listener updates the receiver when
	 * a change has occurred.
	 */
	private IResourceChangeListener resourceListener = new IResourceChangeListener() {

		/*
		 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta mainDelta = event.getDelta();
			if (mainDelta == null)
				return;
			IResourceDelta affectedPsfElement =
				mainDelta.findMember(projectSet.getProjectSetFile().getFullPath());
			if (affectedPsfElement != null) {
				try {
					processPsfDelta(affectedPsfElement);
				} catch (CoreException exception) {
					//Failed so close the receiver
					getSite().getPage().closeEditor(ProjectSetEditor.this, true);
				}
			}
			IResourceDelta affectedPplElement =
				mainDelta.findMember(
					PreferredLocationsUtil
						.getPreferredProjectLocationsFile(projectSet)
						.getFullPath());
			if (affectedPplElement != null) {
				try {
					processPplDelta(affectedPplElement);
				} catch (CoreException exception) {
					// should never happen
					exception.printStackTrace();
				}
			}
		}

		/*
		 * Process the delta for the PSF file
		 */
		private void processPsfDelta(final IResourceDelta delta) throws CoreException {

			Runnable changeRunnable = null;

			if (delta.getKind() == IResourceDelta.REMOVED) {
				if ((IResourceDelta.MOVED_TO & delta.getFlags()) != 0) {
					changeRunnable = new Runnable() {
						public void run() {
							IPath path = delta.getMovedToPath();
							IFile newFile =
								delta.getResource().getWorkspace().getRoot().getFile(path);
							if (newFile != null) {
								sourceChanged(newFile);
							}
						}
					};
				} else {
					changeRunnable = new Runnable() {
						public void run() {
							sourceDeleted = true;
							getSite().getPage().closeEditor(ProjectSetEditor.this, true);
						}
					};
				}
			} else if (
				(delta.getKind() == IResourceDelta.CHANGED)
					&& ((IResourceDelta.CONTENT & delta.getFlags()) != 0)) {
				changeRunnable = new Runnable() {
					public void run() {
						inputChanged();
					}
				};
			}
			if (changeRunnable != null)
				update(changeRunnable);
		}

		private void processPplDelta(final IResourceDelta delta) throws CoreException {

			Runnable changeRunnable = new Runnable() {
				public void run() {
					PreferredLocationsUtil.updatePreferredProjectLocations(projectSet);
					updateErrorsAndView(true);
				}
			};
			update(changeRunnable);
		}

	};

	/**
	 * Posts the update code "behind" the running operation.
	 *
	 * @param runnable the update code
	 */
	private void update(Runnable runnable) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		if (windows != null && windows.length > 0) {
			Display display = windows[0].getShell().getDisplay();
			display.asyncExec(runnable);
		} else
			runnable.run();
	}

	/* 
	 * See IEditorPart.isSaveOnCloseNeeded() 
	 */
	public boolean isSaveOnCloseNeeded() {
		return !sourceDeleted && super.isSaveOnCloseNeeded();
	}

	/**
	 * The source has changed to the newFile. Update
	 * editors and set any required flags
	 */
	private void sourceChanged(IFile newFile) {

		FileEditorInput newInput = new FileEditorInput(newFile);
		setInput(newInput);
		sourceChanged = true;
		inputChanged();
	}
	
	private IPath writeTempFile(IStorageEditorInput input) {
		String tempFileName = getCorrectName(input.getName());
		java.nio.file.Path path;
		try {
			path = Files
					.createTempDirectory(ProjectSetUIPlugin.getDefault()
							.getStateLocation().toFile().toPath(), "commit") //$NON-NLS-1$
					.resolve(tempFileName);
			try (InputStream in = input.getStorage().getContents()) {
				Files.copy(in, path);
			}
			path = path.toAbsolutePath();
		} catch (CoreException | IOException e) {
//			
			// We mustn't return null; doing so might cause an NPE in
			// WorkBenchPage.busyOpenEditor()
			return new Path(""); //$NON-NLS-1$
		}
		File file = path.toFile();
		file.setReadOnly();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (file.setWritable(true) && file.delete()) {
				file.getParentFile().delete();
			} else {
				// Couldn't delete: re-set as read-only
				file.setReadOnly();
			}
		}));
		return new Path(path.toString());
	}

	private String getCorrectName(String name) {
		String commit = name.substring(name.lastIndexOf(' ')+1, name.length());
		String fileName = name.substring(0, name.lastIndexOf(' '));
		String fullTempName = commit+"_"+fileName;
		return fullTempName;
	}

	/**
	 * Initializes the editor, loads the project set and update the errors
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		try {
			if (input instanceof IFileEditorInput) {
				setSite(site);
				setInput(input);
				inputChanged();
				projectSet.getProjectSetFile().getWorkspace().addResourceChangeListener(resourceListener);
			} else if (input instanceof IStorageEditorInput) {
				setSite(site);
				IStorageEditorInput isei = (IStorageEditorInput) input;
				IPath ip = writeTempFile(isei);
				if (!ip.isEmpty()) {
					ResourcesPlugin.getWorkspace().getRoot().getFile(ip);
					IFileEditorInput ifei = new FileEditorInput(ResourcesPlugin.getWorkspace().getRoot().getFile(ip));
					setInput(ifei);
					inputChanged();
					projectSet.getProjectSetFile().getWorkspace().addResourceChangeListener(resourceListener);
				}
			}
		} catch (NullPointerException e) {
			throw new PartInitException(MessageFormat.format(Messages.getString("ProjectSetEditor.File{0}_not_found"), new Object[] {input.getAdapter(IFileEditorInput.class).getFile().getFullPath().toOSString()}));
		}
	}
	
	protected void inputChanged() {
		IFile projectSetFile = getProjectSetFile();
		projectSet = new ProjectSet(projectSetFile, true).copy();
		projectSet.updateErrors();

		if ((tableTreeViewer != null) && (!tableTreeViewer.getControl().isDisposed())) {
			tableTreeViewer.setInput(projectSet);
		}

		updateEntriesAndView();
		setPartName(projectSetFile.getName());
		//setContentDescription(projectSetFile.getFullPath().toString());
		setTitleToolTip(projectSetFile.getFullPath().toString());
	}

	/**
	 * Returns the project set file for this editor
	 */
	protected IFile getProjectSetFile() {
		return ((IFileEditorInput) getEditorInput()).getFile();
	}

	/**
	 * Returns the shell for this editor
	 */
	protected Shell getShell() {
		return getSite().getShell();
	}

	/**
	 * Saves the project set to the file
	 */
	public void doSave(IProgressMonitor monitor) {

		final IFile projectSetFile = getProjectSetFile();
		try {
			checkFileForWritingAccess(projectSetFile);

		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor pm) throws CoreException {
				projectSet.write();
			}
		};

		
			operation.run(monitor);
			setDirty(false);
			projectSetFile.refreshLocal(IResource.DEPTH_ZERO, monitor);
		} catch (CoreException e) {
		} catch (InterruptedException e) {
		} catch (OperationCanceledException e) {
		} catch (InvocationTargetException e) {
			MessageDialog.openError(
				getShell(), 
				Messages.getString("ProjectSetEditor.Error_while_saving"), //$NON-NLS-1$
				MessageFormat.format(Messages.getString("ProjectSetEditor.Can__t_save_file_{0},_reason__{1}"), new Object[] {projectSetFile.getFullPath().toString(),e.getTargetException().getMessage()}) //$NON-NLS-1$
			);
		} catch (IOException e) {
			MessageDialog.openError(
					getShell(),
					Messages.getString("ProjectSetEditor.Error_while_saving"), //$NON-NLS-1$
					e.getMessage());
		}
	}
	private void checkFileForWritingAccess(IFile projectSetFile) throws IOException {
		if(projectSetFile.getLocation() == null) {
			String message = MessageFormat.format(Messages.getString("ProjectSetEditor.File_{0}_is_read_only_and_cannot_be_saved"), new Object[] {projectSetFile.getName()});
			throw new IOException(message);	
		}
		
	}

	/**
	 * not supported 
	 */
	public void gotoMarker(IMarker marker) {
		// not supported 
	}

	/**
	 * Sets the focus to the tree viewer
	 */
	public void setFocus() {
		tableTreeViewer.getTree().setFocus();
	}

	/**
	 * Returns whether saving is needed
	 */
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * Sets whether saving is needed
	 */
	public void setDirty(boolean isDirty) {
		if (this.isDirty != isDirty) {
			this.isDirty = isDirty;
			firePropertyChange(PROP_DIRTY);
		}
	}

	/**
	 * SaveAs not allowed
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * SaveAs not allowed
	 */
	public void doSaveAs() {
		// save as currently not allowed
	}

	/**
	 * Creates the editor gui parts
	 */
	public void createPartControl(Composite parent) {

		bgColor = new Color(parent.getDisplay(), 255, 255, 255);
		blackColor = new Color(parent.getDisplay(), 0, 0, 0);
		warningColor = new Color(parent.getDisplay(), 200, 0, 0);
		lightGreyColor = new Color(parent.getDisplay(), 220, 220, 220);
		// create a composite with standard margins and spacing
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, false));
		mainComposite.setBackground(bgColor);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createMenu(mainComposite);
		// set context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(mainComposite, PSF_EDITOR_HELP_ID);

		createHeader(mainComposite);

		Composite composite = new Composite(mainComposite, SWT.NONE);
		GridLayout compLayout = new GridLayout();
		compLayout.numColumns = 2;
		composite.setLayout(compLayout);
		composite.setBackground(bgColor);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createMenu(composite);

		Composite tableAndDetailsComposite = new Composite(composite, SWT.NONE);
		tableAndDetailsComposite.setLayout(new GridLayout());
		tableAndDetailsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableAndDetailsComposite.setBackground(bgColor);
		createMenu(tableAndDetailsComposite);
		createTableTreeArea(tableAndDetailsComposite);
		createEntrySetDetailArea(tableAndDetailsComposite);

		createButtonArea(composite);

		// force update of buttons enable state
		tableTreeViewer.setSelection(StructuredSelection.EMPTY);
		updateButtonsAndDetailsView(true);
		
		makeActions();
		createTableTreeMenu();

		createChangeListener();
	}

	/**
	 * Creates a resource change listener, so the editor
	 * notice workspace changes for updating the tree and
	 * the conflicts
	 */
	protected void createChangeListener() {

		resourceChangeListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {

			// TODO: do refresh only if required for better performance 

			if ((tableTreeViewer != null) && 
				(!tableTreeViewer.getControl().isDisposed())) {
					tableTreeViewer.getControl().getDisplay().asyncExec(new Runnable() {
						public void run() {
							updateEntriesAndView();
						}
					});
				}
			}
		};

		ProjectSetPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * Disposes resources and change listeners
	 */
	public void dispose() {
		// remove listeners
		ProjectSetPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
		ProjectSetPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);

		super.dispose();
	}

	/**
	 * Creates the popup menu for a control
	 */
	protected void createMenu(Control control) {

		MenuManager manager = new MenuManager();
		IMenuListener listener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				editorContextMenuAboutToShow(manager);
			}
		};
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(listener);
		Menu contextMenu = manager.createContextMenu(control);
		control.setMenu(contextMenu);
	}

	/**
	 * Adds an enable/disable the save action to the menu
	 */
	public void editorContextMenuAboutToShow(IMenuManager menuManager) {
		menuManager.add(saveAction);
		if (isDirty()) {
			saveAction.setEnabled(true);
		} else {
			saveAction.setEnabled(false);
		}

	}

	/**
	 * Returns the title for header of the editor
	 */
	protected String getEditorName() {
		return Messages.getString("ProjectSetEditor.Project_Set"); //$NON-NLS-1$
	}

	/**
	 * Creates the for header of the editor
	 */
	protected void createHeader(Composite parent) {

		Label label = new Label(parent, SWT.NONE);
		label.setText(getEditorName());
		//label.setImage(PDEPluginImages.get(PDEPluginImages.IMG_FORM_BANNER));
		label.setBackground(bgColor);
		label.setFont(JFaceResources.getFontRegistry().get(JFaceResources.HEADER_FONT));
		createMenu(label);
	}

	/**
	 * Creates the area showing the details of the selected entry
	 */
	protected void createEntrySetDetailArea(Composite parent) {

		Composite detailsComposite = new Composite(parent, SWT.NONE);
		GridLayout detailsCompositeLayout = new GridLayout();
		detailsCompositeLayout.marginWidth = 0;
		detailsComposite.setLayout(detailsCompositeLayout);
		detailsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		detailsComposite.setBackground(bgColor);
		createMenu(detailsComposite);

		Composite projectComposite = new Composite(detailsComposite, SWT.NONE);
		projectComposite.setLayout(new GridLayout(2, false));
		projectComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectComposite.setBackground(bgColor);
		createMenu(projectComposite);

		selectedProjectName = createLabelAndText(projectComposite, Messages.getString("ProjectSetEditor.Selected_Project") + "  ", JFaceResources.BANNER_FONT); //$NON-NLS-1$ //$NON-NLS-2$

		createPreferredFolderGroup(projectComposite);

		selectedTagConflicts = createLabelAndText(projectComposite, Messages.getString("ProjectSetEditor.Tag_Conflicts"), JFaceResources.DIALOG_FONT); //$NON-NLS-1$

		selectedPathConflicts = createLabelAndText(projectComposite, Messages.getString("ProjectSetEditor.Path_Problems"), JFaceResources.DIALOG_FONT); //$NON-NLS-1$

		Composite setGroup = createGroupForDetails(
			detailsComposite,
			Messages.getString("ProjectSetEditor.Project_Set_Entry"),//$NON-NLS-1$
			Platform.getPreferencesService().getBoolean(ProjectSetUIPlugin.PLUGIN_ID, IProjectSetUIConstants.EDITOR_EXPAND_ENTRY_DETAILS_PREFERENCE, false, null));
		selectedProjectSetProvider = createLabelAndText(setGroup, Messages.getString("ProjectSetEditor.Provider"), JFaceResources.DIALOG_FONT); //$NON-NLS-1$
		selectedProjectSetLocation = createLabelAndText(setGroup, Messages.getString("ProjectSetEditor.Location"), JFaceResources.DIALOG_FONT); //$NON-NLS-1$
		selectedProjectSetTag = createLabelAndText(setGroup, Messages.getString("ProjectSetEditor.Tag"), JFaceResources.DIALOG_FONT); //$NON-NLS-1$
		selectedProjectSetLocalDirectory = createLabelAndText(setGroup, Messages.getString("ProjectSetEditor.Local_Directory"), JFaceResources.DIALOG_FONT); //$NON-NLS-1$

		Composite wsGroup = createGroupForDetails(
			detailsComposite,
			Messages.getString("ProjectSetEditor.Project_in_Workspace"),  //$NON-NLS-1$
			Platform.getPreferencesService().getBoolean(ProjectSetUIPlugin.PLUGIN_ID, IProjectSetUIConstants.EDITOR_EXPAND_LOADED_DETAILS_PREFERENCE, false, null));
		selectedProjectLoadedProvider = createLabelAndText(wsGroup, Messages.getString("ProjectSetEditor.Provider"), JFaceResources.DIALOG_FONT); //$NON-NLS-1$
		selectedProjectLoadedLocation = createLabelAndText(wsGroup, Messages.getString("ProjectSetEditor.Location"), JFaceResources.DIALOG_FONT); //$NON-NLS-1$
		selectedProjectLoadedVersion = createLabelAndText(wsGroup, Messages.getString("ProjectSetEditor.Tag"), JFaceResources.DIALOG_FONT); //$NON-NLS-1$
		selectedProjectLoadedLocalDirectory = createLabelAndText(wsGroup, Messages.getString("ProjectSetEditor.Local_Directory"), JFaceResources.DIALOG_FONT); //$NON-NLS-1$
	}

	/**
	 * Creates an empty group to be used in the details area
	 * Returns the created group
	 */
	protected Composite createGroupForDetails(
		Composite parent,
		String labelText,
		boolean expanded) {
		ExpandableGroup expandableGroup =
			new ExpandableGroup(
				parent,
				labelText,
				bgColor,
				expanded,
				mainComposite,
				mainComposite.getParent().getParent());

		createMenu(expandableGroup.getButtonAndLabelComposite());
		createMenu(expandableGroup.getGroupLabel());
		createMenu(expandableGroup.getExpandButton());

		final Composite innerComposite = new Composite(expandableGroup, SWT.NONE);
		innerComposite.setLayout(new GridLayout(2, false));
		innerComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		innerComposite.setBackground(bgColor);
		createMenu(innerComposite);

		return innerComposite;
	}

	protected Label createLabel(Composite parent, String labelText, String fontID) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelText);
		label.setBackground(bgColor);
		label.setFont(JFaceResources.getFontRegistry().get(fontID));
		createMenu(label);
		return label;
	}
	/**
	 * Creates a label and a text element and return the text element
	 */
	protected Text createLabelAndText(Composite parent, String labelText, String fontID) {
		createLabel(parent, labelText, fontID);

		Text text = new Text(parent, SWT.NONE);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setEditable(false);
		text.setBackground(bgColor);
		text.setFont(JFaceResources.getFontRegistry().get(fontID));
		return text;
	}

	protected void createPreferredFolderGroup(Composite parent) {
		createLabel(parent, Messages.getString("ProjectSetEditor.Preferred_Local_Directory"), JFaceResources.DIALOG_FONT); //$NON-NLS-1$

		Composite textAndButtonComposite = new Composite(parent, SWT.NONE);
		textAndButtonComposite.setLayout(new GridLayout(4, false));
		textAndButtonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textAndButtonComposite.setBackground(bgColor);
		createMenu(textAndButtonComposite);

		selectedPreferredLocation = new Text(textAndButtonComposite, SWT.BORDER);
		selectedPreferredLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectedPreferredLocation.setEditable(true);
		selectedPreferredLocation.setBackground(bgColor);
		selectedPreferredLocation.setFont(
			JFaceResources.getFontRegistry().get(JFaceResources.DIALOG_FONT));

		selectedPreferredLocation.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updatePreferredLocationFromTextField();
				if (!noUserInput) {
					setDirty(true);
					
					updateErrorsAndView(false);
				}
			}
		});
				
		browseSelectedPreferredLocationButton = createButton(textAndButtonComposite, Messages.getString("ProjectSetEditor.Browse")); //$NON-NLS-1$
		browseSelectedPreferredLocationButton.setLayoutData(new GridData(GridData.END));
		browseSelectedPreferredLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
				dialog.setText(Messages.getString("ProjectSetEditor.Select_Directory")); //$NON-NLS-1$
				dialog.setMessage(Messages.getString("ProjectSetEditor.Select_preferred_local_directory_for_project")); //$NON-NLS-1$
				String selectedDirectory = dialog.open();
				if (selectedDirectory != null) {
					selectedPreferredLocation.setText(selectedDirectory);
					updatePreferredLocationFromTextField();
				}
			}
		});

		setCurrentSelectedPreferredLocationButton = createButton(textAndButtonComposite, Messages.getString("ProjectSetEditor.Current")); //$NON-NLS-1$
		setCurrentSelectedPreferredLocationButton.setLayoutData(new GridData(GridData.END));
		setCurrentSelectedPreferredLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String current = selectedProjectLoadedLocalDirectory.getText();
				// set empty text if current is default location
				if (ProjectSetUtil.pathEquals(
						current,
						PreferredLocationsUtil.getDefaultLocation(getSelectedRootEntry()))) {
					selectedPreferredLocation.setText(""); //$NON-NLS-1$
				} else {
					selectedPreferredLocation.setText(current);
				}
				updatePreferredLocationFromTextField();
			}
		});
		
		addPathVariableButton = createButton(textAndButtonComposite, Messages.getString("ProjectSetEditor.Add_Var")); //$NON-NLS-1$
		addPathVariableButton.setLayoutData(new GridData(GridData.END));
		addPathVariableButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PathVariableSelectionDialog selectionDialog = new PathVariableSelectionDialog(getShell(), getSelectedRootEntry());
				int buttonPressed = selectionDialog.open();
				if ((buttonPressed == Window.OK) && 
					(selectionDialog.getSelectedPathVariable() != null)) {
					selectedPreferredLocation.insert(
						PreferredLocationsUtil.PATH_VARIABLE_START_STRING +
						selectionDialog.getSelectedPathVariable()+
						PreferredLocationsUtil.PATH_VARIABLE_END_STRING);
				}
			}
		});
	}

	protected void updatePreferredLocationFromTextField() {
		ProjectSetEntry selRootEntry = getSelectedRootEntry();
		if (selRootEntry != null) {
			selRootEntry.setUnresolvedPreferredLocation(selectedPreferredLocation.getText());
			updateDetailsArea(false);
		}
	}

	/**
	 * return the selected root entry or null
	 * if none or more than one root entry is selected 
	 */
	protected ProjectSetEntry getSelectedRootEntry() {
		if ((selectedRootEntries != null) && (selectedRootEntries.size() == 1)) {
			return selectedRootEntries.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Show the details for the entry
	 * 
	 * TODO: Very fragile!!
	 */
	protected void showDetails(ProjectSetEntry entry, boolean updatePreferredLocationText) {
		selectedProjectName.setText(entry.getTreePositionString());
		selectedTagConflicts.setText(entry.getErrorDescription(ProjectSetEntry.ERROR_TYPE_TAG_CONFLICT));
		selectedPathConflicts.setText(entry.getPreferredPathLocationErrorDescriptions());

		String unresolvedPrefLocation = entry.getUnresolvedPreferredLocation();
		if (updatePreferredLocationText) {
			if (unresolvedPrefLocation == null) {
				selectedPreferredLocation.setText(""); //$NON-NLS-1$
			} else {
				selectedPreferredLocation.setText(unresolvedPrefLocation);
			}
		}

		selectedProjectSetProvider.setText(entry.getProviderName());
		selectedProjectSetLocation.setText(entry.getLocation());
		selectedProjectSetTag.setText(entry.getTag());
		String resolvedPrefLocation = entry.getResolvedPreferredLocation();
		
		if(ProjectSetUtil.isProviderSupportsPreferredLocalDirectory(entry.getProvider())){
			if (resolvedPrefLocation == null) {
				selectedProjectSetLocalDirectory.setText(Messages.getString("ProjectSetEditor.Undefined_path_variable")); //$NON-NLS-1$
				selectedProjectSetLocalDirectory.setForeground(warningColor);
			} else {
				selectedProjectSetLocalDirectory.setText(resolvedPrefLocation);
				selectedProjectSetLocalDirectory.setForeground(blackColor);
			}
		}else{
			selectedProjectSetLocalDirectory.setText(Messages.getString("ProjectSetEditor.Preferred_path_not_supported")); //$NON-NLS-1$
			selectedProjectSetLocalDirectory.setForeground(warningColor);
		}

		IProject loadedProject = getLoadedProject(entry.getProjectName());
		String stateString = null;
		if (loadedProject.isAccessible()) {
			RepositoryProvider provider = RepositoryProvider.getProvider(loadedProject);
			if (provider == null) {
				stateString = Messages.getString("ProjectSetEditor.not_shared"); //$NON-NLS-1$
			}
		} else {
			stateString = Messages.getString("ProjectSetEditor.not_loaded"); //$NON-NLS-1$
		}

		if (stateString != null) {
			selectedProjectLoadedProvider.setForeground(warningColor);
			selectedProjectLoadedProvider.setText(stateString);
			selectedProjectLoadedLocation.setForeground(warningColor);
			selectedProjectLoadedLocation.setText(stateString);
			selectedProjectLoadedVersion.setForeground(warningColor);
			selectedProjectLoadedVersion.setText(stateString);
			selectedProjectLoadedLocalDirectory.setForeground(warningColor);
			selectedProjectLoadedLocalDirectory.setText(stateString);
		} else {
			ProjectSetEntry loadedSetEntry;
			try {
				loadedSetEntry = new ProjectSetEntry(projectSet, loadedProject);

				if (loadedSetEntry.getProviderName().equals(entry.getProviderName())) {
					selectedProjectLoadedProvider.setForeground(blackColor);
				} else {
					selectedProjectLoadedProvider.setForeground(warningColor);
				}
				selectedProjectLoadedProvider.setText(loadedSetEntry.getProviderName());

				if ((entry.getLocation() != null) && (entry.getLocation()).equals(loadedSetEntry.getLocation())) {
					selectedProjectLoadedLocation.setForeground(blackColor);
				} else {
					selectedProjectLoadedLocation.setForeground(warningColor);
				}
				selectedProjectLoadedLocation.setText(loadedSetEntry.getLocation());

				if (loadedSetEntry.getTag().equals(entry.getTag())) {
					selectedProjectLoadedVersion.setForeground(blackColor);
				} else {
					selectedProjectLoadedVersion.setForeground(warningColor);
				}
				selectedProjectLoadedVersion.setText(loadedSetEntry.getTag());

				String loadedLocalDirectory;
				IProject projectInWorkspace = loadedSetEntry.getProjectInWorkspace();
				if (projectInWorkspace != null) {
					loadedLocalDirectory = projectInWorkspace.getLocation().toOSString();
				} else {
					loadedLocalDirectory = ""; //$NON-NLS-1$
				}

				if (ProjectSetUtil.pathEquals(loadedLocalDirectory, resolvedPrefLocation)) {
					selectedProjectLoadedLocalDirectory.setForeground(blackColor);
				} else {
					selectedProjectLoadedLocalDirectory.setForeground(warningColor);
				}
				selectedProjectLoadedLocalDirectory.setText(loadedLocalDirectory);
			} catch (NewProjectSetEntryInvTargetException | NewProjectSetEntryException e) {
				// the problematic cases are handled in ProjectSet.addToWorkspace method
			}
		}
	}

	/**
	 * Clear details area entries
	 */
	protected void clearDetails(boolean updatePreferredLocationText) {
		selectedProjectName.setText(""); //$NON-NLS-1$
		selectedTagConflicts.setText(""); //$NON-NLS-1$
		selectedPathConflicts.setText(""); //$NON-NLS-1$
		if (updatePreferredLocationText) {
			selectedPreferredLocation.setText(""); //$NON-NLS-1$
		}

		selectedProjectSetProvider.setText(""); //$NON-NLS-1$
		selectedProjectSetLocation.setText(""); //$NON-NLS-1$
		selectedProjectSetTag.setText(""); //$NON-NLS-1$
		selectedProjectSetLocalDirectory.setText(""); //$NON-NLS-1$

		selectedProjectLoadedProvider.setText(""); //$NON-NLS-1$
		selectedProjectLoadedLocation.setText(""); //$NON-NLS-1$
		selectedProjectLoadedVersion.setText(""); //$NON-NLS-1$
		selectedProjectLoadedLocalDirectory.setText(""); //$NON-NLS-1$
	}

	/**
	 * Creates a table tree for showing the content of the project set
	 */
	private void createTableTreeArea(Composite parent) {

		Tree tree = createTableTree(parent);

		tableTreeViewer = new TreeViewer(tree);

		tableTreeViewer.setUseHashlookup(true);

		tableTreeViewer.setContentProvider(new ProjectSetTreeContentProvider());
		ProjectSetTreeLabelProvider labelProvider = new ProjectSetTreeLabelProvider();
		labelProvider.addListener(new ILabelProviderListener() {
			public void labelProviderChanged(LabelProviderChangedEvent event) {
				if (event.getElements() != null) {
					// collect project names from event objects
					List<String> changedProjectNames = new ArrayList<String>();
					Object[] eventElements = event.getElements();
					for (int i = 0; i < eventElements.length; i++) {
						Object object = eventElements[i];
						if (object instanceof IProject) {
							String projectName = ((IProject)object).getName();
							changedProjectNames.add(projectName);
						}
					}
					if (!changedProjectNames.isEmpty()) {
						// collect matching project set entries in tree
						List<ProjectSetEntry> psEntriesForUpdate = new ArrayList<ProjectSetEntry>();
						collectProjectSetEntriesFromTreeItems(
							tableTreeViewer.getTree().getItems(),
							psEntriesForUpdate,
							changedProjectNames);
	
						if (!psEntriesForUpdate.isEmpty()) {
							// update project set entries in tree viewer
							tableTreeViewer.update(psEntriesForUpdate.toArray(), PROJECT_NAME_COLUMN);
						}
					}
				}
			}	
		});
		tableTreeViewer.setLabelProvider(labelProvider);
		tableTreeViewer.setColumnProperties(COLUMN_PROPERTIES);

		tableTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection newSelection = (IStructuredSelection) (event.getSelection());
				// the comparison with previous selection is required !! 
				// (typing a space in ppl-Text field causes problems without comparison)
				if (!newSelection.equals(selection)) {	
					newSelection(newSelection);		
				}
			}
		});
		
		tableTreeViewer.setComparator(projectSetViewerComparator);

		tableTreeViewer.setInput(projectSet);

		tableTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (isOpenIncludedPSFEnabled) {
					openIncludedPSF((StructuredSelection) event.getSelection());
				}
				if (isSelectInNavigatorEnabled) {
					selectInNavigator();
				}
			}
		});
		
		tree.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.stateMask == SWT.CTRL && event.character == '\u0001') { // CTRL+A
					selectAll();
				}
				if (event.character == SWT.DEL || event.character == SWT.BS) {
					removeSelected();
				}
			}			
		});
	}
	
	/**
	 * Collect recursively in item tree all project set entries which names are in project names list
	 */
	private void collectProjectSetEntriesFromTreeItems(TreeItem[] treeItems, List<ProjectSetEntry> psEntryList, List<String> projectNamesList) {		

		for (int i = 0; i < treeItems.length; i++) {
			Object treeItemData = treeItems[i].getData();
			if (treeItemData instanceof ProjectSetEntry) {
				ProjectSetEntry psEntry = (ProjectSetEntry)treeItemData;
				if (projectNamesList.contains(psEntry.getProjectName())) {
					psEntryList.add(psEntry);
				}
			}
			// collect recursively
			collectProjectSetEntriesFromTreeItems(treeItems[i].getItems(), psEntryList, projectNamesList);
		}		
	}

	/**
	 * Sets the new selection and updates the view
	 * (This method is called when the selection in the tree was changed)
	 */
	protected void newSelection(IStructuredSelection newSelection) {
		selection = newSelection;
		divideSelectedEntries();
		updateButtonsAndDetailsView(true);
	}

	/**
	 * For better performance separate root entries
	 * from sub entries and store the lists in instance variables
	 * (for later use in deciding which actions are enabled)
	 */
	protected void divideSelectedEntries() {
		selectedRootEntries = new ArrayList<ProjectSetEntry>();
		selectedSubEntries = new ArrayList<ProjectSetEntry>();

		Iterator<ProjectSetEntry> selectedIterator = selection.iterator();
		while (selectedIterator.hasNext()) {
			ProjectSetEntry psEntry = selectedIterator.next();
			if (psEntry.getProjectSet().equals(projectSet)) {
				selectedRootEntries.add(psEntry);
			} else {
				selectedSubEntries.add(psEntry);
			}
		}
	}

	/**
	 * Updates the enable state for the actions (buttons and menu),
	 * the buttons and the area showing the details of the selected entry
	 */
	protected void updateButtonsAndDetailsView(boolean updatePreferredLocationText) {
		updateIsRemoveSelectedEnabled();
		updateIsSelectedToLoadedEnabled();
		updateIsLoadSelectedEnabled();
		updateIsLoadAllEnabled();
		updateIsOpenIncludedPSFEnabled();
		updateIsSelectInNavigatorEnabled();
		updateIsPreferredLocationEditable();

		updateButtonsState();
		updateDetailsArea(updatePreferredLocationText);
	}

	/**
	 * Updates the enable state for the actions (buttons and menu)
	 */
	protected void updateButtonsState() {

		removeButton.setEnabled(isRemoveSelectedEnabled);
		setSelectedToLoadedButton.setEnabled(isSetSelectedToLoadedEnabled);
		loadSelectedButton.setEnabled(isLoadSelectedEnabled);
		loadAllButton.setEnabled(isLoadAllEnabled);
		openIncludedPSFButton.setEnabled(isOpenIncludedPSFEnabled);

		selectedPreferredLocation.setEditable(isPreferredLocationEditable);
		if(isPreferredLocationEditable) {
			selectedPreferredLocation.setBackground(bgColor);
		} else {
			selectedPreferredLocation.setBackground(lightGreyColor);
		}
		addPathVariableButton.setEnabled(isPreferredLocationEditable);
		browseSelectedPreferredLocationButton.setEnabled(isPreferredLocationEditable);
		setCurrentSelectedPreferredLocationButton.setEnabled(isPreferredLocationEditable && (selectedRootEntries.get(0).getState()	!= ProjectSetEntry.STATE_NOT_LOADED));
	}

	/**
	 * Updates the enable state for the "remove selected" action
	 */
	protected void updateIsRemoveSelectedEnabled() {
		isRemoveSelectedEnabled =
			(!selectedRootEntries.isEmpty()) && (selectedRootEntries.size() == selection.size());
	}

	/**
	 * Updates the enable state for the "remove selected" action
	 */
	protected void updateIsPreferredLocationEditable() {
		isPreferredLocationEditable =
			(selectedRootEntries.size() == 1) && (selectedSubEntries.size() == 0) && ProjectSetUtil.isProviderSupportsPreferredLocalDirectory(selectedRootEntries.get(0).getProvider());
	}

	/**
	 * Updates the enable state for the "set selected to loaded" action
	 */
	protected void updateIsSelectedToLoadedEnabled() {

		if ((!selectedRootEntries.isEmpty()) && (selectedRootEntries.size() == selection.size())) {
			boolean enabled = true;
			Iterator<ProjectSetEntry> selectedRootEntryIterator = selectedRootEntries.iterator();
			while (selectedRootEntryIterator.hasNext() && enabled) {
				ProjectSetEntry psEntry = selectedRootEntryIterator.next();
				int entryState = psEntry.getState();
				if (entryState != ProjectSetEntry.STATE_LOADED_DIFFERENT) {
					enabled = false;
				}
			}
			isSetSelectedToLoadedEnabled = enabled;
		} else {
			isSetSelectedToLoadedEnabled = false;
		}
	}

	/**
	 * Updates the enable state for the "load all" action
	 */
	protected void updateIsLoadAllEnabled() {
		isLoadAllEnabled =	(projectSet.getProjectSetEntries().length > 0) && (!projectSet.hasUndefinedPathVariable(loadRecursive));
	}
	
	/**
	 * Updates the enable state for the "load selected" action
	 */
	protected void updateIsLoadSelectedEnabled() {
		boolean enabled = false;
		// test if selection is not empty
		if (!selection.isEmpty()) {
			// check that none of the selected has an undefined path variable
			Iterator<ProjectSetEntry> selIterator = selection.iterator();
			while (selIterator.hasNext()) {			
				ProjectSetEntry psEntry = selIterator.next();
				int pathVarUndefinedState = psEntry.getErrorState(ProjectSetEntry.ERROR_TYPE_PATH_VARIABLE_UNDEFINED);
				if(loadRecursive) {
					if (pathVarUndefinedState != ProjectSetEntry.ERROR_STATE_OK) {
						isLoadSelectedEnabled = false;
						return;
					}
				} else {
					if ((pathVarUndefinedState == ProjectSetEntry.ERROR_STATE_ERROR) ||
						(pathVarUndefinedState == ProjectSetEntry.ERROR_STATE_ERROR_ALSO_IN_SUB_PS)) {
						isLoadSelectedEnabled = false;
						return;
					}					
				}
			}
			
			// test if only root entries are selected
			if ((selectedRootEntries.size() == selection.size())) {
				enabled = true;
			} else {

				Iterator<ProjectSetEntry> selectionIterator = selection.iterator();
				Map<String, ProjectSetEntry> map = new HashMap<String, ProjectSetEntry>(); // key = project name, value = ps entry
				enabled = true;
				while (selectionIterator.hasNext() && enabled) {
					ProjectSetEntry psEntry = selectionIterator.next();
					String projectName = psEntry.getProjectName();
					// check that all entries has different project names
					// or has equal content
					ProjectSetEntry psEntryInMap = map.get(projectName);
					if (psEntryInMap == null) {
						map.put(projectName, psEntry);
					} else {
						if (!psEntryInMap.contentEqualsIgnorePreferredLocation(psEntry)) {
							enabled = false;
						}
					}
					// check that no parent is selected

					ProjectSet includingPS = psEntry.getProjectSet();
					ProjectSetEntry parentEntry = includingPS.getParentProjectSetEntry();
					boolean parentIsSelected = false;
					Set<ProjectSetEntry> parentEntries = new HashSet<ProjectSetEntry>(); //used to avoid infinite recursion 
					while ((parentEntry != null) && (!parentIsSelected) && (parentEntries.add(parentEntry)) ) {
						if (selection.toList().indexOf(parentEntry) != -1) {
							parentIsSelected = true;
						} else {
							parentEntry = parentEntry.getProjectSet().getParentProjectSetEntry();
						}
					}
					if (parentIsSelected) {
						enabled = false;
					}
				}
			}
		}
		isLoadSelectedEnabled = enabled;
	}

	/**
	 * Updates the enable state for the "load selected" action
	 */
	protected void updateIsSelectInNavigatorEnabled() {
		isSelectInNavigatorEnabled = getSelectedProjectsInWorkspace().length > 0;
	}
	
	/**
	 * Updates the enable state for the "open included PSF" action
	 */
	protected void updateIsOpenIncludedPSFEnabled() {
		if (!selection.isEmpty()) {
			boolean enabled = true;
			Iterator<ProjectSetEntry> selectedEntryIterator = selection.iterator();
			while (selectedEntryIterator.hasNext() && enabled) {
				ProjectSetEntry psEntry = selectedEntryIterator.next();
				int entryState = psEntry.getState();
				if (entryState == ProjectSetEntry.STATE_LOADED) {
					IProject loadedProject = psEntry.getProjectInWorkspace();
					IProjectNature nature = null;
					try {
						if (loadedProject != null) {
							nature = loadedProject.getNature(ProjectSetNature.PS_PROJECT_NATURE_ID);
						}
					} catch (CoreException e) {
					}
					if (nature == null) {
						enabled = false;
					}
				} else {
					enabled = false;
				}
			}

			isOpenIncludedPSFEnabled = enabled;
		} else {
			isOpenIncludedPSFEnabled = false;
		}
	}

	/**
	 * Updates the details area depending on the selected entry
	 */
	protected void updateDetailsArea(boolean updatePreferredLocationText) {
		boolean oldNoUserInput = noUserInput;
		noUserInput = true;
		if ((selection.isEmpty()) || (selection.size() > 1)) {
			clearDetails(updatePreferredLocationText);
		} else {
			showDetails((ProjectSetEntry) selection.getFirstElement(), updatePreferredLocationText);
		}
		noUserInput = oldNoUserInput;
	}

	/**
	 * Creates a table tree for showing the content of the project set
	 */
	protected Tree createTableTree(Composite parent) {

		// create group for flat border around table

		Group borderGroup = new Group(parent, SWT.NONE);
		GridLayout borderGroupLayout = new GridLayout();
		borderGroupLayout.marginWidth = 0;
		borderGroupLayout.marginHeight = 0;
		borderGroupLayout.horizontalSpacing = 0;
		borderGroupLayout.verticalSpacing = 0;
		borderGroup.setLayout(borderGroupLayout);
		borderGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		borderGroup.setBackground(bgColor);
		createMenu(borderGroup);

		Tree tree = new Tree(borderGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);

		tree.setHeaderVisible(true);
		GridData tableGridData = new GridData(GridData.FILL_BOTH);
		tree.setLayoutData(tableGridData);
		
		TreeColumn projectNameCol = createTreeColumn(tree, Messages.getString("ProjectSetEditor.Name"), 200, IProjectSetUIConstants.PROJECT_SET_ENTRY_PROJECT_NAME); //$NON-NLS-1$
		createTreeColumn(tree, Messages.getString("ProjectSetEditor.Tag"), 100, IProjectSetUIConstants.PROJECT_SET_ENTRY_TAG); //$NON-NLS-1$
		createTreeColumn(tree, Messages.getString("ProjectSetEditor.Preferred_path_problems"), 20, IProjectSetUIConstants.PROJECT_SET_ENTRY_PREFERRED_PATH_PROBLEMS); //$NON-NLS-1$
		createTreeColumn(tree, Messages.getString("ProjectSetEditor.Tag_Conflicts"), 20, IProjectSetUIConstants.PROJECT_SET_ENTRY_TAG_CONFLICTS); //$NON-NLS-1$
		createTreeColumn(tree, Messages.getString("ProjectSetEditor.State"), 100, IProjectSetUIConstants.PROJECT_SET_ENTRY_STATE); //$NON-NLS-1$
		createTreeColumn(tree, Messages.getString("ProjectSetEditor.Provider"), 0, IProjectSetUIConstants.PROJECT_SET_ENTRY_PROVIDER); //$NON-NLS-1$
		createTreeColumn(tree, Messages.getString("ProjectSetEditor.Server_Location"), 0, IProjectSetUIConstants.PROJECT_SET_ENTRY_SERVER_LOCATION); //$NON-NLS-1$

		if (sortEntriesInitially()) {
			tree.setSortColumn(projectNameCol);
			tree.setSortDirection(SWT.UP);
			projectSetViewerComparator.setProjectSetEntryProperty(IProjectSetUIConstants.PROJECT_SET_ENTRY_PROJECT_NAME);
			projectSetViewerComparator.setAscending(true);
		}
		
		return tree;
	}
	
	private boolean sortEntriesInitially() {
		return Platform.getPreferencesService().getBoolean(ProjectSetUIPlugin.PLUGIN_ID, IProjectSetUIConstants.EDITOR_SORT_ENTRIES, true, null);
	}
	
	private TreeColumn createTreeColumn(final Tree tree, String text, int width, final int projectSetEntryProperty) {
		final TreeColumn treeColumn = new TreeColumn(tree, SWT.LEFT);
		treeColumn.setText(text);
		treeColumn.setWidth(width);
//		treeColumn.setData(new Integer(projectSetEntryProperty));
		treeColumn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TreeColumn currentSortColumn = tableTreeViewer.getTree().getSortColumn();
				boolean ascendingToBeSet;
				int projectSetEntryPropertyToBeSet = projectSetEntryProperty;
				if (currentSortColumn != treeColumn) {
					tree.setSortColumn(treeColumn);
					tree.setSortDirection(SWT.UP);
					ascendingToBeSet = true;
				} else {
					if (tree.getSortDirection() == SWT.UP) {
						tree.setSortDirection(SWT.DOWN);
						ascendingToBeSet = false;
					} else if (tree.getSortDirection() == SWT.DOWN) {
						tree.setSortColumn(null);
						projectSetEntryPropertyToBeSet = UNSORTED;
						ascendingToBeSet = true;
					} else {
						tree.setSortDirection(SWT.UP);
						ascendingToBeSet = true;
					}
				}
				projectSetViewerComparator.setAscending(ascendingToBeSet);
				projectSetViewerComparator.setProjectSetEntryProperty(projectSetEntryPropertyToBeSet);
				
				try {
					tableTreeViewer.getTree().setRedraw(false);
					tableTreeViewer.refresh();
				} finally {
					tableTreeViewer.getTree().setRedraw(true);
				}
			}
		});
		return treeColumn;
	}

	/**
	 * Creates the context menu for the table tree
	 */
	private void createTableTreeMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				tableContextMenuAboutToShow(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(tableTreeViewer.getControl());
		tableTreeViewer.getControl().setMenu(menu);
		getEditorSite().registerContextMenu(menuMgr, tableTreeViewer, false);
	}

	/**
	 * Creates the actions that will be called by the context menu 
	 */
	protected void makeActions() {
		
		saveAction = new Action(Messages.getString("ProjectSetEditor.Save_Project_Set"), //$NON-NLS-1$
				ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/save.gif")) {//$NON-NLS-1$
		public void run() {
				doSave(null);
			}
		};
		
		addSharedProjectsAction = new Action(
				Messages.getString("ProjectSetEditor.Add_shared_projects_to_set_"), //$NON-NLS-1$
				ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/add.gif")) {//$NON-NLS-1$
			public void run() {
				selectAndAddWorkspaceProjects();
			}
		};
		
		removeSelectedAction = new Action(
				Messages.getString("ProjectSetEditor.Remove_selected_from_set"), //$NON-NLS-1$
				ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/delete.gif")) {//$NON-NLS-1$
			public void run() {
				removeSelected();
			}
		};
		
		setSelectedToLoadedAction = new Action(
				Messages.getString("ProjectSetEditor.Set_selected_to_loaded_tag"), //$NON-NLS-1$
				ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/settoloaded.gif")) {//$NON-NLS-1$
			public void run() {
				setSelectedToLoaded();
			}
		};
		
		loadReplaceSelectedAction = new Action(
				Messages.getString("ProjectSetEditor.Load/Replace_selected"), //$NON-NLS-1$
				ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/load.gif")) {//$NON-NLS-1$
			public void run() {
				loadSelected();
			}
		};
		loadReplaceAllAction = new Action(
				Messages.getString("ProjectSetEditor.Load/Replace_all"), //$NON-NLS-1$
				ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/loadall.gif")) {//$NON-NLS-1$
			public void run() {
				loadAll();
			}
		};

		openIncludedPSFAction = new Action(Messages.getString("ProjectSetEditor.Open_included_PSF"), //$NON-NLS-1$
				ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/psffile.gif")) {//$NON-NLS-1$
			public void run() {
				openIncludedPSF();
			}
		};
		
		selectInNavigatorAction = new Action(Messages.getString("ProjectSetEditor.Select_in_Navigator"), //$NON-NLS-1$
				ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/navigator.gif")) {//$NON-NLS-1$
			public void run() {
				selectInNavigator();
			}
		};

		selectAllAction = new Action(Messages.getString("ProjectSetEditor.Select_all"), //$NON-NLS-1$
				ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/selectall.png")) {//$NON-NLS-1$
	        public void run() {
				selectAll();
			}
		};
		
		selectLoadedAction = new Action(
				Messages.getString("ProjectSetEditor.Select_loaded"), //$NON-NLS-1$
				ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/loaded.png")) {//$NON-NLS-1$
	        public void run() {
				selectLoaded();
			}
		};

		selectUnloadedAction = new Action(Messages.getString("ProjectSetEditor.Select_unloaded"), //$NON-NLS-1$
				ProjectSetUIPlugin.getDefault().getImageDescriptor("icons/notloaded.png")) {//$NON-NLS-1$
	        public void run() {
				selectUnloaded();
			}
		};
	}

	/**
	 * Creates the context menu for the table tree
	 */
	public void tableContextMenuAboutToShow(IMenuManager menuManager) {

		menuManager.add(saveAction);
		if (isDirty()) {
			saveAction.setEnabled(true);
		} else {
			saveAction.setEnabled(false);
		}

		menuManager.add(new Separator("tableActions")); //$NON-NLS-1$

		if (isLoadSelectedEnabled)
			menuManager.add(loadReplaceSelectedAction);
		if (isLoadAllEnabled) 
			menuManager.add(loadReplaceAllAction);
		
		menuManager.add(new Separator("addRemoveActions")); //$NON-NLS-1$
		
		menuManager.add(addSharedProjectsAction);
		if (isRemoveSelectedEnabled)
			menuManager.add(removeSelectedAction);
		
		menuManager.add(new Separator("selectedToLoadedActions")); //$NON-NLS-1$
		
		if (isSetSelectedToLoadedEnabled)
			menuManager.add(setSelectedToLoadedAction);
		
		menuManager.add(new Separator("selectionActions")); //$NON-NLS-1$
		
		if (projectSet.getProjectSetEntries().length > 0)
			menuManager.add(selectAllAction);
		
		if (projectSet.getProjectSetEntries().length > 0)
			menuManager.add(selectLoadedAction);
		
		if (projectSet.getProjectSetEntries().length > 0)
			menuManager.add(selectUnloadedAction);
		
		menuManager.add(new Separator("openAndSelectActions")); //$NON-NLS-1$
		
		if (isOpenIncludedPSFEnabled)
			menuManager.add(openIncludedPSFAction);
		
		if (isSelectInNavigatorEnabled)
			menuManager.add(selectInNavigatorAction);

		menuManager.add(new Separator("additions")); //$NON-NLS-1$

	}

	/**
	 * Creates the area with the button
	 */
	protected void createButtonArea(Composite parent) {
		Composite buttonGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		buttonGroup.setLayout(layout);
		buttonGroup.setLayoutData(
			new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_BEGINNING));
		buttonGroup.setBackground(bgColor);
		createMenu(buttonGroup);

		loadSelectedButton = createButton(buttonGroup, Messages.getString("ProjectSetEditor.Load/Replace_selected")); //$NON-NLS-1$
		loadSelectedButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				loadSelected();
			}
		});

		loadAllButton = createButton(buttonGroup, Messages.getString("ProjectSetEditor.Load/Replace_all")); //$NON-NLS-1$
		loadAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				loadAll();
			}
		});

		addButtonSeparator(buttonGroup);
		
		addButton = createButton(buttonGroup, Messages.getString("ProjectSetEditor.Add_shared_projects_to_set_")); //$NON-NLS-1$
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				selectAndAddWorkspaceProjects();
			}
		});

		removeButton = createButton(buttonGroup, Messages.getString("ProjectSetEditor.Remove_selected_from_set")); //$NON-NLS-1$
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				removeSelected();
			}
		});
		
		addButtonSeparator(buttonGroup);

		setSelectedToLoadedButton = createButton(buttonGroup, Messages.getString("ProjectSetEditor.Set_selected_to_loaded_tag")); //$NON-NLS-1$
		setSelectedToLoadedButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setSelectedToLoaded();
			}
		});
		
		addButtonSeparator(buttonGroup);
		
		selectAllButton = createButton(buttonGroup, Messages.getString("ProjectSetEditor.Select_all")); //$NON-NLS-1$
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				selectAll();
			}
		});
		
		selectLoadedButton = createButton(buttonGroup, Messages.getString("ProjectSetEditor.Select_loaded")); //$NON-NLS-1$
		selectLoadedButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				selectLoaded();
			}
		});
		
		selectUnloadedButton = createButton(buttonGroup, Messages.getString("ProjectSetEditor.Select_unloaded")); //$NON-NLS-1$
		selectUnloadedButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				selectUnloaded();
			}
		});
		
		addButtonSeparator(buttonGroup);

		openIncludedPSFButton = createButton(buttonGroup, Messages.getString("ProjectSetEditor.Open_included_PSF")); //$NON-NLS-1$
		openIncludedPSFButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				openIncludedPSF();
			}
		});
		
		addButtonSeparator(buttonGroup);

		loadRecursiveCheckbox = createCheckbox(buttonGroup, Messages.getString("ProjectSetEditor.Load/Replace_recursively")); //$NON-NLS-1$
		loadRecursiveCheckbox.setSelection(loadRecursive);
		loadRecursiveCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (loadRecursive == true) {
					loadRecursive = false;
				} else {
					loadRecursive = true;
				}
			}
		});
	}
	
	private void addButtonSeparator(Composite composite) {
		Label separatorLabel = new Label(composite, SWT.NONE);
		separatorLabel.setText(""); //$NON-NLS-1$
		
	}

	/**
	 * Creates and returns a button
	 */
	protected Button createButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.PUSH | SWT.FLAT);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.setText(text);
		button.setBackground(bgColor);
		return button;
	}

	/**
	 * Creates and returns a checkbox
	 */
	protected Button createCheckbox(Composite parent, String text) {
		Button button = new Button(parent, SWT.CHECK | SWT.FLAT);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.setText(text);
		button.setBackground(bgColor);
		return button;
	}

	/** 
	 * returns the loaded project or "shadow"-project
	 */
	protected IProject getLoadedProject(String projectName) {
		return ProjectSetPlugin.getWorkspace().getRoot().getProject(projectName);
	}

	/**
	 * Opens a dialog for adding projects, add the projects and update the view  
	 */
	protected void selectAndAddWorkspaceProjects() {
		List<String> excludeProjectList = getProjectNames();
		// also exclude project of psf file
		excludeProjectList.add(projectSet.getProjectSetFile().getProject().getName());
		IProject[] selectedProjects = selectSharedProjects(excludeProjectList);
		if ((selectedProjects != null) && (selectedProjects.length > 0)) {
			setDirty(true);
			for (int i = 0; i < selectedProjects.length; i++) {
				IProject project = selectedProjects[i];
				ProjectSetEntry entryInSet = projectSet.getEntryForProject(project.getName());
				ProjectSetEntry entryForLoaded;
				try {
					entryForLoaded = new ProjectSetEntry(projectSet, project);
					if (entryInSet == null) {
						// add new entry
						projectSet.addProjectSetEntry(entryForLoaded);
					} else {
						// replace data in set
						entryInSet.setDataFrom(entryForLoaded);
					}
				} catch (NewProjectSetEntryInvTargetException | NewProjectSetEntryException e) {
					openErrorDialog(getShell(), e);
					updateEntriesAndView();
				}
			}
		}
		updateEntriesAndView();
	}

	/**
	 * Opens a dialog for adding projects and returns the selected projects 
	 */
	protected IProject[] selectSharedProjects(List<String> excludeProjectList) {
			SharedProjectsSelectionDialog selectionDialog = new SharedProjectsSelectionDialog(getShell(), Messages.getString("ProjectSetEditor.Select_Shared_Projects"), //$NON-NLS-1$
		Messages.getString("ProjectSetEditor.Select_shared_projects_to_be_added_to_project_set"), //$NON-NLS-1$
	excludeProjectList);
		selectionDialog.open();
		return (IProject[]) selectionDialog.getResult();
	}

	/**
	 * Returns the project names of the "root" entries
	 */
	protected List<String> getProjectNames() {
		List<String> projectNames = new ArrayList<String>();
		ProjectSetEntry[] entries = projectSet.getProjectSetEntries();
		for (int i = 0; i < entries.length; i++) {
			projectNames.add(entries[i].getProjectName());
		}
		return projectNames;
	}

	/**
	 * Sets the selected projects entries to the loaded tag and update the view
	 * 
	 * pre: projects of selected entries must be loaded and shared
	 */
	protected void setSelectedToLoaded() {
		Iterator<ProjectSetEntry> setEntryIterator = selection.iterator();
		while (setEntryIterator.hasNext()) {
			ProjectSetEntry projectSetEntry = setEntryIterator.next();

			IProject loadedProject = getLoadedProject(projectSetEntry.getProjectName());

			ProjectSetEntry projectSetEntryForLoaded;
			try {
				projectSetEntryForLoaded = new ProjectSetEntry(projectSet, loadedProject);
				if (!projectSetEntry.contentEqualsIgnorePreferredLocation(projectSetEntryForLoaded)) {
					projectSetEntry.setDataFrom(projectSetEntryForLoaded);
					setDirty(true);
				}
			} catch (NewProjectSetEntryInvTargetException | NewProjectSetEntryException e) {
				// TODO: Log the exception and later on do something else if a problem occurs.
				e.printStackTrace();
			}
		}
		updateEntriesAndView();
	}

	/**
	 * Removes the selected projects entries and update the view
	 */
	protected void removeSelected() {
		if (!selection.isEmpty()) {
			setDirty(true);
			Iterator<ProjectSetEntry> selectionIterator = selection.iterator();
			while (selectionIterator.hasNext()) {
				ProjectSetEntry psEntry = selectionIterator.next();
				projectSet.removeProjectSetEntry(psEntry);

			}
			updateEntriesAndView();
		}
	}

	/**
	 * Adds/replaces the selected projects with the tag specified in the entries
	 * and update the view
	 */
	protected void loadSelected() {
		// load root entries
		List<String> projectNames = new ArrayList<String>();
		Iterator<ProjectSetEntry> selectedRootEntryIterator = selectedRootEntries.iterator();
		while (selectedRootEntryIterator.hasNext()) {
			ProjectSetEntry rootProjectSetEntry = selectedRootEntryIterator.next();
			projectNames.add(rootProjectSetEntry.getProjectName());
		}

		ProjectSetUI.addToWorkspace(projectSet, getShell(), projectNames, loadRecursive);

		loadSelectedSubEntries();

		updateEntriesAndView();
	}

	/**
	 * Adds/replaces the selected projects for the selected sub entries
	 */
	protected void loadSelectedSubEntries() {
		// collected sub entries group by projectset
		Iterator<ProjectSetEntry> selectedSubEntryIterator = selectedSubEntries.iterator();
		// map with: key = projectSet, value = list of project names
		Map<ProjectSet, List<String>> subMap = new HashMap<ProjectSet, List<String>>();
		while (selectedSubEntryIterator.hasNext()) {
			ProjectSetEntry subProjectSetEntry = selectedSubEntryIterator.next();
			ProjectSet subPS = subProjectSetEntry.getProjectSet();
			if (subPS == null) {
				//should never happen !!
				System.out.println(Messages.getString("ProjectSetEditor.Project_set_entry_without_project_set")); //$NON-NLS-1$
			} else {
				List<String> projectNameList = subMap.get(subPS);
				if (projectNameList == null) {
					projectNameList = new ArrayList<String>();
					subMap.put(subPS, projectNameList);
				}
				projectNameList.add(subProjectSetEntry.getProjectName());
			}
		}

		// load sub entries
		Iterator<Map.Entry<ProjectSet, List<String>>> mapEntryIterator = subMap.entrySet().iterator();
		while (mapEntryIterator.hasNext()) {
			Map.Entry<ProjectSet, List<String>> mapEntry = mapEntryIterator.next();
			ProjectSet subProjectSet = mapEntry.getKey();
			List<String> projectNames = mapEntry.getValue();

			//subProjectSet.load();
			ProjectSetUI.addToWorkspace(subProjectSet, getShell(), projectNames, loadRecursive);
		}
	}

	/**
	 * Adds/replaces the all with the tag specified in the entries
	 * and update the view
	 */
	protected void loadAll() {
		ProjectSetUI.addAllToWorkspace(projectSet, getShell(), loadRecursive);
		updateEntriesAndView();
	}

	/**
	 * Updates the entries and the errors
	 * and refresh the view
	 */
	protected void updateEntriesAndView() {
		if (projectSet != null) {
			projectSet.updateEntries();
			updateErrorsAndView(true);
		}
	}

	protected void updateErrorsAndView(boolean updatePreferredLocationText) {
		if (projectSet != null) {
			projectSet.updateErrors();

			if ((tableTreeViewer != null) && (!tableTreeViewer.getControl().isDisposed())) {
				tableTreeViewer.refresh();		
				updateButtonsAndDetailsView(updatePreferredLocationText);
			}
		}
	}

	/**
	 * Selects all root entries in the tree table
	 */
	protected void selectAll() {
		tableTreeViewer.setSelection(new StructuredSelection(projectSet.getProjectSetEntries()));
	}
	
	/**
	 * Selects all loaded root entries in the tree table
	 */
	protected void selectLoaded() {
		List<ProjectSetEntry> loaded = new ArrayList<ProjectSetEntry>();
		for (ProjectSetEntry projectSetEntry : projectSet.getProjectSetEntries()) {
			int projectSetEntryState = projectSetEntry.getState();
			if (projectSetEntryState == ProjectSetEntry.STATE_LOADED || projectSetEntryState == ProjectSetEntry.STATE_LOADED_DIFFERENT) {
				loaded.add(projectSetEntry);
			}
		}
		tableTreeViewer.setSelection(new StructuredSelection(loaded));
	}
	
	/**
	 * Selects all unloaded root entries in the tree table
	 */
	protected void selectUnloaded() {
		List<ProjectSetEntry> loaded = new ArrayList<ProjectSetEntry>();
		for (ProjectSetEntry projectSetEntry : projectSet.getProjectSetEntries()) {
			if (projectSetEntry.getState() == ProjectSetEntry.STATE_NOT_LOADED) {
				loaded.add(projectSetEntry);
			}
		}
		tableTreeViewer.setSelection(new StructuredSelection(loaded));
	}
	
	/**
	 * Opens editors for the project sets of project set projects
	 * in the selected entries
	 */
	protected void openIncludedPSF() {
		openIncludedPSF(selection);
	}

	/**
	 * Selects the selected projects in all navigator view
	 */
	protected void selectInNavigator() {
		if (selection != null) {
			// collect selected projects
			IProject[] selectedProjects = getSelectedProjectsInWorkspace();
			
			if (selectedProjects.length > 0) {
				// set selection in all navigators
				IViewReference[] viewReferences = getSite().getPage().getViewReferences();
				for (int i = 0; i < viewReferences.length; i++) {
					IViewReference reference = viewReferences[i];
					IViewPart view = reference.getView(false);
					if (view instanceof ISetSelectionTarget) {
						ISetSelectionTarget navigator = (ISetSelectionTarget)view;
						navigator.selectReveal(new StructuredSelection(selectedProjects));
					}
				}
			}
		}
	}

	/**
	 * Return the (loaded) projects of the selected project set entries 
	 */
	protected IProject[] getSelectedProjectsInWorkspace() {
		if (selection != null) {
			// collect selected projects
			Set<IProject> selectedProjects = new HashSet<IProject>(); 
			Iterator<ProjectSetEntry> it = selection.iterator();
			while (it.hasNext()) {
				ProjectSetEntry psEntry = it.next();
				IProject project = ProjectSetPlugin.getWorkspace().getRoot().getProject(psEntry.getProjectName());
				if ((project != null) && (project.exists())) {
					selectedProjects.add(project);
				}
			}
			return selectedProjects.toArray(new IProject[selectedProjects.size()]);
		} else {
			return new IProject[0];
		}
	}
	
	/**
	 * Opens editors for the project sets of project set projects
	 * in the selection
	 */
	protected void openIncludedPSF(IStructuredSelection structuredSelection) {
		Iterator<ProjectSetEntry> setEntryIterator = structuredSelection.iterator();
		while (setEntryIterator.hasNext()) {
			ProjectSetEntry projectSetEntry = setEntryIterator.next();
			IProject project = projectSetEntry.getProjectInWorkspace();
			if (project != null) {
				ProjectSetNature nature = null;
				try {
					nature =
						(ProjectSetNature) project.getNature(ProjectSetNature.PS_PROJECT_NATURE_ID);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				if (nature != null) {
					IWorkbenchPage page = ProjectSetUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IFile psFile = nature.getProjectSetFile();
						if (psFile == null) {
							MessageBox msgBox = new MessageBox(getShell());
							msgBox.setText(Messages.getString("ProjectSetEditor.Error")); //$NON-NLS-1$
							msgBox.setMessage(Messages.getString("ProjectSetEditor.Included_PSF_file_can_not_be_opened")); //$NON-NLS-1$
							msgBox.open();
						} else {
							page.openEditor(new FileEditorInput(psFile), PSF_EDITOR_ID);
						}
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Return the title for error dialog in case of fail of create of ProjectSetEntry
	 * 
	 * @return returns the title for error dialog
	 */
	private String getErrorDialogTitle() {
		return Messages.getString("ProjectSetEditor.Error");
	}

	/**
	 * Open error dialog in case of fail of create of ProjectSetEntry
	 */
	private void openErrorDialog(Shell shell, Exception e) {
		String errorMessage = Messages.getString("ProjectSetEditor.Error_PSF_file_creation"); // $NON-NLS-1$

		if (e instanceof NewProjectSetEntryInvTargetException) {
			Throwable target = ((NewProjectSetEntryInvTargetException) e).getTargetException();
			if ((target instanceof TeamException) && (target.getMessage() != null)) {
				errorMessage = errorMessage + MessageFormat.format(Messages.getString("ProjectSetEditor.,_reason__{0}"), new Object[] { target.getMessage() }); // $NON-NLS-1$
			}
		} else {
			errorMessage = errorMessage + MessageFormat.format(Messages.getString("ProjectSetEditor.,_reason__{0}"), new Object[] { e.getMessage() }); // $NON-NLS-1$
		}
		MessageDialog.openError(shell, getErrorDialogTitle(), errorMessage);
	}
}
