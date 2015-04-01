package com.nsn.ices.model.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.nsn.ices.core.Application;
import com.nsn.ices.model.dao.KpiDao;
import com.nsn.ices.model.dao.KpiResultDetailDao;
import com.nsn.ices.model.dao.OmcDao;
import com.nsn.ices.model.entity.KpiResult;
import com.nsn.ices.model.entity.StartCycleWithEnd;
import com.nsn.ices.model.service.KpiResultDetailService;
import com.nsn.ices.model.service.OmcService;

@Service(value="kpiResultDetailService")
public class KpiResultDetailServiceImpl implements KpiResultDetailService {

	@Autowired
	private OmcService omcService;
	
	@Autowired
	private OmcDao omcDao;
	
	@Autowired
	private KpiResultDetailDao kpiResultDetailDao;
	
	@Autowired
	private KpiDao kpiDao;
	
	Logger log = Logger.getLogger(KpiResultDetailServiceImpl.class);
	
	private final static String LOAD_FORMAT_RESULT ="LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '@' ENCLOSED BY '`' LINES TERMINATED BY '\\n' (cdatetime,region_id,result,is_ok)";
	private final static String LOAD_FORMAT ="LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '@' ENCLOSED BY '`' LINES TERMINATED BY '\\n' (period,kpi_id,region_id,result)";
	private final static String LOAD_FORMAT_RESULT_GROUP ="LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '@' ENCLOSED BY '`' LINES TERMINATED BY '\\n' (cdatetime,granularity,group_id,result,is_ok)";
	private final static String LOAD_FORMAT_GROUP ="LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '@' ENCLOSED BY '`' LINES TERMINATED BY '\\n' (period,kpi_id,granularity,group_id,result)";
	private final static String LOAD_FORMAT_CITY_RESULT ="LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '@' ENCLOSED BY '`' LINES TERMINATED BY '\\n' (cdatetime,granularity,`mode`,result,is_ok)";
	private final static String LOAD_FORMAT_CITY ="LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '@' ENCLOSED BY '`' LINES TERMINATED BY '\\n' (period,granularity,`mode`,kpi_id,result)";
	
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	
	/**
	 * 
	 */
	public void syncingByArea(){
		String period = getCycle();
		List<Object[]> resultList = kpiResultDetailDao.getKpiResultDetail(period);
		if(resultList.size()==0){
			log.info("Can not fetch data,return.");
			return;
		}
		String filename = saveToFile(resultList);
		String loadsql = String.format(LOAD_FORMAT, filename,"ices_kpi_result_detail_area");
		log.info("loadsql: "+loadsql);
		kpiDao.loadData(loadsql);
		new File(filename).delete();
		saveKpiResult(period);
	}
	
	public void syncingByCityWeekly(){
		String PERIOD_START_TIME = omcService.getPeriodByCity(1);
		List<StartCycleWithEnd> cycles = omcService.getCycleByCity(1,7);
		List<Object[]> resultList = kpiResultDetailDao.getKpiResultDetailCityWeekly(PERIOD_START_TIME, cycles);
		if(resultList.size()==0){
			log.info("Can not fetch data,return.");
			return;
		}
		String filename = saveToFileCityWeekly(resultList);
		String loadsql = String.format(LOAD_FORMAT_CITY, filename,"ices_kpi_result_detail_city");
		log.info("loadsql: "+loadsql);
		kpiDao.loadData(loadsql);
		new File(filename).delete();
		saveKpiResultCityWeekly(PERIOD_START_TIME);
	}
	
	/**
	 * note: get result by  group, granularity
	 * @param period
	 * @param granularity
	 */
	public void syncingByGroup(String period, int granularity){
		List<Object[]> resultList = kpiResultDetailDao.getKpiResultDetail(period, granularity);
		if(resultList.size()==0){
			log.info("Can not fetch data,return.");
			return;
		}
		String filename = saveToFileGroup(resultList, granularity);
		String loadsql = String.format(LOAD_FORMAT_GROUP, filename,"ices_kpi_result_detail_group");
		log.info("loadsql: "+loadsql);
		kpiDao.loadData(loadsql);
		new File(filename).delete();
		saveKpiResultGroup(period, granularity);
	}
	
