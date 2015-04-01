package com.nsn.ices.model.service.impl;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.nsn.ices.common.annotation.ResourceValue;
import com.nsn.ices.common.utils.KpiCalculateUtil;
import com.nsn.ices.core.Application;
import com.nsn.ices.model.dao.KpiDao;
import com.nsn.ices.model.dao.KpiResultDetailDao;
import com.nsn.ices.model.dao.OmcDao;
import com.nsn.ices.model.entity.Cell;
import com.nsn.ices.model.entity.KpiBusyTime;
import com.nsn.ices.model.entity.KpiResult;
import com.nsn.ices.model.entity.StartCycleWithEnd;
import com.nsn.ices.model.service.KpiResultDetailService;
import com.nsn.ices.model.service.OmcService;

@Service(value="omcService")
public class OmcServiceImpl implements OmcService {
	
	@Autowired
	private OmcDao omcDao;
	
	@Autowired
	private KpiDao kpiDao;
	
	@Autowired
	private KpiResultDetailDao kpiResultDetailDao;
	
	@Autowired
	private KpiResultDetailService kpiResultDetailService;
	
	@ResourceValue(value="loadDataLocalFileDirectory")
	private String loadDataLocalFileDirectory;
	
	Logger log = Logger.getLogger(OmcServiceImpl.class);
	
	private final static String LOAD_FORMAT_RESULT ="LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '@' ENCLOSED BY '`' LINES TERMINATED BY '\\n' (ne_code,cel_id,cdatetime,result,is_ok)";
	private final static String LOAD_FORMAT ="LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '@' ENCLOSED BY '`' LINES TERMINATED BY '\\n' (period,kpi_id,cel_obj_gid,result)";
	private final static String LOAD_FORMAT_CITY_RESULT ="LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '@' ENCLOSED BY '`' LINES TERMINATED BY '\\n' (cdatetime,granularity,`mode`,result,is_ok)";
	private final static String LOAD_FORMAT_CITY ="LOAD DATA LOCAL INFILE '%s' INTO TABLE %s FIELDS TERMINATED BY '@' ENCLOSED BY '`' LINES TERMINATED BY '\\n' (period,granularity,`mode`,kpi_id,result)";
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	
	@Transactional
	public void syncing(int omc_id, int kpi_id, StartCycleWithEnd periods, int rate){
		log.info("Syncing data at "+periods+", omc_id: "+omc_id+", kpi_id: "+kpi_id +", rate: "+rate);
		String kpiResultTable = "ices_kpi_result" ,kpiResultDetailTable = "ices_kpi_result_detail";
		Map<Integer, KpiResult> kpiResultMap = Application.kpiResultMap15;
		kpiResultTable = KpiCalculateUtil.getIcesKpiResultTableByGranularity(rate);
		kpiResultDetailTable = KpiCalculateUtil.getIcesKpiResultDetailTableByGranularity(rate);
		switch (rate) {
		case 1:
			kpiResultMap = Application.kpiResultPerDayMap;
			break;
		case 15:
			kpiResultMap = Application.kpiResultMap15;
			break;
		case 30:
			kpiResultMap = Application.kpiResultMap30;
			break;
		case 60:
			kpiResultMap = Application.kpiResultMap60;
			break;
		default:
			log.error("Value of rate must be 15, 30, 60 or 1, got invalid rate : "+rate);
			return;
		}
		try {
			String kpi_name_en = Application.kpiMap.get(kpi_id).getKpi_name_en();
			String kpi_name = kpi_name_en.substring(0, kpi_name_en.lastIndexOf("_"));
			List<Object[]> resultList = omcDao.find(omc_id, kpi_id, periods);
			log.info("KPI:"+kpi_name_en+", cycle:"+periods.getStartCycle()+", result list size: "+resultList.size());
			if(resultList.size()==0){
				log.info("Can not fetch data,return.");
				return;
			}
			saveKpiResultDetail(resultList, kpi_name, rate, kpiResultMap, kpiResultDetailTable);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			switch (rate) {
			case 1:
				Application.perDayCount ++;
//				if(Application.perDayCount == 44){
				if(Application.perDayCount == 78){
					saveKpiResult(periods, rate, kpiResultMap, kpiResultTable);
					kpiResultDetailService.syncingByGroup(periods.getStartCycle(), rate);
				}
				break;
			case 15:
				Application.count15 ++;
//				if(Application.count15 == 44){
				if(Application.count15 == 78){
					saveKpiResult(periods, rate, kpiResultMap, kpiResultTable);
					kpiResultDetailService.syncingByGroup(periods.getStartCycle(), rate);
				}
				break;
			case 30:
				Application.count30 ++;
//				if(Application.count30 == 44){
				if(Application.count30 == 78){
					saveKpiResult(periods, rate, kpiResultMap, kpiResultTable);
					kpiResultDetailService.syncingByGroup(periods.getStartCycle(), rate);
				}
				break;
			case 60:
				Application.count60 ++;
//				if(Application.count60 == 44){
				if(Application.count60 == 78){
					saveKpiResult(periods, rate, kpiResultMap, kpiResultTable);
					kpiResultDetailService.syncingByGroup(periods.getStartCycle(), rate);
				}
				break;
			default:
				log.error("Value of rate must be 15, 30, 60 or 1, got invalid rate : "+rate);
				return;
			}
		}
		log.info("Synced data at "+periods+", omc_id: "+omc_id+", kpi_id: "+kpi_id);
		
	}
	
