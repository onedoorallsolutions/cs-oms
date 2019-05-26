package com.cs.oms.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.cs.oms.common.Execution;
import com.cs.oms.common.Instrument;
import com.cs.oms.common.LimitOrder;
import com.cs.oms.common.MarketOrder;
import com.cs.oms.common.Order;
import com.cs.oms.common.OrderBook;
import com.cs.oms.common.OrderBookStatus;
import com.cs.oms.common.OrderStatus;
import com.cs.oms.common.util.db.ConnectionManager;

public class OMSServiceDaoImpl implements OMSServiceDao {
	private final static Logger logger = Logger.getLogger(OMSServiceDaoImpl.class);
	private final ConnectionManager connectionManager;
	private final String INSERT_ORDER_QUERY = "INSERT INTO ORDERS"
			+ "(INSTRUMENT_ID, QUANTITY, ENTRY_TIMESTAMP,ORDER_TYPE,STATUS,ASK_PRICE,EXECUTED_QUANTITY,EXECUTED_PRICE) VALUES"
			+ "(?,?,?,?,?,?,?,?)";

	private final String UPDATE_ORDER_QUERY = "UPDATE ORDERS"
			+ " SET STATUS = ?, EXECUTED_QUANTITY = ? , EXECUTED_PRICE = ? where ID = ?";

	private final String INSERT_EXECUTION_QUERY = "INSERT INTO EXECUTIONS" + "(INSTRUMENT_ID, QUANTITY, PRICE) VALUES"
			+ "(?,?,?)";
	private final String GET_INSTRUMENT_QUERY = "SELECT * FROM INSTRUMENTS WHERE SYMBOL = ?";
	private final String GET_ORDER_QUERY_BY_ID = "SELECT * FROM ORDERS WHERE ID = ?";
	private final String GET_ORDER_QUERY_BY_INSTRUMENT = "SELECT * FROM ORDERS WHERE INSTRUMENT_ID = ?";
	private final String GET_VALID_ORDER_QUERY_BY_INSTRUMENT = "SELECT * FROM ORDERS WHERE INSTRUMENT_ID = ? and STATUS = 0";
	private final String GET_INVALID_ORDER_QUERY_BY_INSTRUMENT = "SELECT * FROM ORDERS WHERE INSTRUMENT_ID = ? and STATUS = 1";
	private final String GET_EXECUTIONS_QUERY_BY_INSTRUMENT = "SELECT * FROM EXECUTIONS WHERE INSTRUMENT_ID = ?";
	private final String GET_ORDER_BOOK_QUERY = "SELECT * FROM ORDERBOOK WHERE INSTRUMENT_ID = ?";
	private final String INSERT_ORDER_BOOK_QUERY = "INSERT INTO ORDERBOOK(INSTRUMENT_ID,BOOK_STATUS) VALUES (?,?)";
	private final String UPDATE_ORDER_BOOK_QUERY = "UPDATE ORDERBOOK SET BOOK_STATUS = ? WHERE INSTRUMENT_ID = ?";

	public OMSServiceDaoImpl(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	@Override
	public boolean updateOrder(Order order) {
		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(UPDATE_ORDER_QUERY)) {
			psmt.setInt(1, order.getStatus() == OrderStatus.VALID ? 0 : 1);
			psmt.setLong(2, order.getExecutedQuantity());
			psmt.setDouble(3, order.getExecutedPrice().doubleValue());
			psmt.setLong(4, order.getId());
			psmt.executeUpdate();
			con.commit();
			logger.info("Update Order Successfully");
			return true;
		} catch (Exception e) {
			logger.error("Error Updating the Order " + e);
		}

		return false;
	}

	@Override
	public boolean createExecution(long instrumentId, long quantity, BigDecimal price) {

		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(INSERT_EXECUTION_QUERY)) {
			psmt.setLong(1, instrumentId);
			psmt.setLong(2, quantity);
			psmt.setDouble(3, price.doubleValue());

			psmt.execute();
			logger.info("Created Execution Successfully");
			return true;
		} catch (SQLException e) {
			logger.error("Error Creating the Execution " + e);
		}

