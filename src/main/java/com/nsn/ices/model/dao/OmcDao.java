package com.nsn.ices.model.dao;

import java.util.List;

import com.nsn.ices.model.entity.StartCycleWithEnd;


public interface OmcDao {

	List<Object[]> find(int omc_id, int kpi_id, StartCycleWithEnd periods);
	
	List<Object[]> findByCity(int omc_id, int kpi_id,String PERIOD_START_TIME, List<StartCycleWithEnd> periods);
	
	String sysdate(int omc_id);
}
