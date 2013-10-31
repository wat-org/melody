package com.wat.melody.core.internal.report;

import java.util.Date;

import com.wat.melody.api.report.ITaskReportItem;
import com.wat.melody.api.report.TaskReportItemType;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TaskReportItem implements ITaskReportItem {

	public TaskReportItem(Date date, TaskReportItemType reportItemType,
			String message) {

	}

	@Override
	public String getReportItemMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getReportItemDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TaskReportItemType getReportItemType() {
		// TODO Auto-generated method stub
		return null;
	}

}