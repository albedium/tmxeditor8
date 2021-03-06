/**
 * TmxContainerFactory.java
 *
 * Version information :
 *
 * Date:2013/5/17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */

package net.heartsome.cat.te.core.tmxdata;

import java.io.File;

import net.heartsome.cat.common.bean.DatabaseModelBean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public final class TmxContainerFactory {
	public static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDataAccess.class);

	public static TmxContainer createContainer(File tmxFile) throws Exception {
		TmxFileContainer container = new TmxFileContainer(tmxFile.getAbsolutePath());
		container.parseFileWithVTD();
		return container;
	}

	public static TmxContainer createLargeFileContainer(File tmxFile, IProgressMonitor monitor) throws Exception {
		TmxLargeFileContainer container = new TmxLargeFileContainer();
		boolean r = container.openFile(tmxFile.getAbsolutePath(), monitor);
		if (!r) {
			return null;
		}
		return container;
	}

	public static TmxContainer createContainer(DatabaseModelBean dbModelBean) throws Exception {
		TmxDbContainer container = new TmxDbContainer(dbModelBean);

		container.loadDatabaseOperator();
		container.connectDatabase();
		return container;

	}
}