	@Transactional
	public void syncingByCity(int omc_id, int kpi_id, String PERIOD_START_TIME, List<StartCycleWithEnd> periods, int rate){
		log.info("Syncing data at "+periods+", omc_id: "+omc_id+", kpi_id: "+kpi_id +", rate: "+rate);
		String kpiResultTable = "ices_kpi_result_city" ,kpiResultDetailTable = "ices_kpi_result_detail_city";
		Map<String, KpiResult> kpiResultMap = Application.kpiResultPerDayCityMap;
		try {
			String kpi_name_en = Application.kpiMap.get(kpi_id).getKpi_name_en();
			String kpi_name = kpi_name_en.substring(0, kpi_name_en.lastIndexOf("_"));
			List<Object[]> resultList = omcDao.findByCity(omc_id, kpi_id, PERIOD_START_TIME, periods);
			log.info("KPI:"+kpi_name_en+", cycle:"+PERIOD_START_TIME+", result list size: "+resultList.size());
			if(resultList.size()==0){
				log.info("Can not fetch data,return.");
				return;
			}
			saveKpiResultDetailByCity(resultList, kpi_name, rate, kpiResultMap, kpiResultDetailTable);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			Application.perDayCityCount ++;
			if(Application.perDayCityCount == 23){
				saveKpiResultByCity(PERIOD_START_TIME, rate, kpiResultMap, kpiResultTable);
			}
		}
		log.info("Synced data at "+periods+", omc_id: "+omc_id+", kpi_id: "+kpi_id);
		
	}
	
