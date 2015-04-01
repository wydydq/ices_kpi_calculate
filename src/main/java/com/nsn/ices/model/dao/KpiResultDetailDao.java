package com.nsn.ices.model.dao;

import java.util.List;

import com.nsn.ices.model.entity.KpiBusyTime;
import com.nsn.ices.model.entity.StartCycleWithEnd;

public interface KpiResultDetailDao {
	List<Object[]> getKpiResultDetail(String period);
	List<Object[]> getKpiResultDetail(String period, int granularity);
	List<KpiBusyTime> getKpiBusyTime(String period);
	KpiBusyTime getKpiBusyTimeDefault();
	List<Object[]> getKpiResultDetailCityWeekly(String period, List<StartCycleWithEnd> periods);
}
