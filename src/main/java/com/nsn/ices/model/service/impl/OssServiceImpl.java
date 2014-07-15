package com.nsn.ices.model.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.nsn.ices.model.dao.OssDao;
import com.nsn.ices.model.entity.Oss;
import com.nsn.ices.model.service.OssService;

@Service(value="ossService")
public class OssServiceImpl implements OssService {

	@Autowired
	private OssDao ossDao;
	
	@Transactional
	public void add(Oss oss) {
		ossDao.add(oss);
	}

	@Transactional
	public void edit(Oss oss) {
		ossDao.edit(oss);
	}

	@Transactional
	public void delete(int ossId) {
		ossDao.delete(ossId);
	}

	@Transactional
	public Oss search(int ossId) {
		return ossDao.search(ossId);
	}
	
	@Cacheable("ossCache")
	@Transactional
	public List<Oss> getAllOss() {
		return ossDao.getAllOss();
	}
}
