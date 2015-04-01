package com.nsn.ices.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.nsn.ices.common.utils.KpiUtil;
import com.nsn.ices.model.entity.Bts;
import com.nsn.ices.model.entity.BtsGroupData;
import com.nsn.ices.model.entity.Cell;
import com.nsn.ices.model.entity.Kpi;
import com.nsn.ices.model.entity.KpiResult;
import com.nsn.ices.model.entity.Oss;
import com.nsn.ices.model.entity.StartCycleWithEnd;
import com.nsn.ices.model.service.BtsGroupDataService;
import com.nsn.ices.model.service.BtsService;
import com.nsn.ices.model.service.CellService;
import com.nsn.ices.model.service.KpiResultDetailService;
import com.nsn.ices.model.service.KpiService;
import com.nsn.ices.model.service.OmcService;
import com.nsn.ices.model.service.OssService;

/**
 * Schedule tasks class.
 * 
 */
@EnableScheduling
public class TaskScheduler {
	
	@Autowired
	private KpiService kpiService;
	@Autowired
	private CellService cellService;
	@Autowired
	private BtsService btsService;
	@Autowired
	private OssService ossService;
	@Autowired
	private OmcService omcService;
	@Autowired
	private BtsGroupDataService btsGroupDataService;
	@Autowired
	private KpiResultDetailService kpiResultDetailService;
	@Autowired
	private TaskExecutor taskExecutor;

	private static final Logger logger = Logger.getLogger(TaskScheduler.class);
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	/**
	 * we must fixed rate  that is less than 900 seconds,
	 * cause we must reload data from database in case someone changed the threshold value of kpi
	 * 
	 */
	@Scheduled(fixedRate=900000)
	public void getKpi(){
		List<Kpi> list = kpiService.getAllKpi();
		Application.kpiMap.clear();
		Application.kpiNameMap.clear();
		for(Kpi kpi : list){
			Application.kpiMap.put(kpi.getId(), kpi);
			Application.kpiNameMap.put(kpi.getKpi_name_en(), kpi);
		}
	}
	@SuppressWarnings("unchecked")
	@Scheduled(fixedRate=3600000,initialDelay=2000)
	public void getCell(){
		List<Cell> list = cellService.getAllActiveCell();
		Map<Integer, Map<Integer, Cell>> ossCellMap = Application.ossCellMap;
		Map<Integer, Map<Integer, Cell>> btsCellMap = Application.btsCellMap;
		Map<Integer, KpiResult> kpiResultMap15 = Application.kpiResultMap15;
		kpiResultMap15.clear();
		Application.kpiResultMap30.clear();
		Application.kpiResultMap60.clear();
		Application.cellMap.clear();
		ossCellMap.clear();
		btsCellMap.clear();
		for(Cell cell : list){
			Application.cellMap.put(cell.getCell_obj_gid(), cell);
			int oss_id = Application.btsMap.get(cell.getNe_code()).getOss_id();
			int ne_code = cell.getNe_code();
			if(ossCellMap.containsKey(oss_id)){
				ossCellMap.get(oss_id).put(cell.getCell_obj_gid(),cell);
			}else {
				Map<Integer, Cell> btsMap = new HashMap<Integer, Cell>();
				btsMap.put(cell.getCell_obj_gid(),cell);
				ossCellMap.put(oss_id, btsMap);
			}
			if(btsCellMap.containsKey(ne_code)){
				btsCellMap.get(ne_code).put(cell.getCell_obj_gid(),cell);
			}else {
				Map<Integer, Cell> cellMap = new HashMap<Integer, Cell>();
				cellMap.put(cell.getCell_obj_gid(),cell);
				btsCellMap.put(ne_code, cellMap);
			}
			kpiResultMap15.put(cell.getCell_obj_gid(), new KpiResult());
		}
		Application.kpiResultPerDayMap = (Map<Integer, KpiResult>)KpiUtil.deepClone(kpiResultMap15);
		Application.kpiResultMap30 = (Map<Integer, KpiResult>)KpiUtil.deepClone(kpiResultMap15);
		Application.kpiResultMap60 = (Map<Integer, KpiResult>)KpiUtil.deepClone(kpiResultMap15);
	}
	@Scheduled(fixedRate=3600000)
	public void getBts(){
		List<Bts> list = btsService.getAllBts();
		Map<Integer, List<Bts>> ossBtsMap = Application.ossBtsMap;
		Map<Integer, KpiResult> kpiResultAreaMap = Application.kpiResultAreaMap;
		Map<String, KpiResult> kpiResultPerDayCityMap = Application.kpiResultPerDayCityMap;
		ossBtsMap.clear();
		kpiResultAreaMap.clear();
		kpiResultPerDayCityMap.clear();
		for(Bts bts : list){
			Application.btsMap.put(bts.getNe_code(), bts);
			if(ossBtsMap.containsKey(bts.getOss_id())){
				ossBtsMap.get(bts.getOss_id()).add(bts);
			}else {
				List<Bts> btsList = new ArrayList<Bts>();
				btsList.add(bts);
				ossBtsMap.put(bts.getOss_id(), btsList);
			}
			if(!kpiResultAreaMap.containsKey(bts.getRegion_id())){
				kpiResultAreaMap.put(bts.getRegion_id(), new KpiResult());
			}
			if (!kpiResultPerDayCityMap.containsKey(bts.getMODE())) {
				kpiResultPerDayCityMap.put(bts.getMODE(), new KpiResult());
			}
		}
	}
	@Scheduled(fixedRate=3600000)
	public void getBtsGroupData(){
		List<BtsGroupData> list = btsGroupDataService.getAllBtsGroupData();
		Map<Integer, List<BtsGroupData>> btsGroupDataMap = Application.btsGroupDataMap;
		Map<Integer, KpiResult> kpiResultGroupMap = Application.kpiResultGroupMap;
		btsGroupDataMap.clear();
		kpiResultGroupMap.clear();
		for(BtsGroupData btsGroupData : list){
			if(btsGroupDataMap.containsKey(btsGroupData.getGroup_id())){
				btsGroupDataMap.get(btsGroupData.getGroup_id()).add(btsGroupData);
			}else {
				List<BtsGroupData> btsGroupDataList = new ArrayList<BtsGroupData>();
				btsGroupDataList.add(btsGroupData);
				btsGroupDataMap.put(btsGroupData.getGroup_id(), btsGroupDataList);
			}
			if(!kpiResultGroupMap.containsKey(btsGroupData.getGroup_id())){
				kpiResultGroupMap.put(btsGroupData.getGroup_id(), new KpiResult());
			}
		}
	}
	