	/**
	 * used to save result in kpiResultMap to table ices_kpi_result
	 * @param periods
	 */
	private void saveKpiResult(StartCycleWithEnd periods, int rate, Map<Integer, KpiResult> kpiResultMap, String kpiResultTable) {
		String filename = loadDataLocalFileDirectory+"ices_kpi_result_" + rate + "_" + dateFormat.format(new Date()) + ".txt";
		log.info("Temporary filename: "+filename);
		StringBuffer sb = new StringBuffer();
		int i=1;
		File file = new File(filename);
		FileWriter fw = null;
		BufferedWriter bw =null;
		try {
			if(file.exists()){
				if(!file.delete()){
					log.error("The file already exists and got error when delete it, please close it if it is opened.");
				}
			}
			file.createNewFile();
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			for(int cell_id: kpiResultMap.keySet()){
				sb.append("`");
				sb.append(Application.cellMap.get(cell_id).getNe_code());
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(cell_id);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(periods.getStartCycle());
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(kpiResultMap.get(cell_id).getResultMap());
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(kpiResultMap.get(cell_id).getIsOkJson());
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
				kpiResultMap.get(cell_id).getResultMap().clear();
				kpiResultMap.get(cell_id).getIsOkSet().clear();
			}
			bw.append(sb);
			bw.flush();
			String loadsql = String.format(LOAD_FORMAT_RESULT, filename, kpiResultTable);
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
	
	private void saveKpiResultByCity(String period, int rate, Map<String, KpiResult> kpiResultMap, String kpiResultTable) {
		String filename = loadDataLocalFileDirectory+"ices_kpi_result_city_" + rate + "_" + dateFormat.format(new Date()) + ".txt";
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
			for(String mode: kpiResultMap.keySet()){
				sb.append("`");
				sb.append(period);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(rate);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(mode);
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(kpiResultMap.get(mode).getResultMap());
				sb.append("`");
				sb.append("@");
				sb.append("`");
				sb.append(kpiResultMap.get(mode).getIsOkJson());
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
				kpiResultMap.get(mode).getResultMap().clear();
				kpiResultMap.get(mode).getIsOkSet().clear();
			}
			bw.append(sb);
			bw.flush();
			String loadsql = String.format(LOAD_FORMAT_CITY_RESULT, filename, kpiResultTable);
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
	 * Save the selected result list to a temporary file,which generated the filename using kpi name,date time, if that file is already there,delete it.
	 * Note: When you finished load data from the file,remember to delete it in order not to hold the limited space.We use different method organize the data through kpi name or the returned columns count,cause 
	 * 	     different kpi has different format value,however the difference is small.The following kpi must take more care:KPI_006_1,KPI_013_1,KPI_016_1.
	 * @param resultList
	 * @param kpi_name
	 * @return
	 */
	private void saveKpiResultDetail(List<Object[]> resultList, String kpi_name, int rate, Map<Integer, KpiResult> kpiResultMap, String kpiResultDetailTable){
		log.info("Attention please,saving data...");
		String filename = loadDataLocalFileDirectory+"ices_kpi_result_detail_" + rate + "_" + kpi_name + "_" + dateFormat.format(new Date()) + ".txt";
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
					return ;
				}
			}
			file.createNewFile();
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			int columnCount = resultList.get(0).length;
			if("KPI_006".equals(kpi_name)){
				Map<Integer, Map<Integer, Cell>> btsCellMap = Application.btsCellMap;
				int kpi_id_1 = Application.kpiNameMap.get(kpi_name+"_1").getId();
				int kpi_id_2 = Application.kpiNameMap.get(kpi_name+"_2").getId();
				int kpi_id_3 = Application.kpiNameMap.get(kpi_name+"_3").getId();
				int type =  Application.kpiNameMap.get(kpi_name+"_1").getType();
				boolean hasThreshold_kpi_id_1 = hasThreshold(kpi_id_1);
				boolean hasThreshold_kpi_id_11 = hasThreshold1(kpi_id_1,type);
				boolean hasThreshold_kpi_id_12 = hasThreshold2(kpi_id_1,type);
				boolean hasThreshold_kpi_id_2 = hasThreshold(kpi_id_2);
				boolean hasThreshold_kpi_id_3 = hasThreshold(kpi_id_3);
				double threshold_kpi_id_1 = getThreshold(kpi_id_1);
				double threshold_kpi_id_11 = getThreshold1(kpi_id_1);
				double threshold_kpi_id_12 = getThreshold2(kpi_id_1);
				double threshold_kpi_id_2 = getThreshold(kpi_id_2);
				double threshold_kpi_id_3 = getThreshold(kpi_id_3);
				String relation_kpi_id_1 = getRelation(kpi_id_1);
				String relation_kpi_id_11 = getRelation1(kpi_id_1);
				String relation_kpi_id_12 = getRelation2(kpi_id_1);
				String relation_kpi_id_2 = getRelation(kpi_id_2);
				String relation_kpi_id_3 = getRelation(kpi_id_3);
				if(type == 1){ //1:分子/分母的百分比,可能有附加阈值
				}
				for(Object[] objects : resultList){
					int ne_code = Integer.parseInt(objects[1].toString());
					Map<Integer, Cell> cellMap = btsCellMap.get(ne_code);
					if(cellMap == null || cellMap.size()==0){
						continue;
					}
					for(int cell_id: cellMap.keySet()){
						kpiResultMap.get(cell_id).getResultMap().put(kpi_id_1, objects[4]+"");
						kpiResultMap.get(cell_id).getResultMap().put(kpi_id_2, objects[2]+"");
						kpiResultMap.get(cell_id).getResultMap().put(kpi_id_3, objects[3]+"");
						boolean compareResult1 = false, compareResult11 = false, compareResult12 = false, compareResult2 = false,compareResult3 = false;
						if (hasThreshold_kpi_id_1 && objects[4] != null) {
							double realValue = Double.parseDouble(objects[4]+"");
							compareResult1 = compareTo(realValue, threshold_kpi_id_1, relation_kpi_id_1);
							if(type == 1){
								
							}
						}
						if (hasThreshold_kpi_id_11 && objects[2] != null) {
							double realValue = Double.parseDouble(objects[2]+"");
							compareResult11 = compareTo(realValue, threshold_kpi_id_11, relation_kpi_id_11);
						}
						if (hasThreshold_kpi_id_12 && objects[3] != null) {
							double realValue = Double.parseDouble(objects[3]+"");
							compareResult12 = compareTo(realValue, threshold_kpi_id_12, relation_kpi_id_12);
						}
						if (hasThreshold_kpi_id_2 && objects[2] != null) {
							double realValue = Double.parseDouble(objects[2]+"");
							compareResult2 = compareTo(realValue, threshold_kpi_id_2, relation_kpi_id_2);
						}
						if (hasThreshold_kpi_id_3 && objects[3] != null) {
							double realValue = Double.parseDouble(objects[3]+"");
							compareResult3 = compareTo(realValue, threshold_kpi_id_3, relation_kpi_id_3);
						}
						//记录真实值超过阀值的kpi
						if(hasThreshold_kpi_id_1 && hasThreshold_kpi_id_11 && hasThreshold_kpi_id_12){
							if(compareResult1 && compareResult11 && compareResult12){
								kpiResultMap.get(cell_id).getIsOkSet().add(kpi_id_1);
							}
						}else if(hasThreshold_kpi_id_1 && hasThreshold_kpi_id_11){
							if(compareResult1 && compareResult11){
								kpiResultMap.get(cell_id).getIsOkSet().add(kpi_id_1);
							}
						}else if(hasThreshold_kpi_id_1 && hasThreshold_kpi_id_12){
							if(compareResult1 && compareResult12){
								kpiResultMap.get(cell_id).getIsOkSet().add(kpi_id_1);
							}
						}else if(compareResult1){
							kpiResultMap.get(cell_id).getIsOkSet().add(kpi_id_1);
						}
						
						if(compareResult2){
							kpiResultMap.get(cell_id).getIsOkSet().add(kpi_id_2);
						}
						
						if(compareResult3){
							kpiResultMap.get(cell_id).getIsOkSet().add(kpi_id_3);
						}
						
						sb.append("`");
						sb.append(objects[0]);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(kpi_id_2);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(cell_id);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(objects[2]);
						sb.append("`");
						sb.append("@");
						sb.append("\n");
						
						sb.append("`");
						sb.append(objects[0]);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(kpi_id_3);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(cell_id);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(objects[3]);
						sb.append("`");
						sb.append("@");
						sb.append("\n");
						
						sb.append("`");
						sb.append(objects[0]);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(kpi_id_1);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(cell_id);
						sb.append("`");
						sb.append("@");
						sb.append("`");
						sb.append(objects[4]);
						sb.append("`");
						sb.append("@");
						sb.append("\n");
					}
					i++;
					if(i % 1000 == 0){
						bw.append(sb);
						bw.flush();
						sb = new StringBuffer();
						i=1;
					}
				}
			}else if("KPI_013".equals(kpi_name)){
				int kpi_id_13 = Application.kpiNameMap.get(kpi_name+"_1").getId();
				int kpi_id_16 = Application.kpiNameMap.get("KPI_016_1").getId();
				for(Object[] objects : resultList){
					if (objects[1] == null || !Application.cellMap.containsKey(Integer.parseInt(objects[1]+""))) {
						continue;
					}
					kpiResultMap.get(Integer.parseInt(objects[1]+"")).getResultMap().put(kpi_id_13, "'"+objects[2]+" DL PRBS'");
					kpiResultMap.get(Integer.parseInt(objects[1]+"")).getResultMap().put(kpi_id_16, "'"+objects[3]+" DL PRBS'");
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_13);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[2]);
					sb.append("`");
					sb.append("@");
					sb.append("\n");
					
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_16);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[3]);
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
			}else if(columnCount==5){
				int kpi_id_1 = Application.kpiNameMap.get(kpi_name+"_1").getId();
				int kpi_id_2 = Application.kpiNameMap.get(kpi_name+"_2").getId();
				int kpi_id_3 = Application.kpiNameMap.get(kpi_name+"_3").getId();
				int type =  Application.kpiNameMap.get(kpi_name+"_1").getType();
				boolean hasThreshold_kpi_id_10 = hasThreshold(kpi_id_1);
				boolean hasThreshold_kpi_id_11 = hasThreshold1(kpi_id_1,type);//是否有阈值1,通常为分子
				boolean hasThreshold_kpi_id_12 = hasThreshold2(kpi_id_1,type);//是否有阈值2,通常为分母
				boolean hasThreshold_kpi_id_2 = hasThreshold(kpi_id_2);
				boolean hasThreshold_kpi_id_3 = hasThreshold(kpi_id_3);
				double threshold_kpi_id_10 = getThreshold(kpi_id_1);
				double threshold_kpi_id_11 = getThreshold1(kpi_id_1);
				double threshold_kpi_id_12 = getThreshold2(kpi_id_1);
				double threshold_kpi_id_2 = getThreshold(kpi_id_2);
				double threshold_kpi_id_3 = getThreshold(kpi_id_3);
				String relation_kpi_id_10 = getRelation(kpi_id_1);
				String relation_kpi_id_11 = getRelation1(kpi_id_1);
				String relation_kpi_id_12 = getRelation2(kpi_id_1);
				String relation_kpi_id_2 = getRelation(kpi_id_2);
				String relation_kpi_id_3 = getRelation(kpi_id_3);
				for(Object[] objects : resultList){
					if (!Application.cellMap.containsKey(Integer.parseInt(objects[1]+""))) {
						continue;
					}
					kpiResultMap.get(Integer.parseInt(objects[1]+"")).getResultMap().put(kpi_id_1, objects[4]+"");
					kpiResultMap.get(Integer.parseInt(objects[1]+"")).getResultMap().put(kpi_id_2, objects[2]+"");
					kpiResultMap.get(Integer.parseInt(objects[1]+"")).getResultMap().put(kpi_id_3, objects[3]+"");
					boolean compareResult10 = false, compareResult11 = false, compareResult12 = false, compareResult2 = false,compareResult3 = false;
					if (hasThreshold_kpi_id_10 && objects[4] != null) {
						double realValue = Double.parseDouble(objects[4]+"");
						compareResult10 = compareTo(realValue, threshold_kpi_id_10, relation_kpi_id_10);
					}
					if (hasThreshold_kpi_id_11 && objects[2] != null) {
						double realValue = Double.parseDouble(objects[2]+"");
						compareResult11 = compareTo(realValue, threshold_kpi_id_11, relation_kpi_id_11);
					}
					if (hasThreshold_kpi_id_12 && objects[3] != null) {
						double realValue = Double.parseDouble(objects[3]+"");
						compareResult12 = compareTo(realValue, threshold_kpi_id_12, relation_kpi_id_12);
					}
					if (hasThreshold_kpi_id_2 && objects[2] != null) {
						double realValue = Double.parseDouble(objects[2]+"");
						compareResult2 = compareTo(realValue, threshold_kpi_id_2, relation_kpi_id_2);
					}
					if (hasThreshold_kpi_id_3 && objects[3] != null) {
						double realValue = Double.parseDouble(objects[3]+"");
						compareResult3 = compareTo(realValue, threshold_kpi_id_3, relation_kpi_id_3);
					}
					//记录超过阀值的kpi
					if(hasThreshold_kpi_id_10 && hasThreshold_kpi_id_11 && hasThreshold_kpi_id_12){
						if(compareResult10 && compareResult11 && compareResult12){
							kpiResultMap.get(Integer.parseInt(objects[1]+"")).getIsOkSet().add(kpi_id_1);
						}
					}else if(hasThreshold_kpi_id_10 && hasThreshold_kpi_id_11){
						if(compareResult10 && compareResult11){
							kpiResultMap.get(Integer.parseInt(objects[1]+"")).getIsOkSet().add(kpi_id_1);
						}
					}else if(hasThreshold_kpi_id_10 && hasThreshold_kpi_id_12){
						if(compareResult10 && compareResult12){
							kpiResultMap.get(Integer.parseInt(objects[1]+"")).getIsOkSet().add(kpi_id_1);
						}
					}else if(compareResult10){
						kpiResultMap.get(Integer.parseInt(objects[1]+"")).getIsOkSet().add(kpi_id_1);
					}
					
					if(compareResult2){
						kpiResultMap.get(Integer.parseInt(objects[1]+"")).getIsOkSet().add(kpi_id_2);
					}
					
					if(compareResult3){
						kpiResultMap.get(Integer.parseInt(objects[1]+"")).getIsOkSet().add(kpi_id_3);
					}
					
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_2);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[2]);
					sb.append("`");
					sb.append("@");
					sb.append("\n");
					
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_3);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[3]);
					sb.append("`");
					sb.append("@");
					sb.append("\n");
					
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_1);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[4]);
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
			}else if(columnCount == 3){
				int kpi_id_1 = Application.kpiNameMap.get(kpi_name+"_1").getId();
				boolean hasThreshold_kpi_id_1 = hasThreshold(kpi_id_1);
				double threshold_kpi_id_1 = getThreshold(kpi_id_1);
				String relation_kpi_id_1 = getRelation(kpi_id_1);
				for(Object[] objects : resultList){
					if (!Application.cellMap.containsKey(Integer.parseInt(objects[1]+""))) {
						continue;
					}
					kpiResultMap.get(Integer.parseInt(objects[1]+"")).getResultMap().put(kpi_id_1, objects[2]+"");
					if (hasThreshold_kpi_id_1 && objects[2] != null) {
						double realValue = Double.parseDouble(objects[2]+"");
						if(compareTo(realValue, threshold_kpi_id_1, relation_kpi_id_1)){
							kpiResultMap.get(Integer.parseInt(objects[1]+"")).getIsOkSet().add(kpi_id_1);
						}
					}
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_1);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[2]);
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
			}
			bw.append(sb);
			bw.flush();
			bw.close();
			String loadsql = String.format(LOAD_FORMAT, filename, kpiResultDetailTable);
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
				log.error("Got some errors when delete file "+filename);
				file.delete();
			}
		}
		log.info("Wow, save data successfully.");
	}
	
	private void saveKpiResultDetailByCity(List<Object[]> resultList, String kpi_name, int rate, Map<String, KpiResult> kpiResultMap, String kpiResultDetailTable){
		log.info("Attention please,saving data...");
		String filename = loadDataLocalFileDirectory+"ices_kpi_result_detail_city_" + rate + "_" + kpi_name + "_" + dateFormat.format(new Date()) + ".txt";
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
					return ;
				}
			}
			file.createNewFile();
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			int columnCount = resultList.get(0).length;
			if(columnCount==5){
				int kpi_id_1 = Application.kpiNameMap.get(kpi_name+"_1").getId();
				int kpi_id_2 = Application.kpiNameMap.get(kpi_name+"_2").getId();
				int kpi_id_3 = Application.kpiNameMap.get(kpi_name+"_3").getId();
				for(Object[] objects : resultList){
//					if (!Application.cellMap.containsKey(Integer.parseInt(objects[1]+""))) {
//						continue;
//					}
					kpiResultMap.get(objects[1]+"").getResultMap().put(kpi_id_1, objects[4]+"");
					kpiResultMap.get(objects[1]+"").getResultMap().put(kpi_id_2, objects[2]+"");
					kpiResultMap.get(objects[1]+"").getResultMap().put(kpi_id_3, objects[3]+"");
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(rate);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_2);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[2]);
					sb.append("`");
					sb.append("@");
					sb.append("\n");
					
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(rate);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_3);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[3]);
					sb.append("`");
					sb.append("@");
					sb.append("\n");
					
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(rate);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_1);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[4]);
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
			}else if(columnCount == 3){
				int kpi_id_1 = Application.kpiNameMap.get(kpi_name+"_1").getId();
				for(Object[] objects : resultList){
//					if (!Application.cellMap.containsKey(Integer.parseInt(objects[1]+""))) {
//						continue;
//					}
					kpiResultMap.get(objects[1]+"").getResultMap().put(kpi_id_1, objects[2]+"");
					sb.append("`");
					sb.append(objects[0]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(rate);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[1]);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(kpi_id_1);
					sb.append("`");
					sb.append("@");
					sb.append("`");
					sb.append(objects[2]);
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
			}
			bw.append(sb);
			bw.flush();
			bw.close();
			String loadsql = String.format(LOAD_FORMAT_CITY, filename, kpiResultDetailTable);
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
				log.error("Got some errors when delete file "+filename);
				file.delete();
			}
		}
		log.info("Wow, save data successfully.");
	}
	
	/**
	 * @param omc_id
	 * @param rate 固定频率 15,30,60
	 * @return
	 */
	public StartCycleWithEnd getCycle(int omc_id, int rate){
		if(rate == 1){ //per day every cell
			return getCycle1(omc_id, rate);
		}
		String sysdate = omcDao.sysdate(omc_id);
		log.info("The current time of omc server is: " + sysdate);
		Calendar calendar = new GregorianCalendar();
		Date startDate, endDate;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date currentDate;
		StartCycleWithEnd startCycleWithEnd = new StartCycleWithEnd();
		try {
			currentDate = dateFormat.parse(sysdate);
			calendar.setTime(currentDate);
			calendar.set(Calendar.SECOND, 0);   //秒设置成0
			calendar.add(Calendar.MINUTE, -rate); //当前时间减去rate分钟
			if(rate == 15){
				calendar.add(Calendar.MINUTE, -15); //当前时间减去rate分钟
			}
			int minute = calendar.get(Calendar.MINUTE);
			int mod = minute / rate;
			if (mod > 0) {
				minute = rate * mod;
			} else {
				minute = mod;
			}
			calendar.set(Calendar.MINUTE, minute);
			startDate = calendar.getTime();
			calendar.add(Calendar.MINUTE, rate);
			endDate = calendar.getTime();
			startCycleWithEnd.setStartCycle(dateFormat.format(startDate));
			startCycleWithEnd.setEndCycle(dateFormat.format(endDate));
		} catch (ParseException e) {
			log.error("getCycle error!");
			e.printStackTrace();
		}
		return startCycleWithEnd;
	}
	
	private StartCycleWithEnd getCycle1(int omc_id, int rate){
		String sysdate = omcDao.sysdate(omc_id);
		Calendar calendar = new GregorianCalendar();
		Date startDate, endDate;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat resultDateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		Date currentDate;
		StartCycleWithEnd startCycleWithEnd = new StartCycleWithEnd();
		try {
			currentDate = dateFormat.parse(sysdate);
			calendar.setTime(currentDate);
			endDate = calendar.getTime();
			calendar.add(Calendar.DAY_OF_MONTH, -rate); //当前时间减去rate天
			startDate = calendar.getTime();
			System.out.println(startDate+":"+endDate);
			startCycleWithEnd.setStartCycle(resultDateFormat.format(startDate));
			startCycleWithEnd.setEndCycle(resultDateFormat.format(endDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return startCycleWithEnd;
	}
	
	private List<StartCycleWithEnd> getCycleByCity(int omc_id){
		String sysdate = omcDao.sysdate(omc_id);
		Calendar calendar = new GregorianCalendar();
		Date startDate, endDate;
		List<StartCycleWithEnd> startCycleWithEnds = new ArrayList<StartCycleWithEnd>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat resultDateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		Date currentDate;
		StartCycleWithEnd startCycleWithEnd = null;
		try {
			currentDate = dateFormat.parse(sysdate);
			calendar.setTime(currentDate);
			calendar.add(Calendar.DAY_OF_MONTH, -7); //当前时间减去rate天, 上周五
			startDate = calendar.getTime();
			calendar.add(Calendar.DAY_OF_MONTH, 1); //加1天,上周六
			endDate = calendar.getTime();
			startCycleWithEnd = new StartCycleWithEnd();
			startCycleWithEnd.setStartCycle(resultDateFormat.format(startDate));
			startCycleWithEnd.setEndCycle(resultDateFormat.format(endDate));
			startCycleWithEnds.add(startCycleWithEnd);
			
			calendar.setTime(currentDate);
			endDate = calendar.getTime(); //本周五
			calendar.add(Calendar.DAY_OF_MONTH, -4); //当前时间减去rate天 本周一
			startDate = calendar.getTime(); 
			startCycleWithEnd = new StartCycleWithEnd();
			startCycleWithEnd.setStartCycle(resultDateFormat.format(startDate));
			startCycleWithEnd.setEndCycle(resultDateFormat.format(endDate));
			startCycleWithEnds.add(startCycleWithEnd);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return startCycleWithEnds;
	}
	
	/**
	 * Note: this method is used to get several periods of one day witch user defined themselves. 
	 * eg: when input string  "7,6,8,9,8,12,17-20,20-21",returned periods: 2014-10-20 06:00:00至2014-10-20 10:00:00, 2014-10-20 12:00:00至2014-10-20 13:00:00, 2014-10-20 17:00:00至2014-10-20 22:00:00.
	 * @param omc_id
	 * @return
	 */
	public List<StartCycleWithEnd> getCycleByCity(int omc_id, int rate){
		if (rate == 7) {
			return getCycleByCity(omc_id);
		}
		String sysdate = omcDao.sysdate(omc_id);
		List<StartCycleWithEnd> startCycleWithEnds = new ArrayList<StartCycleWithEnd>();
		Calendar calendar = new GregorianCalendar();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat resultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
		Date currentDate;
		try {
			currentDate = dateFormat.parse(sysdate);
			calendar.setTime(currentDate);
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		String busyTime = "";
		List<KpiBusyTime> kpiBusyTimeList = kpiResultDetailDao.getKpiBusyTime(sysdate);
		if (kpiBusyTimeList.size() ==0 ) {
			KpiBusyTime kpiBusyTimeDefault = kpiResultDetailDao.getKpiBusyTimeDefault();
			busyTime = kpiBusyTimeDefault.getBusy_time();
		}else {
			for(KpiBusyTime kpiBusyTime : kpiBusyTimeList){
				if("".equals(busyTime)){
					busyTime = kpiBusyTime.getBusy_time();
				}else if(!"".equals(kpiBusyTime.getBusy_time())){
					busyTime += "," + kpiBusyTime.getBusy_time();
				}
			}
		}
		Set<Integer> hourSet = new HashSet<Integer>();
		busyTime=busyTime.replaceAll(" ", "");
		String[] hours = busyTime.split(",");
		for (String hour: hours) {
			if(!hour.contains("-")){
				hourSet.add(Integer.parseInt(hour));
			}else {
				String[] subhours = hour.split("-");
				if (subhours.length == 2) {
					int hourFrom = Integer.parseInt(subhours[0]);
					int hourTo = Integer.parseInt(subhours[1]);
					if (hourFrom<hourTo) {
						for(int i = hourFrom ;i<=hourTo;i++){
							hourSet.add(i);
						}
					}
				}
			}
		}
		Object[] hourIntegers = hourSet.toArray();
		Arrays.sort(hourIntegers);
		int firstNum = 0, nextNum = 0;
		for(int i=0; i<hourIntegers.length; i++){
			firstNum = Integer.parseInt(hourIntegers[i]+"");
			int j = 0;
			while (true) {
				j++;
				if (i+1 >= hourIntegers.length) {
					break;
				}
				nextNum = Integer.parseInt(hourIntegers[i+1]+"");
				if (nextNum != firstNum +j) {
					nextNum = Integer.parseInt(hourIntegers[i]+"");
					break;
				}else {
					i++;
				}
			}
			StartCycleWithEnd se1 = new StartCycleWithEnd();
			calendar.set(Calendar.HOUR_OF_DAY, firstNum);
			se1.setStartCycle(resultDateFormat.format(calendar.getTime()));
			if (j > 1) {
				calendar.set(Calendar.HOUR_OF_DAY, nextNum+1);
			}else {
				calendar.set(Calendar.HOUR_OF_DAY, firstNum+1);
			}
			se1.setEndCycle(resultDateFormat.format(calendar.getTime()));
			startCycleWithEnds.add(se1);
		}
		return startCycleWithEnds;
	}
	
	public String getPeriodByCity(int omc_id){
		String sysdate = omcDao.sysdate(omc_id);
		Calendar calendar = new GregorianCalendar();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date currentDate;
		try {
			currentDate = dateFormat.parse(sysdate);
			calendar.setTime(currentDate);
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(calendar.getTime());
	}
	
	/**
	 * 是否有阈值(0:有,1:没有)
	 * @param kpi_id
	 * @return
	 */
	private boolean hasThreshold(int kpi_id){
		return Application.kpiMap.get(kpi_id).getHas_threshold() == 0; 
	}
	
	//判断是否有附加阈值1,判断关系1不为空,并且阈值1不为0 返回true
	private boolean hasThreshold1(int kpi_id, int type){
		boolean result  = false;
		if(type==1){
			String relation11 = Application.kpiMap.get(kpi_id).getRelation1();
			double threshold11 =  Application.kpiMap.get(kpi_id).getThreshold1();
			if (!StringUtils.isEmpty(relation11) && !(threshold11 == 0)) {
				result = true;
			}
		}
		return result; 
	}
	
	//判断是否有附加阈值2,判断关系2不为空,并且阈值2不为0 返回true
	private boolean hasThreshold2(int kpi_id, int type){
		boolean result  = false;
		if(type==1){
			String relation12 = Application.kpiMap.get(kpi_id).getRelation2();
			double threshold12 =  Application.kpiMap.get(kpi_id).getThreshold2();
			if (!StringUtils.isEmpty(relation12) && !(threshold12 == 0)) {
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * 获取指标的阈值
	 * @param kpi_id
	 * @return
	 */
	private double getThreshold(int kpi_id){
		return Application.kpiMap.get(kpi_id).getThreshold();
	}
	
	private double getThreshold1(int kpi_id){
		return Application.kpiMap.get(kpi_id).getThreshold1();
	}
	
	private double getThreshold2(int kpi_id){
		return Application.kpiMap.get(kpi_id).getThreshold2();
	}
	
	private String getRelation(int kpi_id){
		return Application.kpiMap.get(kpi_id).getRelation();
	}
	
	private String getRelation1(int kpi_id){
		return Application.kpiMap.get(kpi_id).getRelation1();
	}
	
	private String getRelation2(int kpi_id){
		return Application.kpiMap.get(kpi_id).getRelation2();
	}
	
	/**
	 * return the value of [v1 relation v2] 
	 * @param v1 double
	 * @param v2 double
	 * @param relation 关系运算符(<,<=,>,>=,=,!=)
	 * @return
	 */
	private boolean compareTo(double v1, double v2, String relation){
		boolean result = false;
		if(">".equals(relation)){
			if (v1 > v2) {
				result = true;
			}
		}else if(">=".equals(relation)){
			if (v1 >= v2) {
				result = true;
			}
		}else if("<".equals(relation)){
			if (v1 < v2) {
				result = true;
			}
		}else if("<=".equals(relation)){
			if (v1 <= v2) {
				result = true;
			}
		}else if("=".equals(relation)){
			if (v1 == v2) {
				result = true;
			}
		}else if("!=".equals(relation)){
			if (v1 != v2) {
				result = true;
			}
		}
		
		return result;
	}
	public static void main(String[] args) {
	}
	
}
