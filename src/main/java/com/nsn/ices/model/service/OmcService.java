package com.nsn.ices.model.service;

import java.util.List;

import com.nsn.ices.model.entity.StartCycleWithEnd;

public interface OmcService {

	void syncing(int omc_id, int kpi_id, StartCycleWithEnd periods, int rate);
	void syncingByCity(int omc_id, int kpi_id, String PERIOD_START_TIME, List<StartCycleWithEnd> periods, int rate);
	StartCycleWithEnd getCycle(int omc_id, int rate);
	List<StartCycleWithEnd> getCycleByCity(int omc_id, int rate);
	String getPeriodByCity(int omc_id);
}
