package com.nsn.ices.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ices_kpi")
public class Kpi{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private String kpi_name_en;//指标英文名称
	private String kpi_name_cn;//指标中文名称
	private String formula;//计算公式
	private String oss_table_name;//数据来源表(OMC库)
	private int has_threshold;//是否有阈值(0:有,1:没有)
	private String relation;//关系运算符(<,<=,>,>=,==,<>)
	private double threshold;//阈值
	private String relation1;//附加阈值分子关系运算符(<,<=,>,>=,==,<>)
	private double threshold1;//附加阈值分子阈值
	private String relation2;//附加阈值分母关系运算符(<,<=,>,>=,==,<>)
	private double threshold2;//附加阈值分母阈值
	private int continuous_cycle;//最小连续周期数
	private int is_notice;//是否需要通知(0:需要,1不需要)
	private int cycle; //周期,单位分钟(默认15)
	private int type;
	private String description;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getKpi_name_en() {
		return kpi_name_en;
	}
	public void setKpi_name_en(String kpi_name_en) {
		this.kpi_name_en = kpi_name_en;
	}
	public String getKpi_name_cn() {
		return kpi_name_cn;
	}
	public void setKpi_name_cn(String kpi_name_cn) {
		this.kpi_name_cn = kpi_name_cn;
	}
	public String getFormula() {
		return formula;
	}
	public void setFormula(String formula) {
		this.formula = formula;
	}
	public String getOss_table_name() {
		return oss_table_name;
	}
	public void setOss_table_name(String oss_table_name) {
		this.oss_table_name = oss_table_name;
	}
	public int getHas_threshold() {
		return has_threshold;
	}
	public void setHas_threshold(int has_threshold) {
		this.has_threshold = has_threshold;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public double getThreshold() {
		return threshold;
	}
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	public String getRelation1() {
		return relation1;
	}
	public void setRelation1(String relation1) {
		this.relation1 = relation1;
	}
	public double getThreshold1() {
		return threshold1;
	}
	public void setThreshold1(double threshold1) {
		this.threshold1 = threshold1;
	}
	public String getRelation2() {
		return relation2;
	}
	public void setRelation2(String relation2) {
		this.relation2 = relation2;
	}
	public double getThreshold2() {
		return threshold2;
	}
	public void setThreshold2(double threshold2) {
		this.threshold2 = threshold2;
	}
	public int getContinuous_cycle() {
		return continuous_cycle;
	}
	public void setContinuous_cycle(int continuous_cycle) {
		this.continuous_cycle = continuous_cycle;
	}
	public int getIs_notice() {
		return is_notice;
	}
	public void setIs_notice(int is_notice) {
		this.is_notice = is_notice;
	}
	public int getCycle() {
		return cycle;
	}
	public void setCycle(int cycle) {
		this.cycle = cycle;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
