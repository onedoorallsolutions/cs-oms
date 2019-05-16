package com.cs.oms.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cs.oms.common.Instrument;
import com.cs.oms.common.OrderBook;
import com.cs.oms.service.dao.OMSServiceDao;

public class StatisticsService implements Runnable {
	private final static Logger logger = Logger.getLogger(StatisticsService.class);
	private OMSServiceDao omsServiceDao;

	public StatisticsService(OMSServiceDao omsServiceDao) {
		this.omsServiceDao = omsServiceDao;
	}

	@Override
	public void run() {
		List<Instrument> instrumentList = omsServiceDao.getAllInstrument();
		for (Instrument instrument : instrumentList) {
			logger.info("---------------------------------------------");
			logger.info("Statistics for Symbol: " + instrument.getSymbol());
			OrderBook orderBook = omsServiceDao.getOrderBook(instrument.getId());
			if (orderBook != null) {
				logger.info("Order Book Status: " + orderBook.getStatus());
				logger.info("Total Valid Order Quantity: " + orderBook.getValidOrderQuantity());
				logger.info("Total Invalid Order Quantity: " + orderBook.getInvalidOrderQuantity());
				logger.info("Total Executed Quantity: " + orderBook.getExecutedQuantity());
				logger.info("Executed Price: " + orderBook.getExecutionPrice());
				logger.info("Demand Limit Price Statistics");
				Map<Double, Long> map = orderBook.getDemandStatistics();
				map.entrySet().stream().forEach(e -> {
					logger.info(e.getKey() + " - " + e.getValue());
				});
			}
		}

	}

}
