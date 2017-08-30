package com.floreantpos.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Timestamp;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

import com.floreantpos.main.Application;
import com.floreantpos.model.Terminal;
import com.floreantpos.model.Ticket;
import com.floreantpos.model.dao.RestaurantDAO;
import com.floreantpos.model.dao.TerminalDAO;

public class IntegerIdGenerator implements IdentifierGenerator {
	private final static RestaurantDAO restaurantDAO = RestaurantDAO.getInstance();
	private static int lastGeneratedId;

	@Override
	public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
		Integer generatedId = null;
		try {
			Class<? extends Object> clazz = object.getClass();
			Method method = clazz.getMethod("getId", (Class<?>[]) null);
			if (method != null) {
				Object id = method.invoke(object, (Object[]) null);
				if (id != null) {
					generatedId = (Integer) id;
				}
			}
			else {
				method = clazz.getMethod("getAutoId", (Class<?>[]) null);
				if (method != null) {
					Object id = method.invoke(object, (Object[]) null);
					if (id != null) {
						generatedId = (Integer) id;
					}
				}
			}

		} catch (Exception e) {
		}
		if (generatedId == null) {
			Timestamp timestamp = restaurantDAO.geTimestamp();
			long time = timestamp.getTime();
			generatedId = (int) ((time / 1000L) % Integer.MAX_VALUE);
		}
		if (generatedId == lastGeneratedId) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
			return generate(session, object);
		}
		lastGeneratedId = generatedId;
		if (object instanceof Ticket) {
			Ticket ticket = (Ticket) object;
			Terminal terminal = TerminalDAO.getInstance().get(Application.getInstance().getTerminal().getId());
			if (terminal != null) {
				long sequenceNo = terminal.getNextAvailableSequence();
				ticket.setSequence(sequenceNo);
				terminal.setNextAvailableSequence(++sequenceNo);
				TerminalDAO.getInstance().update(terminal);
			}
		}
		return generatedId;
	}
}
