/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package freenet.support;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Test case for {@link freenet.support.LRUQueue} class.
 * 
 * @author Alberto Bacchelli &lt;sback@freenetproject.org&gt;
 */
public class LRUQueueTest extends TestCase {
	
	private final int sampleElemsNumber = 100;

	/**
	 * Creates an array of objects with a specified size
	 * @param size the array size
	 * @return the objects array
	 */
	private Object[] createSampleObjects(int size) {
		Object[] sampleObjects = new Object[size];
		for (int i=0; i<sampleObjects.length;i++)
			sampleObjects[i] = new Object();
		return sampleObjects;
	}
	
	/**
	 * Creates a LRUQueue filled with the specified objects number
	 * @param size queue size
	 * @return the created LRUQueue
	 */
	private LRUQueue createSampleQueue(int size) {
		LRUQueue methodLRUQueue = new LRUQueue();
		Object[] sampleObjects = createSampleObjects(size);
		for (int i=0;i<sampleObjects.length;i++)
			methodLRUQueue.push(sampleObjects[i]);
		return methodLRUQueue;
	}
	
	/**
	 * Verifies if an element is present in an array
	 * @param anArray the array to search into
	 * @param aElementToSearch the object that must be found
	 * @return true if there is at least one reference to the object
	 */
	private boolean isPresent(Object[] anArray, Object aElementToSearch) {
		for(int i=0; i<anArray.length; i++)
			if (anArray[i].equals(aElementToSearch))
				return true;
		return false;
	}
	
	/**
	 * Tests push(Object) method
	 * providing a null object as arg (after 
	 * setting up a sample queue) 
	 * and verifying if the correct exception
	 * is raised
	 */
	public void testPushNull() {
		LRUQueue methodLRUQueue = this.createSampleQueue(sampleElemsNumber);
		try {
			methodLRUQueue.push(null);
			fail("Expected Exception Error Not Thrown!"); }
		catch (NullPointerException anException) {
			assertNotNull(anException);	}
	}
	
	/**
	 * Tests push(Object) method
	 * and verifies the behaviuor when
	 * pushing the same object more than one
	 * time
	 */
	public void testPushSameObjTwice() {
		LRUQueue methodLRUQueue = new LRUQueue();
		Object sampleObj = new Object();
		methodLRUQueue.push(sampleObj);
		assertTrue(methodLRUQueue.contains(sampleObj));
		assertTrue(methodLRUQueue.size()==1);
		methodLRUQueue.push(sampleObj);			//push the same object again
		
		//assertTrue(methodLRUQueue.size()==2);	//I expect that, if I put the same object twice in 
												//an empty queue, I will have size 2...
		
		//assertEquals(methodLRUQueue.pop(),sampleObj);		
		//assertEquals(methodLRUQueue.pop(),sampleObj);		...and I could pop it twice
	}
	
	/**
	 * Tests pop() method pushing
	 * and popping(?) objects and
	 * verifying if they are correctly (in a FIFO manner)
	 * fetched and deleted
	 */
	public void testPop() {
		LRUQueue methodLRUQueue = new LRUQueue();
		Object[] sampleObjects = createSampleObjects(sampleElemsNumber);
		for (int i=0; i<sampleObjects.length; i++)		//pushing objects
			methodLRUQueue.push(sampleObjects[i]);
		for (int i=0; i<sampleObjects.length; i++)		//getting objects
			assertEquals(sampleObjects[i],methodLRUQueue.pop());
		assertNull(methodLRUQueue.pop());				//the queue must be empty
	}

	/**
	 * Tests size() method checking size
	 * when empty, when putting each object
	 * and when popping each object.
	 */
	public void testSize() {
		Object[] sampleObjects = createSampleObjects(sampleElemsNumber);
		LRUQueue methodLRUQueue = new LRUQueue();
		assertTrue(methodLRUQueue.size()==0);
		for (int i=0; i<sampleObjects.length; i++) {		//pushing objects
			methodLRUQueue.push(sampleObjects[i]);
			assertTrue(methodLRUQueue.size()==i+1); }
		for (int i=sampleObjects.length-1; i>=0; i--) {		//getting all objects
			methodLRUQueue.pop();
			assertTrue(methodLRUQueue.size()==i); }
		assertTrue(methodLRUQueue.size()==0);
	}

