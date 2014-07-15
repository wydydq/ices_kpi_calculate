package com.nsn.ices.model.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nsn.ices.model.dao.OssDao;
import com.nsn.ices.model.entity.Oss;

@Repository(value="ossDao")
public class OssDaoImpl implements OssDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public void add(Oss oss) {
		sessionFactory.getCurrentSession().save(oss);

	}

	@Override
	public void edit(Oss oss) {
		sessionFactory.getCurrentSession().update(oss);

	}

	@Override
	public void delete(int ossId) {
		sessionFactory.getCurrentSession().delete(search(ossId));

	}

	@Override
	public Oss search(int ossId) {
		return (Oss)sessionFactory.getCurrentSession().get(Oss.class, ossId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Oss> getAllOss() {
		return sessionFactory.getCurrentSession().createQuery("from Oss").list();
	}

}