	@Scheduled(fixedRate=3600000)
	public void getOss(){
		List<Oss> ossList = ossService.getAllOss();
		Application.ossMap.clear();
		for(Oss oss : ossList){
			Application.ossMap.put(oss.getId(), oss);
		}
	}
	
	/**
	 * description: calculated every 15 minutes
	 * cron: second, minute, hour, day of month, month, week
	 * notice: KPI_016_1 is calculated with KPI_013, when we only deal with kpi end with _1,together with kpi end with _2 and _3 if there is any
	 */
	@Scheduled(cron="0 3,18,33,48 * * * *")
	public void calculate(){
		logger.info("Schedule 15 minutes rate tasks begin at " + dateFormat.format(new Date()));
		Application.count15 = 0;
		Map<Integer, Map<Integer, Cell>> ossCellMap = Application.ossCellMap;
		logger.info("ossCellMap.size()="+ossCellMap.size());
		Map<Integer, Kpi> kpiMap = Application.kpiMap;
		Set<Integer> ossSet = ossCellMap.keySet();
		for(final int oss_id : ossSet){
			final StartCycleWithEnd cycle = omcService.getCycle(oss_id,15);
			for(final int kpi_id: kpiMap.keySet()){
				if (!kpiMap.get(kpi_id).getKpi_name_en().endsWith("1") || kpiMap.get(kpi_id).getKpi_name_en().equals("KPI_016_1") || kpiMap.get(kpi_id).getKpi_name_en().equals("KPI_045_1")) {
					continue;
				}
				taskExecutor.execute(new Runnable() {
					public void run() {
						omcService.syncing(oss_id, kpi_id, cycle, 15);
					}
				});
			}
		}
		logger.info("Schedule  15 minutes rate tasks end at " + dateFormat.format(new Date()));
	}
	
	/**
	 * description: calculated half an hour
	 */
	@Scheduled(cron="0 21,51 * * * *")
	public void calculateHalfHour(){
		logger.info("Schedule 30 minutes rate tasks begin at " + dateFormat.format(new Date()));
		Application.count30 = 0;
		Map<Integer, Map<Integer, Cell>> ossCellMap = Application.ossCellMap;
		logger.info("ossCellMap.size()="+ossCellMap.size());
		Map<Integer, Kpi> kpiMap = Application.kpiMap;
		Set<Integer> ossSet = ossCellMap.keySet();
		for(final int oss_id : ossSet){
			final StartCycleWithEnd cycle = omcService.getCycle(oss_id,30);
			for(final int kpi_id: kpiMap.keySet()){
				if (!kpiMap.get(kpi_id).getKpi_name_en().endsWith("1") || kpiMap.get(kpi_id).getKpi_name_en().equals("KPI_016_1") || kpiMap.get(kpi_id).getKpi_name_en().equals("KPI_045_1")) {
					continue;
				}
				taskExecutor.execute(new Runnable() {
					public void run() {
						omcService.syncing(oss_id, kpi_id, cycle, 30);
					}
				});
			}
		}
		logger.info("Schedule  30 minutes rate tasks end at " + dateFormat.format(new Date()));
	}
	
