package com.wat.melody.api.report;

import java.util.Date;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITaskReportItem {

	public String getReportItemMessage();

	public Date getReportItemDate();

	public TaskReportItemType getReportItemType();

}