/**
 * LineNumberPainter.java
 *
 * Version information :
 *
 * Date:Mar 1, 2012
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.editor.nattable.painter;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.TextPainter;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class LineNumberPainter extends TextPainter {

	
	public LineNumberPainter() {
		super(true,false);
	}
		
	@Override
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return 0;
	}	
	@Override
	public void paintCell(LayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
		super.paintCell(cell, gc, rectangle, configRegistry);
	}
}
