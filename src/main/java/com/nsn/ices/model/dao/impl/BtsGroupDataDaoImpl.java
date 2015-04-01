package com.nsn.ices.model.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nsn.ices.model.dao.BtsGroupDataDao;
import com.nsn.ices.model.entity.BtsGroupData;

@Repository(value="btsGroupDataDao")
public class BtsGroupDataDaoImpl implements BtsGroupDataDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public void add(BtsGroupData btsGroupData) {
		sessionFactory.getCurrentSession().save(btsGroupData);

	}

	@Override
	public void edit(BtsGroupData btsGroupData) {
		sessionFactory.getCurrentSession().update(btsGroupData);

	}

	@Override
	public void delete(int btsGroupDataId) {
		sessionFactory.getCurrentSession().delete(search(btsGroupDataId));

	}

	@Override
	public BtsGroupData search(int btsGroupDataId) {
		return (BtsGroupData)sessionFactory.getCurrentSession().get(BtsGroupData.class, btsGroupDataId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BtsGroupData> getAllBtsGroupData() {
		return sessionFactory.getCurrentSession().createQuery("from BtsGroupData").list();
	}
}
