package com.nsn.ices.model.entity;

public class StartCycleWithEnd {

	private String startCycle;
	
	private String endCycle;

	public String getStartCycle() {
		return startCycle;
	}

	public void setStartCycle(String startCycle) {
		this.startCycle = startCycle;
	}

	public String getEndCycle() {
		return endCycle;
	}

	public void setEndCycle(String endCycle) {
		this.endCycle = endCycle;
	}
	
	public String toString(){
		return "[startCycle: "+startCycle+",endCycle:"+endCycle+"]";
	}
}
