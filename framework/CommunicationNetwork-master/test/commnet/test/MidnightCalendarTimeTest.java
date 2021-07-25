package commnet.test;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import commnet.builder.NetworkBuilder;

/**
 * This class tests the equality of different Calendar objects set to midnight.
 * 
 *
 */
public class MidnightCalendarTimeTest {

	NetworkBuilder builder = new NetworkBuilder(null, null, null, null);
	
	@Test
	public void newInstanceOfDate() {
		Calendar c1 = builder.getMidnightCalendar(new Date());
		Calendar c2 = builder.getMidnightCalendar(new Date());
		assertEquals(c1, c2);
	}

	@Test
	public void newInstanceOfDateWithDelay() throws InterruptedException {
		Date d1 =  new Date();
		Calendar c1 = builder.getMidnightCalendar(d1);
		Date d2 =  new Date();
		Calendar c2 = builder.getMidnightCalendar(d2);
		assertEquals(c1, c2);
	}
	
	@Test
	public void settingMinutesEqual2() {
		Date d = new Date();
		Calendar c1 = builder.getMidnightCalendar(d);
		d.setMinutes(2);
		Calendar c2 = builder.getMidnightCalendar(d);
		assertEquals(c1, c2);
	}

	@Test
	public void settingSecondsEqual2() {
		Date d = new Date();
		Calendar c1 = builder.getMidnightCalendar(d);
		d.setSeconds(2);
		Calendar c2 = builder.getMidnightCalendar(d);
		assertEquals(c1, c2);
	}

	@Test
	public void settingHourEqual2() {
		Date d = new Date();
		Calendar c1 = builder.getMidnightCalendar(d);
		d.setHours(2);
		Calendar c2 = builder.getMidnightCalendar(d);
		assertEquals(c1, c2);
	}
	
	
	@Test
	public void settingHourAndSecondEqual2() {
		Date d = new Date();
		Calendar c1 = builder.getMidnightCalendar(d);
		d.setHours(2);
		d.setSeconds(2);
		Calendar c2 = builder.getMidnightCalendar(d);
		assertEquals(c1, c2);
	}
	
	
	@Test
	public void settingHourMinutesEqual2() {
		Date d = new Date();
		Calendar c1 = builder.getMidnightCalendar(d);
		d.setHours(2);
		d.setMinutes(2);
		Calendar c2 = builder.getMidnightCalendar(d);
		assertEquals(c1, c2);
	}
	
	@Test
	public void settingMinutesAndSecondEqual2() {
		Date d = new Date();
		Calendar c1 = builder.getMidnightCalendar(d);
		d.setMinutes(2);
		d.setSeconds(2);
		Calendar c2 = builder.getMidnightCalendar(d);
		assertEquals(c1, c2);
	}
	
	
	@Test
	public void settingHourMinutesSecondsEqual2() {
		Date d = new Date();
		Calendar c1 = builder.getMidnightCalendar(d);
		d.setHours(2);
		d.setMinutes(2);
		d.setSeconds(2);
		Calendar c2 = builder.getMidnightCalendar(d);
		assertEquals(c1, c2);
	}
}
