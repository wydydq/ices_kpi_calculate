/*
 * Copyright (c) 2014, NSN and/or its affiliates. All rights reserved.
 * NSN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 */
package com.nsn.ices.core;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.nsn.ices.model.entity.Bts;
import com.nsn.ices.model.entity.Cell;
import com.nsn.ices.model.entity.Kpi;
import com.nsn.ices.model.entity.KpiResult;
import com.nsn.ices.model.entity.Oss;

/**
 * A class include spring context initial,which is used to start the project
 * @author Mars Yu
 *
 */
public class Application {
	private ApplicationContext springContext;
	private static Application instance = new Application();
	private SessionFactory sessionFactory;
	private Date startTime = new Date();
	public static Map<Integer, Bts> btsMap = new HashMap<Integer, Bts>();
	public static Map<Integer, Map<Integer, Cell>> btsCellMap = new HashMap<Integer, Map<Integer, Cell>>();
	public static Map<Integer, Cell> cellMap = new HashMap<Integer, Cell>();
	public static Map<Integer, Oss> ossMap = new HashMap<Integer, Oss>();
	public static Map<Integer, Kpi> kpiMap = new HashMap<Integer, Kpi>();
	public static Map<String, Kpi> kpiNameMap = new HashMap<String, Kpi>();
	public static Map<Integer, List<Bts>> ossBtsMap = new HashMap<Integer, List<Bts>>();
	public static Map<Integer, Map<Integer, Cell>> ossCellMap = new HashMap<Integer, Map<Integer, Cell>>();
	public static Map<Integer, KpiResult> kpiResultMap = new HashMap<Integer, KpiResult>();
	public static int count = 0;
	private Application(){
		initSpring();
		initLog4J();
	}
	private void initSpring(){
		springContext = new ClassPathXmlApplicationContext(new String[]{
			"classpath:servlet-context.xml"
		});
	}
	private void initLog4J(){
		PropertyConfigurator.configure("log4j.properties");
	}
	public static Application getInstance(){
		return instance;
	}
	public Object getBean(String name){
		Object returnObject = springContext.getBean(name);
		return returnObject;
	}

	public SessionFactory getSessionFactory(){
		if (sessionFactory == null){
			sessionFactory = (SessionFactory) getBean("sessionFactory");
		}
		return sessionFactory;
	}
	
	public SessionFactory getSessionFactoryByName(String name){
		return (SessionFactory) getBean(name);
	}
	
	public Date getStartTime(){
		return startTime;
	}
	public static void main(String[] args) {
		Application.getInstance();
	}

}
