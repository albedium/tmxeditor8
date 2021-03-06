/**
 * NewDatabaseWizardImportPage.java
 *
 * Version information :
 *
 * Date:Oct 28, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.ui.tm.wizard;

import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.database.SystemDBOperator;
import net.heartsome.cat.database.bean.TMPreferenceConstants;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.tm.Activator;
import net.heartsome.cat.database.ui.tm.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 导入TMX文件向导页
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class NewTmDbImportPage extends WizardPage {
	Logger logger = LoggerFactory.getLogger(NewTmDbImportPage.class);
	private Text tmxFileText;

	private final NewTmDbBaseInfoPage createDbPage;

	/**
	 * Create the wizard.
	 */
	public NewTmDbImportPage(NewTmDbBaseInfoPage createDbPage) {
		super("importWizardPage");
		this.createDbPage = createDbPage;
		setTitle(Messages.getString("wizard.NewTmDbImportPage.title"));
		setDescription(Messages.getString("wizard.NewTmDbImportPage.desc"));
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(3, false));

		Label lblTmx = new Label(container, SWT.NONE);
		lblTmx.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTmx.setText(Messages.getString("wizard.NewTmDbImportPage.lblTmx"));

		tmxFileText = new Text(container, SWT.BORDER);
		tmxFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tmxFileText.setEditable(false);

		Button tmxFileBorwserBtn = new Button(container, SWT.NONE);
		tmxFileBorwserBtn.setText(Messages.getString("wizard.NewTmDbImportPage.tmxFileBorwserBtn"));
		tmxFileBorwserBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(getShell());
				String[] filterExt = { "*.tmx" };
				dlg.setFilterExtensions(filterExt);
				String path = dlg.open();
				if (path != null) {
					tmxFileText.setText(path);
				}
			}
		});
	}

	/**
	 * 创建数据库,并导入文件
	 * @param tmxFile
	 *            TMX文件
	 * @param tbxFile
	 *            TBX文件
	 * @param dbOp
	 * @param monitor
	 * @throws InterruptedException
	 *             ;
	 */
	public void performFinish(String tmxFile, SystemDBOperator dbOp, IProgressMonitor monitor)
			throws InterruptedException {
		monitor.beginTask(Messages.getString("wizard.NewTmDbImportPage.task1"), 300);
		String message = createDbPage.canCreateDb(new SubProgressMonitor(monitor, 80));
		if (message != null) {
			final String _message = message;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setErrorMessage(_message);
				}
			});
			throw new InterruptedException();
		}

		message = createDbPage.executeCreateDB(dbOp, new SubProgressMonitor(monitor, 80));
		if (message != null) {
			final String _message = message;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setErrorMessage(_message);
				}
			});
			throw new InterruptedException();
		}
		if (tmxFile != null) {
			executeImport(tmxFile, dbOp.getMetaData(), new SubProgressMonitor(monitor, 140));
		}
		monitor.done();
	}

	/**
	 * 执行导入
	 * @param tmxFile
	 * @param tbxFile
	 * @param dbMetaData
	 * @param monitor
	 *            ;
	 */
	public void executeImport(String tmxFile, MetaData dbMetaData, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.setTaskName(Messages.getString("wizard.NewTmDbImportPage.task2"));
		monitor.beginTask("", 100);
		int tmxResult = -10;
		String message = "";
		if (tmxFile != null) {
			try {
				tmxResult = DatabaseService.importTmxWithFile(dbMetaData, tmxFile,
						new SubProgressMonitor(monitor, 100), getTmxImportStrategy(), isNeedCheckContext());
			} catch (ImportException e) {
				message = e.getMessage();
			}
			if (!message.equals("")) {
				final String _message = message;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setErrorMessage(_message);
					}
				});
			}
		}

		StringBuffer resultMessage = new StringBuffer();
		if (tmxResult != DatabaseService.SUCCESS) {
			if (tmxResult == DatabaseService.FAILURE_1) {
				resultMessage.append(Messages.getString("wizard.NewTmDbImportPage.msg1"));
			} else if (tmxResult == DatabaseService.FAILURE_2) {
				resultMessage.append(Messages.getString("wizard.NewTmDbImportPage.msg2"));
			} else if (tmxResult == DatabaseService.FAILURE_3) {
				resultMessage.append(Messages.getString("wizard.NewTmDbImportPage.msg3"));
			} else if (tmxResult == DatabaseService.FAILURE_4) {
				resultMessage.append(Messages.getString("wizard.NewTmDbImportPage.msg4"));
			} else if (tmxResult == DatabaseService.FAILURE) {
				resultMessage.append(Messages.getString("wizard.NewTmDbImportPage.msg5"));
			} else if (tmxResult == DatabaseService.CANCEL) {
				resultMessage.append(Messages.getString("wizard.NewTmDbImportPage.msg6"));
			}

			if (!resultMessage.toString().equals("")) {
				final String _message = resultMessage.toString();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setErrorMessage(_message);
					}
				});
			}
		}
		monitor.done();
	}

	public String getTMXFile() {
		String file = tmxFileText.getText().trim();
		if (file == null || file.length() == 0) {
			return null;
		}
		return file;
	}

	/**
	 * 从首选项中读取TMX导入策略
	 * @return ;
	 */
	public int getTmxImportStrategy() {
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		return ps.getInt(TMPreferenceConstants.TM_UPDATE);
	}

	/**
	 * 判断在导入的时候是否需要检查上下文
	 * @return ;
	 */
	public boolean isNeedCheckContext() {
		// TODO 是否需要检查上下文
		return false;
	}
}
