package org.opensrp.web.dto;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class TaskSearchCriteria {

	@NotBlank
	private String planIdentifier;

	private List<String> groupIdentifiers;

	private String code;

	private String status;

	private String businessStatus;

	private boolean returnTaskCountOnly;

	private Integer pageNumber;

	private Integer pageSize;

	private String orderByType;

	private String orderByFieldName;

	public String getPlanIdentifier() {
		return planIdentifier;
	}

	public void setPlanIdentifier(String planIdentifier) {
		this.planIdentifier = planIdentifier;
	}

	public List<String> getGroupIdentifiers() {
		return groupIdentifiers;
	}

	public void setGroupIdentifiers(List<String> groupIdentifiers) {
		this.groupIdentifiers = groupIdentifiers;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBusinessStatus() {
		return businessStatus;
	}

	public void setBusinessStatus(String businessStatus) {
		this.businessStatus = businessStatus;
	}

	public boolean isReturnTaskCountOnly() {
		return returnTaskCountOnly;
	}

	public void setReturnTaskCountOnly(boolean returnTaskCountOnly) {
		this.returnTaskCountOnly = returnTaskCountOnly;
	}

	public Integer getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public String getOrderByType() {
		return orderByType;
	}

	public void setOrderByType(String orderByType) {
		this.orderByType = orderByType;
	}

	public String getOrderByFieldName() {
		return orderByFieldName;
	}

	public void setOrderByFieldName(String orderByFieldName) {
		this.orderByFieldName = orderByFieldName;
	}
}
