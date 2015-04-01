package com.nsn.ices.model.entity;

public class KpiResultDetail {

	private String period;
	private int kpi_id;
	private int cel_obj_gid;
	private String result;
	public String getPeriod() {
		return period;
	}
	
	public void setPeriod(String period) {
		this.period = period;
	}
	public int getKpi_id() {
		return kpi_id;
	}
	public void setKpi_id(int kpi_id) {
		this.kpi_id = kpi_id;
	}
	public int getCel_obj_gid() {
		return cel_obj_gid;
	}
	public void setCel_obj_gid(int cel_obj_gid) {
		this.cel_obj_gid = cel_obj_gid;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	
	
}
