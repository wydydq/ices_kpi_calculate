package com.nsn.ices.model.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nsn.ices.model.dao.BtsStateDao;
import com.nsn.ices.model.entity.BtsState;

@Repository(value="btsStateDao")
public class BtsStateDaoImpl implements BtsStateDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public void add(BtsState btsState) {
		sessionFactory.getCurrentSession().save(btsState);

	}

	@Override
	public void edit(BtsState btsState) {
		sessionFactory.getCurrentSession().update(btsState);

	}

	@Override
	public void delete(int btsStateId) {
		sessionFactory.getCurrentSession().delete(search(btsStateId));

	}

	@Override
	public BtsState search(int btsStateId) {
		return (BtsState)sessionFactory.getCurrentSession().get(BtsState.class, btsStateId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BtsState> getAllBtsState() {
		return sessionFactory.getCurrentSession().createQuery("from BtsState").list();
	}

}
