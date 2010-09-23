/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.wikimachine.debug;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.log4j.Logger;

public class MailMemorylLogger extends AbstractLogger {

	private static final Logger log4j = Logger
			.getLogger(MailMemorylLogger.class);

	private static final String DATE_FORMAT_NOW = "yyyy.MM.dd HH:mm:ss";

	private static final String ADDRESS_TO = "mail.logger.system@googlemail.com";

	private static final String ADDRESS_FROM = "i_galkin@rbg.informatik.tu-darmstadt.de";

	/**
	 * @see {@link http 
	 *      ://gnu.gds.tuwien.ac.at/software/classpathx/javamail/javadoc /gnu/
	 *      mail/providers/smtp/package-summary.html}
	 */

	private static final Properties TRANSPORT_PROPERTIES = new Properties() {
		private static final long serialVersionUID = 1L;

		{
			this.put("mail.transport.protocol", "smtp");
			this.put("mail.smtp.host", "mail.rbg.informatik.tu-darmstadt.de");
			this.put("mail.smtp.port", "25");
			this.put("mail.smtp.from", ADDRESS_FROM);
			this.put("mail.smtp.localhost", "tk.informatik.tu-darmstadt.de");
		}
	};

	private static final Session MAIL_SESSION = Session
			.getDefaultInstance(TRANSPORT_PROPERTIES);

	/**
	 * maximal messages count, send a new email if arrived
	 */
	private static final int MESSAGES_MAX = 1000;
	/**
	 * milliseconds, send new email if exceed
	 */
	private static final int LASTSEND_MAX = 1000 * 60 * 60 * 3;

	private String subject;

	private long lastSend;
	private StringBuffer messageBuffer;
	private int messageCount;

	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	protected Message initMessage() throws MessagingException {
		Message msg = new MimeMessage(MAIL_SESSION);
		msg.setFrom(new InternetAddress(ADDRESS_FROM));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(
				ADDRESS_TO));
		msg.setSubject(subject);
		return msg;
	}

	public MailMemorylLogger() {
		subject = "[TIMEMACHINE](" + now() + ")";
		lastSend = 0;
		messageBuffer = new StringBuffer(MESSAGES_MAX * 10);
		messageCount = 0;
	}

	protected void appendRunntimeInfo() {
		messageBuffer.append("local time\t");
		messageBuffer.append(new Date());
		messageBuffer.append("\ttotal memory\t");
		messageBuffer.append(Runtime.getRuntime().totalMemory());
		messageBuffer.append("\tfree memory\t");
		messageBuffer.append(Runtime.getRuntime().freeMemory());
		messageBuffer.append("\t");
	}

	private void send() {
		try {
			Message msg = initMessage();
			msg.setContent(messageBuffer.toString(), "text/plain");
			Transport.send(msg);
		} catch (Exception e) {
			log4j.error("Unable to send message", e);
		}
	}

	// FIXME either setup VM to run finalize on close or create an explicit
	// Logger.close() method
	@Override
	protected void finalize() throws Throwable {
		if (messageBuffer.length() > 0) {
			send();
		}
		super.finalize();
	}

	@Override
	public void logObject(Object message) {
		appendRunntimeInfo();
		messageBuffer.append(message);
		messageBuffer.append("\n");
		long timeStamp = System.currentTimeMillis();
		if (++messageCount > MESSAGES_MAX
				|| (timeStamp - lastSend) > LASTSEND_MAX) {

			send();

			messageCount = 0;
			lastSend = timeStamp;
			messageBuffer.setLength(0);

		}
	}
}
