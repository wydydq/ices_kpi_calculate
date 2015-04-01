package com.nsn.ices.model.entity;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ices_kpi_busy_time")
public class KpiBusyTime{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private String start_date;//开始日期
	private String end_date;//结束日期
	private String busy_time;//忙时
	private String description;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getStart_date() {
		return start_date;
	}
	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}
	public String getEnd_date() {
		return end_date;
	}
	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}
	public String getBusy_time() {
		return busy_time;
	}
	public void setBusy_time(String busy_time) {
		this.busy_time = busy_time;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
