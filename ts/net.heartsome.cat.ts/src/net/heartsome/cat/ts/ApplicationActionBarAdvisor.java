package net.heartsome.cat.ts;

import java.util.Arrays;
import java.util.List;

import net.heartsome.cat.ts.resource.Messages;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.ReopenEditorMenu;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.menus.IMenuService;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the actions added to a workbench window.
 * Each window will be populated with new actions.
 */
@SuppressWarnings("restriction")
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	private final IWorkbenchWindow window;

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.

	// tool bar context menu
	private IWorkbenchAction lockToolBarAction;

	// file menu action
	private IWorkbenchAction exitAction;

	// edit menu action
	private IWorkbenchAction undoAction;
	private IWorkbenchAction redoAction;
	private IWorkbenchAction cutAction;
	private IWorkbenchAction copyAction;
	private IWorkbenchAction pasteAction;
	private IWorkbenchAction deleteAction;
	private IWorkbenchAction findAction;

	// help menu action
	private IWorkbenchAction helpAction;

	/**
	 * Indicates if the action builder has been disposed
	 */
	private boolean isDisposed = false;

	/**
	 * The coolbar context menu manager.
	 */
	private MenuManager coolbarPopupMenuManager;

	/**
	 * @param configurer
	 */
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
		window = configurer.getWindowConfigurer().getWindow();
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {

		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		exitAction.setImageDescriptor(net.heartsome.cat.ts.ui.Activator.getImageDescriptor("images/file/logout.png"));
		exitAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.exitAction"));

		undoAction = ActionFactory.UNDO.create(window);
		undoAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.undoAction"));
		undoAction.setImageDescriptor(net.heartsome.cat.ts.ui.Activator.getImageDescriptor("images/edit/undo.png"));
		register(undoAction);

		redoAction = ActionFactory.REDO.create(window);
		redoAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.redoAction"));
		redoAction.setImageDescriptor(net.heartsome.cat.ts.ui.Activator.getImageDescriptor("images/edit/redo.png"));
		register(redoAction);

		cutAction = ActionFactory.CUT.create(window);
		register(cutAction);
		cutAction.setImageDescriptor(net.heartsome.cat.ts.ui.Activator.getImageDescriptor("images/edit/cut.png"));
		cutAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.cutAction"));

		copyAction = ActionFactory.COPY.create(window);
		copyAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.copyAction"));
		copyAction.setImageDescriptor(net.heartsome.cat.ts.ui.Activator.getImageDescriptor("images/edit/copy.png"));
		register(copyAction);

		pasteAction = ActionFactory.PASTE.create(window);
		pasteAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.pasteAction"));
		pasteAction.setImageDescriptor(net.heartsome.cat.ts.ui.Activator.getImageDescriptor("images/edit/paste.png"));
		register(pasteAction);

		deleteAction = ActionFactory.DELETE.create(window);
		deleteAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.deleteAction"));
		deleteAction.setImageDescriptor(net.heartsome.cat.ts.ui.Activator.getImageDescriptor("images/edit/delete.png"));
		register(deleteAction);

		findAction = ActionFactory.FIND.create(window);
		findAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.findAction"));
		findAction.setImageDescriptor(net.heartsome.cat.ts.ui.Activator
				.getImageDescriptor("images/edit/search_replace.png"));
		register(findAction);

		lockToolBarAction = ActionFactory.LOCK_TOOL_BAR.create(window);
		register(lockToolBarAction);

		helpAction = ActionFactory.HELP_CONTENTS.create(window);
		helpAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.helpAction"));
		helpAction.setImageDescriptor(Activator.getImageDescriptor("images/help/help.png"));
		register(helpAction);

		removeUnusedAction();

	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		menuBar.add(createFileMenu());
		menuBar.add(createEditMenu());
		menuBar.add(new GroupMarker("view"));
		menuBar.add(new GroupMarker("translation"));
		menuBar.add(new GroupMarker("project"));
		menuBar.add(new GroupMarker("database"));
		menuBar.add(new GroupMarker("qa"));
		menuBar.add(createToolMenu());
		menuBar.add(new GroupMarker("advance"));
		menuBar.add(createHelpMenu());
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		// Set up the context Menu
		coolbarPopupMenuManager = new MenuManager();
		coolbarPopupMenuManager.add(new ActionContributionItem(lockToolBarAction));
		coolBar.setContextMenuManager(coolbarPopupMenuManager);
		IMenuService menuService = (IMenuService) window.getService(IMenuService.class);
		menuService.populateContributionManager(coolbarPopupMenuManager, "popup:windowCoolbarContextMenu");

		coolBar.add(new GroupMarker("group.file"));

		coolBar.add(new GroupMarker("group.new.menu"));

		coolBar.add(new GroupMarker("group.undoredo"));

		coolBar.add(new GroupMarker("group.copySource"));

		coolBar.add(new GroupMarker("group.search"));
		createToolItem(coolBar);

		coolBar.add(new GroupMarker("group.completeTranslation"));

		coolBar.add(new GroupMarker("group.approve"));

		coolBar.add(new GroupMarker("group.addTerm"));

		coolBar.add(new GroupMarker("group.preview"));

		coolBar.add(new GroupMarker("group.tagoperation"));

		coolBar.add(new GroupMarker("group.sourceoperation"));

		coolBar.add(new GroupMarker("group.deleteTrans"));

		coolBar.add(new GroupMarker("group.changeLayout"));

		coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));

		coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_HELP));

	}

	private IToolBarManager createToolItem(ICoolBarManager coolBar) {
		IToolBarManager toolBar = new ToolBarManager(coolBar.getStyle());
		coolBar.add(new ToolBarContributionItem(toolBar, "findreplace"));
		toolBar.add(findAction);
		return toolBar;
	}

	/**
	 * 创建工具菜单
	 * @return 返回工具菜单的 menu manager;
	 */
	private MenuManager createToolMenu() {
		MenuManager menu = new MenuManager(Messages.getString("ts.ApplicationActionBarAdvisor.menu.tool"),
				"net.heartsome.cat.ts.ui.menu.tool") {
			@Override
			public boolean isVisible() {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (workbenchWindow == null)
					return false;
				IWorkbenchPage activePage = workbenchWindow.getActivePage();
				if (activePage == null)
					return false;
				if (activePage.getPerspective().getId().contains("net.heartsome.cat.ts.perspective"))
					return true;
				return false;
			}
		}; // &Tool
		menu.add(new GroupMarker("pluginConfigure"));
		menu.add(new GroupMarker("preference.groupMarker"));
		// menu.add(preferenceAction);
		return menu;
	}

	/**
	 * 创建文件菜单
	 * @return 返回文件菜单的 menu manager;
	 */
	private MenuManager createFileMenu() {
		MenuManager menu = new MenuManager(Messages.getString("ts.ApplicationActionBarAdvisor.menu.file"),
				IWorkbenchActionConstants.M_FILE); // &File
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		// 添加 new.ext group，这样 IDE 中定义的 Open File... 可以显示在最顶端
		menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
		menu.add(new Separator());
		menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
		menu.add(new GroupMarker("xliff.switch"));
		menu.add(new GroupMarker("rtf.switch"));
		menu.add(new GroupMarker("xliff.split"));
		menu.add(new Separator());
		// 设置保存文件记录条数为 5 条
		WorkbenchPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.RECENT_FILES, 5);
		// 添加文件访问列表
		ContributionItemFactory REOPEN_EDITORS = new ContributionItemFactory("reopenEditors") { //$NON-NLS-1$
			/* (non-javadoc) method declared on ContributionItemFactory */
			public IContributionItem create(IWorkbenchWindow window) {
				if (window == null) {
					throw new IllegalArgumentException();
				}
				return new ReopenEditorMenu(window, getId(), false);
			}
		};
		menu.add(REOPEN_EDITORS.create(window));

		menu.add(exitAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
	}

	/**
	 * 创建编辑菜单
	 * @return 返回编辑菜单的 menu manager;
	 */
	private MenuManager createEditMenu() {
		MenuManager menu = new MenuManager(Messages.getString("ts.ApplicationActionBarAdvisor.menu.edit"),
				IWorkbenchActionConstants.M_EDIT); // &Edit
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
		// menu.add(undoAction);
		// menu.add(redoAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
		menu.add(new Separator());
		menu.add(cutAction);
		menu.add(copyAction);
		menu.add(pasteAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
		menu.add(new Separator());
		menu.add(findAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
		return menu;
	}

	/**
	 * 创建帮助菜单
	 * @return 返回帮助菜单的 menu manager;
	 */
	private MenuManager createHelpMenu() {
		MenuManager menu = new MenuManager(Messages.getString("ts.ApplicationActionBarAdvisor.menu.help"),
				IWorkbenchActionConstants.M_HELP);
		menu.add(helpAction);
		menu.add(new GroupMarker("help.keyAssist"));
		menu.add(new Separator());
		menu.add(new GroupMarker("help.updatePlugin"));
		menu.add(new Separator());
		menu.add(new GroupMarker("help.license"));
		// 关于菜单需要始终显示在最底端
		menu.add(new GroupMarker("group.about"));
		return menu;
	}

	/**
	 * 创建自定义的插件菜单 2012-03-07
	 * @return ;
	 */
	/*
	 * private MenuManager createAutoPluginMenu() { MenuManager menu = new MenuManager("asdfasd",
	 * "net.heartsome.cat.ts.ui.menu.plugin"); // menu = MenuManag
	 * 
	 * // menu.appendToGroup(groupName, item) menu.add(helpSearchAction); return menu; }
	 */

	@Override
	public void dispose() {
		if (isDisposed) {
			return;
		}
		isDisposed = true;
		IMenuService menuService = (IMenuService) window.getService(IMenuService.class);
		menuService.releaseContributions(coolbarPopupMenuManager);
		coolbarPopupMenuManager.dispose();
		super.dispose();
	}

	/**
	 * 移除无用的菜单项：<br/>
	 * File 菜单下的“open file...”和“Convert Line Delimiters To”
	 */
	private void removeUnusedAction() {
		ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
		IActionSetDescriptor[] actionSets = reg.getActionSets();

		List<String> actionSetIds = Arrays.asList("org.eclipse.ui.actionSet.openFiles",
				"org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo",
				"org.eclipse.ui.actions.showKeyAssistHandler");
		for (int i = 0; i < actionSets.length; i++) {
			if (actionSetIds.contains(actionSets[i].getId())) {
				IExtension ext = actionSets[i].getConfigurationElement().getDeclaringExtension();
				reg.removeExtension(ext, new Object[] { actionSets[i] });
			}
		}
	}
}
