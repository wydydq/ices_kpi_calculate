package com.nsn.ices.model.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nsn.ices.model.dao.BtsDao;
import com.nsn.ices.model.entity.Area;
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
	
	@SuppressWarnings("unchecked")
	public List<Area> getArea(){
		String sql = "select AreaCode,AreaName,ParentCode,AreaLevel from des_area where exists (select region_id FROM ices_bts WHERE areacode = region_id)";
		List<Object[]> list = sessionFactory.getCurrentSession().createSQLQuery(sql).list();
		List<Area> areas = new ArrayList<Area>();
		for(Object[] arr : list){
			Area area = new Area();
			area.setAreaCode(arr[0].toString());
			area.setAreaName(arr[1].toString());
			area.setParentCode(arr[2].toString());
			area.setAreaLevel(arr[3].toString());
			areas.add(area);
		}
		return areas;
	}
}
