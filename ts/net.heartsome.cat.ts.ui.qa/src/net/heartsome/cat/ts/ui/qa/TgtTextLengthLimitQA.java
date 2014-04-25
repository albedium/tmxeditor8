package net.heartsome.cat.ts.ui.qa;

import java.text.MessageFormat;
import java.util.Map;

import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.model.QAResultBean;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 目标文本段长度限制检查
 * 备注：使用的是纯文本
 * @author  robert
 * @version 
 * @since   JDK1.6
 */
public class TgtTextLengthLimitQA extends QARealization {
	private int tipLevel;
	private IPreferenceStore preferenceStore;
	private boolean hasError;
	private boolean isCheckTgtMinLength = false;
	private boolean isCheckTgtMaxLength = false;
	/** 目标文本段长度允许减少的比例 */
	private float minPercentage;
	/** 目标文本段长度允许增加的比例 */
	private float maxPercentage;
	
	public TgtTextLengthLimitQA(){
		preferenceStore = Activator.getDefault().getPreferenceStore();
		tipLevel = preferenceStore.getInt(QAConstant.QA_PREF_tgtLengthLimit_TIPLEVEL);
		
		isCheckTgtMinLength = preferenceStore.getBoolean(QAConstant.QA_PREF_isCheckTgtMinLength);
		isCheckTgtMaxLength = preferenceStore.getBoolean(QAConstant.QA_PREF_isCheckTgtMaxLength);
		
		if (isCheckTgtMinLength) {
			String minValue = preferenceStore.getString(QAConstant.QA_PREF_tgtMinLength);
			if (minValue == null || "".equals(minValue)) {
				minValue = "0";
			}
			minPercentage = Float.parseFloat(minValue) / 100;
		}
		
		if (isCheckTgtMaxLength) {
			String maxValue = preferenceStore.getString(QAConstant.QA_PREF_tgtMaxLength);
			if (maxValue == null || "".equals(maxValue)) {
				maxValue = "0";
			}
			maxPercentage = Float.parseFloat(maxValue) / 100;
		}
	}
	
	@Override
	void setParentQaResult(QAResult qaResult) {
		super.setQaResult(qaResult);
	}

	@Override
	public String startQA(QAModel model, IProgressMonitor monitor, IFile iFile, QAXmlHandler xmlHandler,
			Map<String, String> tuMap) {
		hasError = false;
		
		String langPair = tuMap.get("langPair");
		String lineNumber = tuMap.get("lineNumber");
		String iFileFullPath = tuMap.get("iFileFullPath");
		String qaType = Messages.getString("qa.all.qaItem.TgtTextLengthLimitQA");
		String rowId = tuMap.get("rowId");
		String tarPureText = tuMap.get("tarPureText");
		int srcTextLength = tuMap.get("srcPureText").length();
		int tgtTextLength = tarPureText.length();
		
		if (isCheckTgtMaxLength) {
			int maxWidth = (int) (srcTextLength + srcTextLength * maxPercentage);
			if (tgtTextLength > maxWidth) {
				String errorTip = MessageFormat.format(Messages.getString("qa.TgtTextLengthLimitQA.tip1"),
						new Object[] { tgtTextLength, maxWidth });
				hasError = true;
				super.printQAResult(new QAResultBean(lineNumber, qaType, errorTip, iFileFullPath, langPair, rowId, tipLevel, QAConstant.QA_TGTTEXTLENGTHLIMIT));
			}
		}
		
		if (isCheckTgtMinLength) {
			int minWidth = (int) (srcTextLength - srcTextLength * minPercentage);
			minWidth = minWidth >= 0 ? minWidth : 0;
			if (tgtTextLength < minWidth) {
				String errorTip = MessageFormat.format(Messages.getString("qa.TgtTextLengthLimitQA.tip2"),
						new Object[] { tgtTextLength, minWidth });
				hasError = true;
				super.printQAResult(new QAResultBean(lineNumber, qaType, errorTip, iFileFullPath, langPair, rowId, tipLevel, QAConstant.QA_TGTTEXTLENGTHLIMIT));
			}
		}
		
		String result = "";
		if (hasError && tipLevel == 0) {
			result = QAConstant.QA_TGTTEXTLENGTHLIMIT;
		}
		return result;
	}
	
}
