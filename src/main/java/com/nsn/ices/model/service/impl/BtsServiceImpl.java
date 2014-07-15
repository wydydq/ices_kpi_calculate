package com.nsn.ices.model.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.nsn.ices.model.dao.BtsDao;
import com.nsn.ices.model.entity.Bts;
import com.nsn.ices.model.service.BtsService;

@Service(value="btsService")
public class BtsServiceImpl implements BtsService {

	@Autowired
	private BtsDao btsDao;
	
	@Transactional
	public void add(Bts bts) {
		btsDao.add(bts);
	}

	@Transactional
	public void edit(Bts bts) {
		btsDao.edit(bts);
	}

	@Transactional
	public void delete(int btsId) {
		btsDao.delete(btsId);
	}

	@Transactional
	public Bts search(int btsId) {
		return btsDao.search(btsId);
	}

	@Cacheable("btsCache")
	@Transactional
	public List<Bts> getAllBts() {
		return btsDao.getAllBts();
	}
}
