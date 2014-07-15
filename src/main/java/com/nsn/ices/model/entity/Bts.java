package com.nsn.ices.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ices_bts")
public class Bts {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private String ne_name_cn;
	private String ne_name_en;
	private int ne_code;
	private int region_id;
	private String ip;
	private String username;
	private String PASSWORD;
	private String MODE;
	private String ne_type;
	private int station_type;
	private String supporter;
	private int oss_id;
	private int is_active;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNe_name_cn() {
		return ne_name_cn;
	}
	public void setNe_name_cn(String ne_name_cn) {
		this.ne_name_cn = ne_name_cn;
	}
	public String getNe_name_en() {
		return ne_name_en;
	}
	public void setNe_name_en(String ne_name_en) {
		this.ne_name_en = ne_name_en;
	}
	public int getNe_code() {
		return ne_code;
	}
	public void setNe_code(int ne_code) {
		this.ne_code = ne_code;
	}
	public int getRegion_id() {
		return region_id;
	}
	public void setRegion_id(int region_id) {
		this.region_id = region_id;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPASSWORD() {
		return PASSWORD;
	}
	public void setPASSWORD(String pASSWORD) {
		PASSWORD = pASSWORD;
	}
	public String getMODE() {
		return MODE;
	}
	public void setMODE(String mODE) {
		MODE = mODE;
	}
	public String getNe_type() {
		return ne_type;
	}
	public void setNe_type(String ne_type) {
		this.ne_type = ne_type;
	}
	public int getStation_type() {
		return station_type;
	}
	public void setStation_type(int station_type) {
		this.station_type = station_type;
	}
	public String getSupporter() {
		return supporter;
	}
	public void setSupporter(String supporter) {
		this.supporter = supporter;
	}
	public int getOss_id() {
		return oss_id;
	}
	public void setOss_id(int oss_id) {
		this.oss_id = oss_id;
	}
	public int getIs_active() {
		return is_active;
	}
	public void setIs_active(int is_active) {
		this.is_active = is_active;
	}

}
