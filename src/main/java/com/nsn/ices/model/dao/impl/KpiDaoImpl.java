package com.nsn.ices.model.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nsn.ices.model.dao.KpiDao;
import com.nsn.ices.model.entity.Kpi;

@Repository(value="kpiDao")
public class KpiDaoImpl implements KpiDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public void add(Kpi kpi) {
		sessionFactory.getCurrentSession().save(kpi);

	}

	@Override
	public void edit(Kpi kpi) {
		sessionFactory.getCurrentSession().update(kpi);

	}

	@Override
	public void delete(int kpiId) {
		sessionFactory.getCurrentSession().delete(search(kpiId));

	}

	@Override
	public Kpi search(int kpiId) {
		return (Kpi)sessionFactory.getCurrentSession().get(Kpi.class, kpiId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Kpi> getAllKpi() {
		return sessionFactory.getCurrentSession().createQuery("from Kpi").list();
	}
	
	@Override
	public int loadData(String sql){
		return sessionFactory.getCurrentSession().createSQLQuery(sql).executeUpdate();
	}

}
