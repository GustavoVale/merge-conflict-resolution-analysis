package commnet.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import commnet.model.beans.Event;
import commnet.model.datastructures.EventBinaryTree;

public class EventBinaryTreeTest {

	private EventBinaryTree bt;
	private Calendar c;

	@Before
	public void setup() {
		c = new GregorianCalendar();
	}

	@After
	public void teardown() {
		c = null;
	}

	@Test
	public void testRootBetweenDatesOK() {
		c.set(2018, 1, 23);
		Date msBase = c.getTime();
		c.set(2018, 10, 22);
		Date msMerge = c.getTime();
		bt = createBinaryTree(Position.INBETWEEN);

		List<Event> result = bt.values(msBase, msMerge).get();
		 
		assertEquals("there should be all seven nodes", 7, result.size());
		assertEquals("there should be node 3", new Integer(3), result.get(0).getIdDB());
		assertEquals("there should be node 6", new Integer(6), result.get(1).getIdDB());
		assertEquals("there should be node 7", new Integer(7), result.get(2).getIdDB());
		assertEquals("there should be node 1", new Integer(1), result.get(3).getIdDB());
		assertEquals("there should be node 5", new Integer(5), result.get(4).getIdDB());
		assertEquals("there should be node 4", new Integer(4), result.get(5).getIdDB());
		assertEquals("there should be node 2", new Integer(2), result.get(6).getIdDB());
	}
	
	@Test
	public void testRootBetweenDatesOut() {
		c.set(2017, 11, 31);
		Date msBase = c.getTime();
		c.set(2019, 0, 1);
		Date msMerge = c.getTime();
		bt = createBinaryTree(Position.INBETWEEN);

		List<Event> result = bt.values(msBase, msMerge).get();
		 
		assertEquals("there should be all seven nodes", 7, result.size());
		assertEquals("there should be node 3", new Integer(3), result.get(0).getIdDB());
		assertEquals("there should be node 6", new Integer(6), result.get(1).getIdDB());
		assertEquals("there should be node 7", new Integer(7), result.get(2).getIdDB());
		assertEquals("there should be node 1", new Integer(1), result.get(3).getIdDB());
		assertEquals("there should be node 5", new Integer(5), result.get(4).getIdDB());
		assertEquals("there should be node 4", new Integer(4), result.get(5).getIdDB());
		assertEquals("there should be node 2", new Integer(2), result.get(6).getIdDB());
	}

	@Test
	public void testRootBetweenDateLeftOut() {
		c.set(2017, 10, 1);
		Date msBase = c.getTime();
		c.set(2018, 11, 31);
		Date msMerge = c.getTime();
		bt = createBinaryTree(Position.INBETWEEN);

		List<Event> result = bt.values(msBase, msMerge).get();

		assertEquals("there should be all seven nodes", 7, result.size());
		assertEquals("there should be node 3", new Integer(3), result.get(0).getIdDB());
		assertEquals("there should be node 6", new Integer(6), result.get(1).getIdDB());
		assertEquals("there should be node 7", new Integer(7), result.get(2).getIdDB());
		assertEquals("there should be node 1", new Integer(1), result.get(3).getIdDB());
		assertEquals("there should be node 5", new Integer(5), result.get(4).getIdDB());
		assertEquals("there should be node 4", new Integer(4), result.get(5).getIdDB());
		assertEquals("there should be node 2", new Integer(2), result.get(6).getIdDB());
	}

	@Test
	public void testRootBetweenDateRightOut() {
		c.set(2018, 1, 23);
		Date msBase = c.getTime();
		c.set(2019, 11, 31);
		Date msMerge = c.getTime();
		bt = createBinaryTree(Position.INBETWEEN);

		List<Event> result = bt.values(msBase, msMerge).get();

		assertEquals("there should be all seven nodes", 7, result.size());
		assertEquals("there should be node 3", new Integer(3), result.get(0).getIdDB());
		assertEquals("there should be node 6", new Integer(6), result.get(1).getIdDB());
		assertEquals("there should be node 7", new Integer(7), result.get(2).getIdDB());
		assertEquals("there should be node 1", new Integer(1), result.get(3).getIdDB());
		assertEquals("there should be node 5", new Integer(5), result.get(4).getIdDB());
		assertEquals("there should be node 4", new Integer(4), result.get(5).getIdDB());
		assertEquals("there should be node 2", new Integer(2), result.get(6).getIdDB());
	}

