package com.cs.oms.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cs.oms.service.dao.OMSServiceDao;

public class OMSService {

	private OMSServiceDao omsServiceDao;

	public OMSService(OMSServiceDao omsServiceDao) {
		this.omsServiceDao = omsServiceDao;
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		StatisticsService statisticsService = new StatisticsService(omsServiceDao);
		scheduledExecutorService.scheduleAtFixedRate(statisticsService, 1, 1, TimeUnit.MINUTES);
	}

	public OMSServiceDao getOmsServiceDao() {
		return omsServiceDao;
	}

}
