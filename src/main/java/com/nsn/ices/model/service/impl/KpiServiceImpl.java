package com.nsn.ices.model.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.nsn.ices.model.dao.KpiDao;
import com.nsn.ices.model.entity.Kpi;
import com.nsn.ices.model.service.KpiService;

@Service(value="kpiService")
public class KpiServiceImpl implements KpiService {

	@Autowired
	private KpiDao kpiDao;
	
	@Transactional
	public void add(Kpi kpi) {
		kpiDao.add(kpi);
	}

	@Transactional
	public void edit(Kpi kpi) {
		kpiDao.edit(kpi);
	}

	@Transactional
	public void delete(int kpiId) {
		kpiDao.delete(kpiId);
	}

	@Transactional
	public Kpi search(int kpiId) {
		return kpiDao.search(kpiId);
	}
	
	@Cacheable("kpiCache")
	@Transactional
	public List<Kpi> getAllKpi() {
		return kpiDao.getAllKpi();
	}
}
