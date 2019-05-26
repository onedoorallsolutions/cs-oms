package com.cs.oms.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cs.oms.common.Instrument;
import com.cs.oms.common.Order;
import com.cs.oms.common.OrderBook;
import com.cs.oms.common.OrderBookStatus;
import com.cs.oms.common.OrderType;
import com.cs.oms.common.exception.OMSException;
import com.cs.oms.common.util.db.ConnectionManager;
import com.cs.oms.dao.OMSServiceDaoImpl;
import com.ibatis.common.jdbc.ScriptRunner;

public class OMSServiceTest {
	private final static Logger logger = Logger.getLogger(ConnectionManager.class);
	private static OMSService omsService = null;

	@BeforeClass
	public static void setUp() throws IOException {

		try {
			org.h2.tools.Server.createTcpServer().start();
			logger.info("Started H2 DataBase");
		} catch (SQLException e1) {
			logger.error("Exception on starting H2 DataBase " + e1);
		}

		String connectionString = "org.h2.Driver,jdbc:h2:tcp://localhost/mem:test,sa,password";
		ConnectionManager connectionManager = new ConnectionManager(connectionString, 1, 1, 1, Collections.emptyList());
		omsService = new OMSServiceImpl(new OMSServiceDaoImpl(connectionManager));

		try (Connection con = connectionManager.getConnection()) {
			ScriptRunner scriptExecutor = new ScriptRunner(con, false, false);
			Reader reader = new BufferedReader(new FileReader("oms.sql"));
			scriptExecutor.runScript(reader);
		} catch (SQLException e) {
			logger.error("Exception on loading startup scripts " + e);
		}

	}

	@Test
	public void test1() {
		boolean isSuccess = false;
		try {
			isSuccess = omsService.openOrderBook("IBM.N");
			isSuccess = omsService.openOrderBook("APPL.N");
		} catch (OMSException e) {
			isSuccess = false;
		}

		assertTrue(isSuccess);

		try {
			isSuccess = omsService.openOrderBook("IBM.N");
		} catch (OMSException e) {
			isSuccess = false;
		}

		assertFalse(isSuccess);

		try {
			Instrument instrument = omsService.getInstrument("APPL.N");
			OrderBook orderBook = omsService.getOrderBook("APPL.N");
			assertEquals(orderBook.getStatus(), OrderBookStatus.OPEN);
			assertEquals(instrument.getId(), orderBook.getInstrumentId());
		} catch (OMSException e) {

		}
	}

	@Test
	public void test2() {
		boolean isSuccess = false;
		try {
			isSuccess = omsService.addOrder("UBER.N", 100, new BigDecimal(25.0));
		} catch (OMSException e) {
			isSuccess = false;
		}
		assertFalse(isSuccess);

		try {
			isSuccess = omsService.addOrder("IBM.N", 50, new BigDecimal(23.0));
			isSuccess = omsService.addOrder("IBM.N", 100, new BigDecimal(25.0));
			isSuccess = omsService.addOrder("APPL.N", 50, new BigDecimal(26.0));
			isSuccess = omsService.addOrder("APPL.N", 150, new BigDecimal(28.0));
			isSuccess = omsService.addOrder("IBM.N", 130, new BigDecimal(29.0));
			isSuccess = omsService.addOrder("IBM.N", 80);
			isSuccess = omsService.addOrder("APPL.N", 120);
		} catch (OMSException e) {
			isSuccess = false;
		}

		assertTrue(isSuccess);

		try {
			Order order = omsService.getBiggestOrder("IBM.N");
			assertEquals(130, order.getQuantity());
			order = omsService.getSmallestOrder("IBM.N");
			assertEquals(50, order.getQuantity());

			order = omsService.getLatestOrder("APPL.N");
			assertEquals(OrderType.MARKET, order.getOrderType());
			assertEquals(120, order.getQuantity());

			order = omsService.getEarliestOrder("APPL.N");
			assertEquals(50, order.getQuantity());
			assertEquals(OrderType.LIMIT, order.getOrderType());
		} catch (OMSException e) {

		}
	}

	@Test
	public void test3() {
		boolean isSuccess = false;
		try {
			isSuccess = omsService.addExecution("IBM.N", 100, new BigDecimal(25.0));
		} catch (OMSException e) {
			isSuccess = false;
		}
		assertFalse(isSuccess);
	}

	@Test
	public void test4() {
		boolean isSuccess = false;
		try {
			isSuccess = omsService.closeOrderBook("UBER.N");
		} catch (OMSException e) {
			isSuccess = false;
		}
		assertFalse(isSuccess);

		try {
			isSuccess = omsService.closeOrderBook("IBM.N");
			isSuccess = omsService.closeOrderBook("APPL.N");
		} catch (OMSException e) {
			isSuccess = false;
		}
		assertTrue(isSuccess);
	}

	@Test
	public void test5() {
		boolean isSuccess = false;
		try {
			isSuccess = omsService.addExecution("IBM.N", 20, new BigDecimal(25.0));
			isSuccess = omsService.addExecution("IBM.N", 180, new BigDecimal(25.0));
			isSuccess = omsService.addExecution("IBM.N", 110, new BigDecimal(25.0));
			isSuccess = omsService.addExecution("IBM.N", 80, new BigDecimal(25.0));
			isSuccess = omsService.addExecution("APPL.N", 100, new BigDecimal(27.0));
			isSuccess = omsService.addExecution("APPL.N", 80, new BigDecimal(27.0));
			isSuccess = omsService.addExecution("APPL.N", 50, new BigDecimal(27.0));
			isSuccess = omsService.addExecution("APPL.N", 150, new BigDecimal(27.0));
		} catch (OMSException e) {
			isSuccess = false;
		}
		assertTrue(isSuccess);

	}

	@Test
	public void test6() {
		try {
			OrderBook orderBook = omsService.getOrderBook("IBM.N");
			assertEquals(orderBook.getStatus(), OrderBookStatus.EXECUTED);
		} catch (OMSException e) {
		}

		try {
			OrderBook orderBook = omsService.getOrderBook("APPL.N");
			assertEquals(orderBook.getStatus(), OrderBookStatus.EXECUTED);
		} catch (OMSException e) {
		}
	}

	@Test
	public void test7() {
		boolean isSuccess = false;
		try {
			isSuccess = omsService.openOrderBook("IBM.N");
		} catch (OMSException e) {
			isSuccess = false;
		}

		assertFalse(isSuccess);

		try {
			isSuccess = omsService.closeOrderBook("IBM.N");
		} catch (OMSException e) {
			isSuccess = false;
		}

		assertFalse(isSuccess);
	}

	@Test
	public void test8() {
		boolean isSuccess = false;
		try {
			isSuccess = omsService.openOrderBook("IBM.N");
		} catch (OMSException e) {
			isSuccess = false;
		}

		assertFalse(isSuccess);

		try {
			isSuccess = omsService.closeOrderBook("IBM.N");
		} catch (OMSException e) {
			isSuccess = false;
		}

		assertFalse(isSuccess);
	}

	@Test
	public void test9() {
		omsService.printAllStatistics("IBM.N");
		omsService.printAllStatistics("UBER.N");
		omsService.printAllStatistics("APPL.N");
	}

	@AfterClass
	public static void stop() {
		try {
			org.h2.tools.Server.createTcpServer().stop();
			logger.info("Stopped H2 DataBase");
		} catch (SQLException e1) {
			logger.error("Exception on stopping H2 DataBase " + e1);
		}
	}

}
