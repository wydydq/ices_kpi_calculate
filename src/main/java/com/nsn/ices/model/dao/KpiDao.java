package com.nsn.ices.model.dao;

import java.util.List;

import com.nsn.ices.model.entity.Kpi;

public interface KpiDao {
	void add(Kpi kpi);
	void edit(Kpi kpi);
	void delete(int kpiId);
	Kpi search(int kpiId);
	List<Kpi> getAllKpi();
	int loadData(String sql);
}