		return false;
	}

	@Override
	public Set<Execution> getExecutions(long instrumentId) {
		Set<Execution> executions = new HashSet<>();
		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(GET_EXECUTIONS_QUERY_BY_INSTRUMENT)) {
			psmt.setLong(1, instrumentId);
			ResultSet rs = psmt.executeQuery();
			while (rs.next()) {
				long id = rs.getLong("ID");
				long quantity = rs.getLong("QUANTITY");
				BigDecimal price = BigDecimal.valueOf(rs.getDouble("PRICE"));
				Execution execution = new Execution(id, instrumentId, quantity, price);
				executions.add(execution);
			}

		} catch (Exception e) {
			logger.error("Error Fetching the Order " + e);
		}
		return executions;
	}

	@Override
	public Set<Order> getOrders(long instrumentId) {
		Set<Order> orders = new HashSet<>();
		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(GET_ORDER_QUERY_BY_INSTRUMENT)) {
			psmt.setLong(1, instrumentId);
			ResultSet rs = psmt.executeQuery();
			Order order = null;
			while (rs.next()) {
				order = getOrderFromResultSet(rs);
				orders.add(order);
			}

		} catch (Exception e) {
			logger.error("Error Fetching the Order " + e);
		}
		return orders;
	}

	@Override
	public Set<Order> getValidOrders(long instrumentId) {
		Set<Order> orders = new HashSet<>();
		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(GET_VALID_ORDER_QUERY_BY_INSTRUMENT)) {
			psmt.setLong(1, instrumentId);
			ResultSet rs = psmt.executeQuery();
			Order order = null;
			while (rs.next()) {
				order = getOrderFromResultSet(rs);
				orders.add(order);
			}

		} catch (Exception e) {
			logger.error("Error Fetching the Order " + e);
		}
		return orders;
	}

	@Override
	public Set<Order> getInvalidOrders(long instrumentId) {
		Set<Order> orders = new HashSet<>();
		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(GET_INVALID_ORDER_QUERY_BY_INSTRUMENT)) {
			psmt.setLong(1, instrumentId);
			ResultSet rs = psmt.executeQuery();
			Order order = null;
			while (rs.next()) {
				order = getOrderFromResultSet(rs);
				orders.add(order);
			}

		} catch (Exception e) {
			logger.error("Error Fetching the Order " + e);
		}
		return orders;
	}

	private Order getOrderFromResultSet(ResultSet rs) throws SQLException {
		Order order;
		long instrumentId = rs.getLong("INSTRUMENT_ID");
		long id = rs.getLong("ID");
		int type = rs.getInt("ORDER_TYPE");
		int status = rs.getInt("STATUS");
		long quantity = rs.getLong("QUANTITY");
		Timestamp timestamp = rs.getTimestamp("ENTRY_TIMESTAMP");
		BigDecimal executedPrice = BigDecimal.valueOf(rs.getDouble("EXECUTED_PRICE"));
		long executedQuantity = rs.getLong("EXECUTED_QUANTITY");

		if (type == LIMIT_ORDER) {
			BigDecimal askPrice = BigDecimal.valueOf(rs.getDouble("ASK_PRICE"));
			order = new LimitOrder(id, instrumentId, quantity, askPrice, Instant.ofEpochMilli(timestamp.getTime()));
		} else {
			order = new MarketOrder(id, instrumentId, quantity, Instant.ofEpochMilli(timestamp.getTime()));
		}
		order.setExecutedQuantity(executedQuantity);
		order.setStatus(status == VALID_ORDER ? OrderStatus.VALID : OrderStatus.INVALID);
		order.setExecutedPrice(executedPrice);
		return order;
	}

	@Override
	public OrderBook getOrderBook(long instrumentId) {
		OrderBook orderBook = null;
		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(GET_ORDER_BOOK_QUERY)) {
			psmt.setLong(1, instrumentId);

			ResultSet rs = psmt.executeQuery();
			if (rs.next()) {
				long id = rs.getLong("INSTRUMENT_ID");
				int status = rs.getInt("BOOK_STATUS");
				OrderBookStatus orderBookStatus;
				switch (status) {
				case BOOK_OPEN:
					orderBookStatus = OrderBookStatus.OPEN;
					break;
				case BOOK_CLOSE:
					orderBookStatus = OrderBookStatus.CLOSE;
					break;
				case BOOK_EXECUTED:
					orderBookStatus = OrderBookStatus.EXECUTED;
					break;
				default:
					throw new RuntimeException("Invalid Book Status");
				}

				orderBook = new OrderBook(id);
				orderBook.setStatus(orderBookStatus);

			}
			logger.info("Fetch Order Book Successfully");
		} catch (Exception e) {
			logger.error("Error in Fetching  the Order Book" + e);
		}
		return orderBook;
	}

	@Override
	public Instrument getInstrument(String symbol) {
		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(GET_INSTRUMENT_QUERY)) {
			psmt.setString(1, symbol);

			ResultSet rs = psmt.executeQuery();
			if (rs.next()) {
				long id = rs.getLong("ID");

				return new Instrument(id, symbol);

			}
			logger.info("Created Order Successfully");
		} catch (Exception e) {
			logger.error("Error Creating the Order " + e);
		}
		return null;

	}

	@Override
	public Order getOrder(long orderId) {
		Order order = null;
		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(GET_ORDER_QUERY_BY_ID)) {
			psmt.setLong(1, orderId);
			ResultSet rs = psmt.executeQuery();
			if (rs.next()) {
				order = getOrderFromResultSet(rs);

			}

		} catch (Exception e) {
			logger.error("Error Fetching the Order " + e);
		}
		return order;
	}

	@Override
	public boolean createOrder(long instrumentId, long quantity, BigDecimal price) {

		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(INSERT_ORDER_QUERY)) {
			psmt.setLong(1, instrumentId);
			psmt.setLong(2, quantity);
			psmt.setTimestamp(3, new Timestamp(Instant.now().toEpochMilli()));
			psmt.setInt(4, LIMIT_ORDER);
			psmt.setInt(5, VALID_ORDER);
			psmt.setDouble(6, price.doubleValue());
			psmt.setLong(7, 0);
			psmt.setDouble(8, BigDecimal.ZERO.doubleValue());
			psmt.execute();
			logger.info("Created Order Successfully");
			return true;
		} catch (Exception e) {
			logger.error("Error Creating the Order " + e);
		}

		return false;
	}

	@Override
	public boolean createOrder(long instrumentId, long quantity) {

		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(INSERT_ORDER_QUERY)) {
			psmt.setLong(1, instrumentId);
			psmt.setLong(2, quantity);
			psmt.setTimestamp(3, new Timestamp(Instant.now().toEpochMilli()));
			psmt.setInt(4, MARKET_ORDER);
			psmt.setInt(5, VALID_ORDER);
			psmt.setDouble(6, BigDecimal.ZERO.doubleValue());
			psmt.setLong(7, 0);
			psmt.setDouble(8, BigDecimal.ZERO.doubleValue());
			psmt.execute();
			logger.info("Created Order Successfully");
			return true;
		} catch (Exception e) {
			logger.error("Error Creating the Order " + e);
			return false;
		}
	}

	@Override
	public boolean updateOrderBook(OrderBook orderBook) {
		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(UPDATE_ORDER_BOOK_QUERY)) {

			switch (orderBook.getStatus()) {
			case CLOSE:
				psmt.setInt(1, BOOK_CLOSE);
				break;
			case EXECUTED:
				psmt.setInt(1, BOOK_EXECUTED);
				break;
			case OPEN:
				psmt.setInt(1, BOOK_OPEN);
				break;
			default:
				break;
			}

			psmt.setLong(2, orderBook.getInstrumentId());
			psmt.executeUpdate();
			logger.info("Updated Order Book Successfully for Instrument:" + orderBook.getInstrumentId());
			return true;
		} catch (Exception e) {
			logger.error("Error Updating the Order Book " + e);
		}
		return false;
	}

	@Override
	public boolean createOrderBook(long instrumentId) {

		try (Connection con = connectionManager.getConnection();
				PreparedStatement psmt = con.prepareStatement(INSERT_ORDER_BOOK_QUERY)) {
			psmt.setLong(1, instrumentId);
			psmt.setInt(2, BOOK_OPEN);
			psmt.execute();
			logger.info("Created Order Book Successfully for Instrument:" + instrumentId);
			return true;
		} catch (SQLException e) {
			logger.error("Error Creating the Order Book " + e);
		}
		return false;
	}

	
}
