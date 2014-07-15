package com.nsn.ices.model.entity;

import java.util.Date;


public class BtsState {
	
	private int ne_code;
	private int lncel_eutra_cel_id;
	private int cell_id;
	private int cell_obj_gid;
	private int cell_status;
	private int cell_angle;
	private int bts_obj_gid;
	private int bts_status;
	private int trans_state;
	private int cell_rru;
	private String bts_version;
	private String bts_name;
	private int bts_altitude;
	private int bts_longitude;
	private int bts_latitude;
	private int cell_load_act_ue_avg;
	private Date cdatetime;
	
	public int getNe_code() {
		return ne_code;
	}
	public void setNe_code(int ne_code) {
		this.ne_code = ne_code;
	}
	public int getLncel_eutra_cel_id() {
		return lncel_eutra_cel_id;
	}
	public void setLncel_eutra_cel_id(int lncel_eutra_cel_id) {
		this.lncel_eutra_cel_id = lncel_eutra_cel_id;
	}
	public int getCell_id() {
		return cell_id;
	}
	public void setCell_id(int cell_id) {
		this.cell_id = cell_id;
	}
	public int getCell_obj_gid() {
		return cell_obj_gid;
	}
	public void setCell_obj_gid(int cell_obj_gid) {
		this.cell_obj_gid = cell_obj_gid;
	}
	public int getCell_status() {
		return cell_status;
	}
	public void setCell_status(int cell_status) {
		this.cell_status = cell_status;
	}
	public int getCell_angle() {
		return cell_angle;
	}
	public void setCell_angle(int cell_angle) {
		this.cell_angle = cell_angle;
	}
	public int getBts_obj_gid() {
		return bts_obj_gid;
	}
	public void setBts_obj_gid(int bts_obj_gid) {
		this.bts_obj_gid = bts_obj_gid;
	}
	public int getBts_status() {
		return bts_status;
	}
	public void setBts_status(int bts_status) {
		this.bts_status = bts_status;
	}
	public int getTrans_state() {
		return trans_state;
	}
	public void setTrans_state(int trans_state) {
		this.trans_state = trans_state;
	}
	public int getCell_rru() {
		return cell_rru;
	}
	public void setCell_rru(int cell_rru) {
		this.cell_rru = cell_rru;
	}
	public String getBts_version() {
		return bts_version;
	}
	public void setBts_version(String bts_version) {
		this.bts_version = bts_version;
	}
	public String getBts_name() {
		return bts_name;
	}
	public void setBts_name(String bts_name) {
		this.bts_name = bts_name;
	}
	public int getBts_altitude() {
		return bts_altitude;
	}
	public void setBts_altitude(int bts_altitude) {
		this.bts_altitude = bts_altitude;
	}
	public int getBts_longitude() {
		return bts_longitude;
	}
	public void setBts_longitude(int bts_longitude) {
		this.bts_longitude = bts_longitude;
	}
	public int getBts_latitude() {
		return bts_latitude;
	}
	public void setBts_latitude(int bts_latitude) {
		this.bts_latitude = bts_latitude;
	}
	public int getCell_load_act_ue_avg() {
		return cell_load_act_ue_avg;
	}
	public void setCell_load_act_ue_avg(int cell_load_act_ue_avg) {
		this.cell_load_act_ue_avg = cell_load_act_ue_avg;
	}
	public Date getCdatetime() {
		return cdatetime;
	}
	public void setCdatetime(Date cdatetime) {
		this.cdatetime = cdatetime;
	}
}
