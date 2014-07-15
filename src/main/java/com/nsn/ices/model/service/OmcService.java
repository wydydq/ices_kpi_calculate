package com.nsn.ices.model.service;

public interface OmcService {

	void syncing(int omc_id, int kpi_id, String period);
	String getCycle(int omc_id);
}