	/**
	 * description: calculated per hour
	 */
	@Scheduled(cron="0 35 * * * *")
	public void calculatePerHour(){
		logger.info("Schedule one hour rate tasks begin at " + dateFormat.format(new Date()));
		Application.count60 = 0;
		Map<Integer, Map<Integer, Cell>> ossCellMap = Application.ossCellMap;
		logger.info("ossCellMap.size()="+ossCellMap.size());
		Map<Integer, Kpi> kpiMap = Application.kpiMap;
		Set<Integer> ossSet = ossCellMap.keySet();
		for(final int oss_id : ossSet){
			final StartCycleWithEnd cycle = omcService.getCycle(oss_id,60);
			for(final int kpi_id: kpiMap.keySet()){
				if (!kpiMap.get(kpi_id).getKpi_name_en().endsWith("1") || kpiMap.get(kpi_id).getKpi_name_en().equals("KPI_016_1") || kpiMap.get(kpi_id).getKpi_name_en().equals("KPI_045_1")) {
					continue;
				}
				taskExecutor.execute(new Runnable() {
					public void run() {
						omcService.syncing(oss_id, kpi_id, cycle, 60);
					}
				});
			}
		}
		logger.info("Schedule one hour rate tasks end at " + dateFormat.format(new Date()));
	}
	
	/**
	 * description: calculated per day
	 */
	@Scheduled(cron="0 6 3 * * *")
	public void calculatePerday(){
		logger.info("Schedule per day rate tasks begin at " + dateFormat.format(new Date()));
		Application.perDayCount = 0;
		Map<Integer, Map<Integer, Cell>> ossCellMap = Application.ossCellMap;
		logger.info("ossCellMap.size()="+ossCellMap.size());
		Map<Integer, Kpi> kpiMap = Application.kpiMap;
		Set<Integer> ossSet = ossCellMap.keySet();
		for(final int oss_id : ossSet){
			final StartCycleWithEnd cycle = omcService.getCycle(oss_id,1);
			for(final int kpi_id: kpiMap.keySet()){
				if (!kpiMap.get(kpi_id).getKpi_name_en().endsWith("1") || kpiMap.get(kpi_id).getKpi_name_en().equals("KPI_016_1") || kpiMap.get(kpi_id).getKpi_name_en().equals("KPI_045_1")) {
					continue;
				}
				taskExecutor.execute(new Runnable() {
					public void run() {
						omcService.syncing(oss_id, kpi_id, cycle, 1);
					}
				});
			}
		}
		logger.info("Schedule per day rate tasks end at " + dateFormat.format(new Date()));
	}
	
	/**
	 * description: calculated per hour group by area code, cause there is no area code in omc
	 *              (or we don't know how to get it), so we calculated it using kpi result detail data
	 */
	@Scheduled(cron="0 37 * * * *")
	public void calculateOneHourByArea(){
		logger.info("Schedule one hour rate tasks by area begin at " + dateFormat.format(new Date()));
		kpiResultDetailService.syncingByArea();
		logger.info("Schedule one hour rate tasks by area end at " + dateFormat.format(new Date()));
	}
	
	/**
	 * description: calculated per day, we don't need group by lncel_id or PERIOD_START_TIME but must divide cells by mode(FDD and TDD) and kpi_id when calculate.
	 */
	@Scheduled(cron="0 8 3 * * *")
	public void calculatePerdayByCity(){
		logger.info("Schedule perday rate tasks by city begin at " + dateFormat.format(new Date()));
		Application.perDayCityCount = 0;
		Map<Integer, Map<Integer, Cell>> ossCellMap = Application.ossCellMap;
		logger.info("ossCellMap.size()="+ossCellMap.size());
		Map<Integer, Kpi> kpiMap = Application.kpiMap;
		Set<Integer> ossSet = ossCellMap.keySet();
		for(final int oss_id : ossSet){
			final String PERIOD_START_TIME = omcService.getPeriodByCity(oss_id);
			final List<StartCycleWithEnd> cycles = omcService.getCycleByCity(oss_id,0);
			for(final int kpi_id: kpiMap.keySet()){
				if (kpi_id <= 45 || !kpiMap.get(kpi_id).getKpi_name_en().endsWith("1") || kpiMap.get(kpi_id).getKpi_name_en().equals("KPI_016_1") || kpi_id >= 79) {
					continue;
				}
				taskExecutor.execute(new Runnable() {
					public void run() {
						omcService.syncingByCity(oss_id, kpi_id, PERIOD_START_TIME, cycles, 1);
					}
				});
			}
		}
		logger.info("Schedule perday rate tasks by city end at " + dateFormat.format(new Date()));
	}
	
	/**
	 * description: calculated on 3:10 of every Friday, we don't need group by lncel_id or PERIOD_START_TIME but must divide cells by mode(FDD and TDD) and kpi_id when calculate.
	 */
	@Scheduled(cron="0 10 3 * * FRI")
	public void calculateWeeklyAvg(){
		logger.info("Schedule weekly tasks by city begin at " + dateFormat.format(new Date()));
		kpiResultDetailService.syncingByCityWeekly();
		logger.info("Schedule weekly tasks by city end at " + dateFormat.format(new Date()));
	}
}

