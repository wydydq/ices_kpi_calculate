package com.nsn.ices.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ices_bts_group_data")
public class BtsGroupData {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private int group_id;
	private int ne_code;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getGroup_id() {
		return group_id;
	}
	public void setGroup_id(int group_id) {
		this.group_id = group_id;
	}
	public int getNe_code() {
		return ne_code;
	}
	public void setNe_code(int ne_code) {
		this.ne_code = ne_code;
	}
	
}
