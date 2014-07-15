package com.nsn.ices.model.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.nsn.ices.model.dao.BtsStateDao;
import com.nsn.ices.model.entity.BtsState;
import com.nsn.ices.model.service.BtsStateService;

@Service(value="btsStateService")
public class BtsStateServiceImpl implements BtsStateService {

	@Autowired
	private BtsStateDao btsStateDao;
	
	@Transactional
	public void add(BtsState btsState) {
		btsStateDao.add(btsState);
	}

	@Transactional
	public void edit(BtsState btsState) {
		btsStateDao.edit(btsState);
	}

	@Transactional
	public void delete(int btsStateId) {
		btsStateDao.delete(btsStateId);
	}

	@Transactional
	public BtsState search(int btsStateId) {
		return btsStateDao.search(btsStateId);
	}

	@Cacheable("btsStateCache")
	@Transactional
	public List<BtsState> getAllBtsState() {
		return btsStateDao.getAllBtsState();
	}
}
