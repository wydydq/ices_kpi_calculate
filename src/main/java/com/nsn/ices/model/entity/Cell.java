package com.nsn.ices.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ices_cell")
public class Cell {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private String cell_name_cn;
	private String cell_name_en;
	private int ne_code;
	private int cell_id;
	private int cell_obj_gid;
	private int sector_id;
	private int region_id;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCell_name_cn() {
		return cell_name_cn;
	}
	public void setCell_name_cn(String cell_name_cn) {
		this.cell_name_cn = cell_name_cn;
	}
	public String getCell_name_en() {
		return cell_name_en;
	}
	public void setCell_name_en(String cell_name_en) {
		this.cell_name_en = cell_name_en;
	}
	public int getNe_code() {
		return ne_code;
	}
	public void setNe_code(int ne_code) {
		this.ne_code = ne_code;
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
	public int getSector_id() {
		return sector_id;
	}
	public void setSector_id(int sector_id) {
		this.sector_id = sector_id;
	}
	public int getRegion_id() {
		return region_id;
	}
	public void setRegion_id(int region_id) {
		this.region_id = region_id;
	}
}