	/**
	 * Tests remove(Object) method
	 * verifies if all objects are correctly
	 * removed checking the method retun value,
	 * if the object is still contained and
	 * the queue size.
	 */
	public void testRemove() {
		LRUQueue methodLRUQueue = new LRUQueue();
		Object[] sampleObjects = createSampleObjects(sampleElemsNumber);
		for (int i=0;i<sampleObjects.length;i++)
			methodLRUQueue.push(sampleObjects[i]);
		for(int i=sampleObjects.length-1;i>=0;i--) {			//removing all objects in the opposite way used by pop() method
			assertTrue(methodLRUQueue.remove(sampleObjects[i]));
			assertFalse(methodLRUQueue.contains(sampleObjects[i])); 
			assertTrue(methodLRUQueue.size()==i); }
	}
	
	/**
	 * Tests remove(Object) providing a null
	 * argument and trying to remove it after 
	 * setting up a sample queue.
	 */
	public void testRemoveNull() {
		LRUQueue methodLRUQueue = createSampleQueue(sampleElemsNumber);
		try {
			methodLRUQueue.remove(null);
			fail("Expected Exception Error Not Thrown!"); }
		catch (NullPointerException anException) {
			assertNotNull(anException);	}
	}
	
	/**
	 * Tests remove(Object) method
	 * trying to remove a not present object after 
	 * setting up a sample queue.
	 */
	public void removeNotPresent() {
		LRUQueue methodLRUQueue = createSampleQueue(sampleElemsNumber);
		assertFalse(methodLRUQueue.remove(new Object()));
	}

	/**
	 * Tests contains(Object) method
	 * trying to find a not present object after 
	 * setting up a sample queue.
	 * Then it search a present object.
	 */
	public void testContains() {
		LRUQueue methodLRUQueue = createSampleQueue(sampleElemsNumber);
		assertFalse(methodLRUQueue.contains(new Object()));
		Object methodSampleObj = new Object();
		methodLRUQueue.push(methodSampleObj);
		assertTrue(methodLRUQueue.contains(methodSampleObj));
	}

	public void testElements() {
		//fail("Not yet implemented");
	}

	/**
	 * Tests toArray() method
	 * verifying if the array generated has the same object
	 * that are put into the created LRUQueue
	 */
	public void testToArray() {
		LRUQueue methodLRUQueue = new LRUQueue();
		Object[] sampleObjects = createSampleObjects(sampleElemsNumber);
		for (int i=0; i<sampleObjects.length; i++)		//pushing objects
			methodLRUQueue.push(sampleObjects[i]);
		Object[] resultingArray = methodLRUQueue.toArray();
		assertTrue(resultingArray.length==sampleObjects.length);
		for(int i=0;i<sampleObjects.length;i++)
			assertTrue(isPresent(resultingArray,sampleObjects[i])); 
	}
	
	/**
	 * Tests toArray() method
	 * when the queue is empty
	 */
	public void testToArrayEmptyQueue() {
		LRUQueue methodLRUQueue = new LRUQueue();
		assertTrue(methodLRUQueue.toArray().length==0);
	}

	/**
	 * Tests isEmpty() method
	 * trying it with an empty queue
	 * and then with a sample queue.
	 */
	public void testIsEmpty() {
		LRUQueue methodLRUQueue = new LRUQueue();
		assertTrue(methodLRUQueue.isEmpty());
		methodLRUQueue = createSampleQueue(sampleElemsNumber);
		assertFalse(methodLRUQueue.isEmpty());
		for(int i=0;i<sampleElemsNumber;i++)		//emptying the queue...
			methodLRUQueue.pop();
		assertTrue(methodLRUQueue.isEmpty());
	}

}
