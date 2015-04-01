package com.nsn.ices.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class KpiUtil {

	public static Set<String> getStringSetFromMap(Set<Integer> set, int limit) {
		StringBuffer sbBuffer = new StringBuffer();
		int i=0;
		Set<String> resultSet = new HashSet<String>();
		for(Integer cell_id: set){
			sbBuffer.append(cell_id);
			if(i<999){
				sbBuffer.append(",");
			}else{
				resultSet.add(sbBuffer.toString());
				sbBuffer = new StringBuffer();
				i=0;
			}
			i++;
		}
		return resultSet;
	}
	
	/**
	 * get the latest 15 minutes cycle. e.g:2014-07-23 14:15:00
	 * @return
	 * @author: yudq
	 */
	public static String latest15MinutesCycle(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
		String currentTime ;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -15);
		int minutes = calendar.get(Calendar.MINUTE);
		currentTime = dateFormat.format(calendar.getTime());
		if(minutes > 0 && minutes <=15){
			currentTime+=":00:00";
		}else if(minutes > 15 && minutes <=30){
			currentTime+=":15:00";
		}else if(minutes > 30 && minutes <=45){
			currentTime+=":30:00";
		}else if(minutes > 45 && minutes <60){
			currentTime+=":45:00";
		}
		return currentTime;
	}
	
	public static Object deepClone(Object object){
		Object newObject = new Object();
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(object);
			ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
			ObjectInputStream oi = new ObjectInputStream(bi);
			newObject = oi.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return newObject;
	}
	
}
