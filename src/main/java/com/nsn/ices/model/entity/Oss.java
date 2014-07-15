package com.nsn.ices.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ices_oss")
public class Oss{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private String oss_name;
	private String oss_ip;
	private String oss_port;
	private String oss_username;
	private String oss_password;
	private String oss_url;
	private String oss_instance;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOss_name() {
		return oss_name;
	}
	public void setOss_name(String oss_name) {
		this.oss_name = oss_name;
	}
	public String getOss_ip() {
		return oss_ip;
	}
	public void setOss_ip(String oss_ip) {
		this.oss_ip = oss_ip;
	}
	public String getOss_port() {
		return oss_port;
	}
	public void setOss_port(String oss_port) {
		this.oss_port = oss_port;
	}
	public String getOss_username() {
		return oss_username;
	}
	public void setOss_username(String oss_username) {
		this.oss_username = oss_username;
	}
	public String getOss_password() {
		return oss_password;
	}
	public void setOss_password(String oss_password) {
		this.oss_password = oss_password;
	}
	public String getOss_url() {
		return oss_url;
	}
	public void setOss_url(String oss_url) {
		this.oss_url = oss_url;
	}
	public String getOss_instance() {
		return oss_instance;
	}
	public void setOss_instance(String oss_instance) {
		this.oss_instance = oss_instance;
	}

}
