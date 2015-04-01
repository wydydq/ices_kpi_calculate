package com.nsn.ices.model.entity;

public class Area {
	private String AreaCode;
	private String AreaName;
	private String ParentCode;
	private String AreaLevel;
	private String FullName;
	private String FullPath;
	private String IsLeaf;
	
	public String getAreaCode() {
		return AreaCode;
	}
	public void setAreaCode(String areaCode) {
		AreaCode = areaCode;
	}
	public String getAreaName() {
		return AreaName;
	}
	public void setAreaName(String areaName) {
		AreaName = areaName;
	}
	public String getParentCode() {
		return ParentCode;
	}
	public void setParentCode(String parentCode) {
		ParentCode = parentCode;
	}
	public String getAreaLevel() {
		return AreaLevel;
	}
	public void setAreaLevel(String areaLevel) {
		AreaLevel = areaLevel;
	}
	public String getFullName() {
		return FullName;
	}
	public void setFullName(String fullName) {
		FullName = fullName;
	}
	public String getFullPath() {
		return FullPath;
	}
	public void setFullPath(String fullPath) {
		FullPath = fullPath;
	}
	public String getIsLeaf() {
		return IsLeaf;
	}
	public void setIsLeaf(String isLeaf) {
		IsLeaf = isLeaf;
	}

}
