package com.nsn.ices.model.service;


public interface KpiResultDetailService {
	void syncingByArea();
	void syncingByGroup(String period, int granularity);
	void syncingByCityWeekly();
}