	@Test
	public void testRootLeftMostDatesOK() {
		c.set(2018, 4, 1);
		Date msBase = c.getTime();
		c.set(2018, 10, 22);
		Date msMerge = c.getTime();
		bt = createBinaryTree(Position.LEFTMOST);
		
		List<Event> result = bt.values(msBase, msMerge).get();

		assertEquals("there should be all seven nodes", 4, result.size());
		assertEquals("there should be node 7", new Integer(7), result.get(0).getIdDB());
		assertEquals("there should be node 5", new Integer(5), result.get(1).getIdDB());
		assertEquals("there should be node 4", new Integer(4), result.get(2).getIdDB());
		assertEquals("there should be node 2", new Integer(2), result.get(3).getIdDB());
	}

	@Test
	public void testRootLeftmostDateRightOut() {
		c.set(2018, 4, 1);
		Date msBase = c.getTime();
		c.set(2019, 11, 31);
		Date msMerge = c.getTime();
		bt = createBinaryTree(Position.LEFTMOST);

		List<Event> result = bt.values(msBase, msMerge).get();

		assertEquals("there should be all seven nodes", 4, result.size());
		assertEquals("there should be node 7", new Integer(7), result.get(0).getIdDB());
		assertEquals("there should be node 5", new Integer(5), result.get(1).getIdDB());
		assertEquals("there should be node 4", new Integer(4), result.get(2).getIdDB());
		assertEquals("there should be node 2", new Integer(2), result.get(3).getIdDB());
	}

	@Test
	public void testRootRightMostDatesOK() {
		c.set(2018, 0, 1);
		Date msBase = c.getTime();
		c.set(2018, 9, 31);
		Date msMerge = c.getTime();
		bt = createBinaryTree(Position.RIGHTMOST);
		
		List<Event> result = bt.values(msBase, msMerge).get();
		 
		assertEquals("there should be all seven nodes", 5, result.size());
		assertEquals("there should be node 3", new Integer(3), result.get(0).getIdDB());
		assertEquals("there should be node 6", new Integer(6), result.get(1).getIdDB());
		assertEquals("there should be node 7", new Integer(7), result.get(2).getIdDB());
		assertEquals("there should be node 5", new Integer(5), result.get(3).getIdDB());
		assertEquals("there should be node 4", new Integer(4), result.get(4).getIdDB());
		
	}

	@Test
	public void testRootRightmostDateLeftOut() {
		c.set(2016, 4, 1);
		Date msBase = c.getTime();
		c.set(2018, 9, 31);
		Date msMerge = c.getTime();
		bt = createBinaryTree(Position.RIGHTMOST);

		List<Event> result = bt.values(msBase, msMerge).get();
		 
		assertEquals("there should be all seven nodes", 5, result.size());
		assertEquals("there should be node 3", new Integer(3), result.get(0).getIdDB());
		assertEquals("there should be node 6", new Integer(6), result.get(1).getIdDB());
		assertEquals("there should be node 7", new Integer(7), result.get(2).getIdDB());
		assertEquals("there should be node 5", new Integer(5), result.get(3).getIdDB());
		assertEquals("there should be node 4", new Integer(4), result.get(4).getIdDB());
	}

	private EventBinaryTree createBinaryTree(Position pos) {
		EventBinaryTree bt = new EventBinaryTree();
		Calendar c = new GregorianCalendar();

		// root
		Event one = mock(Event.class);
		switch (pos) {
		case LEFTMOST:
			c.set(2018, 0, 1);
			when(one.toString()).thenReturn("1.Jan.2018");
			break;
		case INBETWEEN:
			c.set(2018, 6, 1);
			when(one.toString()).thenReturn("1.Jul.2018");
			break;
		case RIGHTMOST:
			c.set(2018, 11, 31);
			when(one.toString()).thenReturn("31.Dez.2018");
			break;
		}
		when(one.getCreatedAt()).thenReturn(c.getTime());
		when(one.getIdDB()).thenReturn(1);

		bt.add(one);
		bt.add(getMockEvent(2018, 11, 22, 2));
		bt.add(getMockEvent(2018, 2, 23, 3)); 
		bt.add(getMockEvent(2018, 9, 24, 4));
		bt.add(getMockEvent(2018, 7, 25, 5));
		bt.add(getMockEvent(2018, 3, 26, 6));
		bt.add(getMockEvent(2018, 6, 27, 7));

//		System.out.println(bt);

		return bt;
	}

	private Event getMockEvent(int year, int month, int day, int id) {
		Event mockEvent = mock(Event.class);
		c.set(year, month - 1, day);
		when(mockEvent.getCreatedAt()).thenReturn(c.getTime());
		when(mockEvent.toString()).thenReturn(day + "." + month + "." + year);
		when(mockEvent.getIdDB()).thenReturn(id);
		return mockEvent;
	}

	enum Position {
		LEFTMOST, INBETWEEN, RIGHTMOST
	}
}
