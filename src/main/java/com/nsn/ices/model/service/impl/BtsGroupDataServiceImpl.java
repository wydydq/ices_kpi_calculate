package com.nsn.ices.model.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nsn.ices.model.dao.BtsGroupDataDao;
import com.nsn.ices.model.entity.BtsGroupData;
import com.nsn.ices.model.service.BtsGroupDataService;

@Service(value="btsGroupDataService")
public class BtsGroupDataServiceImpl implements BtsGroupDataService {

	@Autowired
	private BtsGroupDataDao btsGroupDataDao;
	
	@Transactional
	public void add(BtsGroupData btsGroupData) {
		btsGroupDataDao.add(btsGroupData);
	}

	@Transactional
	public void edit(BtsGroupData btsGroupData) {
		btsGroupDataDao.edit(btsGroupData);
	}

	@Transactional
	public void delete(int btsGroupDataId) {
		btsGroupDataDao.delete(btsGroupDataId);
	}

	@Transactional
	public BtsGroupData search(int btsGroupDataId) {
		return btsGroupDataDao.search(btsGroupDataId);
	}

	@Transactional
	public List<BtsGroupData> getAllBtsGroupData() {
		return btsGroupDataDao.getAllBtsGroupData();
	}
}