	private String saveToFile(List<Object[]> resultList){
		log.info("Attention please,saving data...");
		String filename = "ices_kpi_result_detail_area_" + dateFormat.format(new Date()) + ".txt";
		log.info("Temporary filename: "+filename);
		StringBuffer sb = new StringBuffer();
		int i=1;
		File file = new File(filename);
		FileWriter fw = null;
		BufferedWriter bw =null;
		Map<Integer, KpiResult> kpiResultAreaMap = Application.kpiResultAreaMap;
		try {
			if(file.exists()){
				if(!file.delete()){
					log.error("The file already exists and get exception when delete it, please close it if it is opened.");
					return filename;
				}
			}
			file.createNewFile();
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			for(Object[] objects : resultList){
				String cdatetime = objects[0].toString();
				int kpi_id = Integer.parseInt(objects[1].toString());
				int region_id = Integer.parseInt(objects[2].toString());
				String result = objects[3]+"";
				if (!kpiResultAreaMap.containsKey(region_id)) {
					continue;
				}
				kpiResultAreaMap.get(region_id).getResultMap().put(kpi_id, result);
				sb.append("`");
				sb.append(cdatetime);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(kpi_id);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(region_id);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(result);
				sb.append("`");
				sb.append("@");
				sb.append("\n");
				i++;
				if(i % 1000 == 0){
					bw.append(sb);
					bw.flush();
					sb = new StringBuffer();
					i=1;
				}
			}
			bw.append(sb);
			bw.flush();
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fw != null){
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		log.info("Wow, save data successfully.");
		return filename;
	}
	
	private String saveToFileGroup(List<Object[]> resultList, int granularity){
		log.info("Attention please,saving data...");
		String filename = "ices_kpi_result_detail_group_" + dateFormat.format(new Date()) + ".txt";
		log.info("Temporary filename: "+filename);
		StringBuffer sb = new StringBuffer();
		int i=1;
		File file = new File(filename);
		FileWriter fw = null;
		BufferedWriter bw =null;
		Map<Integer, KpiResult> kpiResultGroupMap = Application.kpiResultGroupMap;
		try {
			if(file.exists()){
				if(!file.delete()){
					log.error("The file already exists and get exception when delete it, please close it if it is opened.");
					return filename;
				}
			}
			file.createNewFile();
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			for(Object[] objects : resultList){
				String cdatetime = objects[0].toString();
				int kpi_id = Integer.parseInt(objects[1].toString());
				int group_id = Integer.parseInt(objects[2].toString());
				String result = objects[3]+"";
				if (!kpiResultGroupMap.containsKey(group_id)) {
					continue;
				}
				kpiResultGroupMap.get(group_id).getResultMap().put(kpi_id, result);
				sb.append("`");
				sb.append(cdatetime);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(kpi_id);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(granularity);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(group_id);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(result);
				sb.append("`");
				sb.append("@");
				sb.append("\n");
				i++;
				if(i % 1000 == 0){
					bw.append(sb);
					bw.flush();
					sb = new StringBuffer();
					i=1;
				}
			}
			bw.append(sb);
			bw.flush();
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fw != null){
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		log.info("Wow, save data successfully.");
		return filename;
	}
	
	private String saveToFileCityWeekly(List<Object[]> resultList){
		log.info("Attention please,saving data...");
		String filename = "ices_kpi_result_detail_city_" + dateFormat.format(new Date()) + ".txt";
		log.info("Temporary filename: "+filename);
		StringBuffer sb = new StringBuffer();
		int i=1;
		File file = new File(filename);
		FileWriter fw = null;
		BufferedWriter bw =null;
		Map<String, KpiResult> kpiResultPerDayCityMap = Application.kpiResultPerDayCityMap;
		try {
			if(file.exists()){
				if(!file.delete()){
					log.error("The file already exists and get exception when delete it, please close it if it is opened.");
					return filename;
				}
			}
			file.createNewFile();
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			for(Object[] objects : resultList){
				String cdatetime = objects[0].toString();
				int granularity = Integer.parseInt(objects[1].toString());
				String mode = objects[2].toString();
				int kpi_id = Integer.parseInt(objects[3].toString());
				String result = objects[4]+"";
				if (!kpiResultPerDayCityMap.containsKey(mode)) {
					continue;
				}
				kpiResultPerDayCityMap.get(mode).getResultMap().put(kpi_id, result);
				sb.append("`");
				sb.append(cdatetime);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(granularity);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(mode);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(kpi_id);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(result);
				sb.append("`");
				sb.append("@");
				sb.append("\n");
				i++;
				if(i % 1000 == 0){
					bw.append(sb);
					bw.flush();
					sb = new StringBuffer();
					i=1;
				}
			}
			bw.append(sb);
			bw.flush();
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fw != null){
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		log.info("Wow, save data successfully.");
		return filename;
	}
	
	
	
	/**
	 * used to save result in kpiResultAreaMap to table ices_kpi_result_area
	 * @param periods
	 */
	private void saveKpiResult(String periods) {
		Map<Integer, KpiResult> kpiResultAreaMap = Application.kpiResultAreaMap;
		String filename = "ices_kpi_result_area_" + dateFormat.format(new Date()) + ".txt";
		log.info("Temporary filename: "+filename);
		StringBuffer sb = new StringBuffer();
		int i=1;
		File file = new File(filename);
		FileWriter fw = null;
		BufferedWriter bw =null;
		try {
			if(file.exists()){
				if(!file.delete()){
					log.error("The file already exists and get exception when delete it, please close it if it is opened.");
				}
			}
			file.createNewFile();
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			for(int areacode : kpiResultAreaMap.keySet()){
				KpiResult kpiResult = kpiResultAreaMap.get(areacode);
				String resultString = kpiResult.getResultMap().toString();
				String is_okString = kpiResult.getIsOkJson();
				sb.append("`");
				sb.append(periods);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(areacode);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(resultString);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(is_okString);
				sb.append("`");
				sb.append("@");
				sb.append("\n");
				i++;
				if(i % 1000 == 0){
					bw.append(sb);
					bw.flush();
					sb = new StringBuffer();
					i=1;
				}
				kpiResultAreaMap.get(areacode).getResultMap().clear();
				kpiResultAreaMap.get(areacode).getIsOkSet().clear();
			}
			bw.append(sb);
			bw.flush();
			String loadsql = String.format(LOAD_FORMAT_RESULT, filename,"ices_kpi_result_area");
			log.info("loadsql: "+loadsql);
			kpiDao.loadData(loadsql);
			file.delete();
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fw != null){
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(file!=null && file.exists()){
				file.delete();
			}
		}
	}
	
	private void saveKpiResultGroup(String periods, int granularity) {
		Map<Integer, KpiResult> kpiResultGroupMap = Application.kpiResultGroupMap;
		String filename = "ices_kpi_result_group_" + dateFormat.format(new Date()) + ".txt";
		log.info("Temporary filename: "+filename);
		StringBuffer sb = new StringBuffer();
		int i=1;
		File file = new File(filename);
		FileWriter fw = null;
		BufferedWriter bw =null;
		try {
			if(file.exists()){
				if(!file.delete()){
					log.error("The file already exists and get exception when delete it, please close it if it is opened.");
				}
			}
			file.createNewFile();
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			for(int group_id : kpiResultGroupMap.keySet()){
				KpiResult kpiResult = kpiResultGroupMap.get(group_id);
				String resultString = kpiResult.getResultMap().toString();
				String is_okString = kpiResult.getIsOkJson();
				sb.append("`");
				sb.append(periods);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(granularity);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(group_id);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(resultString);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(is_okString);
				sb.append("`");
				sb.append("@");
				sb.append("\n");
				i++;
				if(i % 1000 == 0){
					bw.append(sb);
					bw.flush();
					sb = new StringBuffer();
					i=1;
				}
				kpiResultGroupMap.get(group_id).getResultMap().clear();
				kpiResultGroupMap.get(group_id).getIsOkSet().clear();
			}
			bw.append(sb);
			bw.flush();
			String loadsql = String.format(LOAD_FORMAT_RESULT_GROUP, filename,"ices_kpi_result_group");
			log.info("loadsql: "+loadsql);
			kpiDao.loadData(loadsql);
			file.delete();
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fw != null){
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(file!=null && file.exists()){
				file.delete();
			}
		}
	}
	
	private void saveKpiResultCityWeekly(String periods) {
		Map<String, KpiResult> kpiResultPerDayCityMap = Application.kpiResultPerDayCityMap;
		String filename = "ices_kpi_result_city_" + dateFormat.format(new Date()) + ".txt";
		log.info("Temporary filename: "+filename);
		StringBuffer sb = new StringBuffer();
		int i=1;
		File file = new File(filename);
		FileWriter fw = null;
		BufferedWriter bw =null;
		try {
			if(file.exists()){
				if(!file.delete()){
					log.error("The file already exists and get exception when delete it, please close it if it is opened.");
				}
			}
			file.createNewFile();
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			for(String mode : kpiResultPerDayCityMap.keySet()){
				KpiResult kpiResult = kpiResultPerDayCityMap.get(mode);
				String resultString = kpiResult.getResultMap().toString();
				String is_okString = kpiResult.getIsOkJson();
				sb.append("`");
				sb.append(periods);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(7);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(mode);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(resultString);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(is_okString);
				sb.append("`");
				sb.append("@");
				sb.append("\n");
				i++;
				if(i % 1000 == 0){
					bw.append(sb);
					bw.flush();
					sb = new StringBuffer();
					i=1;
				}
				kpiResultPerDayCityMap.get(mode).getResultMap().clear();
				kpiResultPerDayCityMap.get(mode).getIsOkSet().clear();
			}
			bw.append(sb);
			bw.flush();
			String loadsql = String.format(LOAD_FORMAT_CITY_RESULT, filename,"ices_kpi_result_city");
			log.info("loadsql: "+loadsql);
			kpiDao.loadData(loadsql);
			file.delete();
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fw != null){
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(file!=null && file.exists()){
				file.delete();
			}
		}
	}
	
	/**
	 * @param omc_id
	 * @return
	 */
	private String getCycle(){
		String sysdate = omcDao.sysdate(1);
		log.info("The current time of omc server is: " + sysdate);
		Calendar calendar = new GregorianCalendar();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date currentDate;
		try {
			currentDate = dateFormat.parse(sysdate);
			calendar.setTime(currentDate);
			calendar.set(Calendar.SECOND, 0);   //秒设置成0
			calendar.set(Calendar.MINUTE, 0);   //分钟设置成0
			calendar.add(Calendar.HOUR_OF_DAY, -1); //减去1小时
			
		} catch (ParseException e) {
			log.error("getCycle error!");
			e.printStackTrace();
		}
		return dateFormat.format(calendar.getTime());
	}
}
