package com.nsn.ices.model.service;

import java.util.List;

import com.nsn.ices.model.entity.Kpi;

public interface KpiService {
	void add(Kpi kpi);
	void edit(Kpi kpi);
	void delete(int kpiId);
	Kpi search(int kpiId);
	List<Kpi> getAllKpi();
}
