package com.nsn.ices.model.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nsn.ices.model.dao.BtsDao;
import com.nsn.ices.model.entity.Bts;

@Repository(value="btsDao")
public class BtsDaoImpl implements BtsDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public void add(Bts bts) {
		sessionFactory.getCurrentSession().save(bts);

	}

	@Override
	public void edit(Bts bts) {
		sessionFactory.getCurrentSession().update(bts);

	}

	@Override
	public void delete(int btsId) {
		sessionFactory.getCurrentSession().delete(search(btsId));

	}

	@Override
	public Bts search(int btsId) {
		return (Bts)sessionFactory.getCurrentSession().get(Bts.class, btsId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Bts> getAllBts() {
		return sessionFactory.getCurrentSession().createQuery("from Bts where is_active=0").list();
	}

}
