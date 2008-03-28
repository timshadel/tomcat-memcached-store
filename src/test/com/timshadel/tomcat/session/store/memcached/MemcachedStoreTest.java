package com.timshadel.tomcat.session.store.memcached;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.apache.catalina.Session;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.PersistentManager;
import org.apache.catalina.session.StandardSession;

public class MemcachedStoreTest extends TestCase {
	MemcachedStore ms;
	StandardSession session;
	PersistentManager persistentManager;

	protected void setUp() throws Exception {
		ms = new MemcachedStore();
		ms.setServers("localhost:11211, localhost:11221");
		StandardContext context = new StandardContext();
		persistentManager = new PersistentManager();
		persistentManager.setContainer(context);
		persistentManager.setStore(ms);
		context.setManager(persistentManager);
		session = (StandardSession) persistentManager.createSession("bob");
	}
	
	public void testFakeSerializable() throws Exception {
		ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
		oos.writeObject(persistentManager.createSession());
	}

	/**
	 * Normal ois.readObject(session) fails on an NPE; must do it this way.
	 */
	public void testStandardSerializable() throws Exception {
		session.setAttribute("tim", new Integer(12));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		session.writeObjectData(oos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		StandardSession localSession = (StandardSession) persistentManager.createEmptySession();
		localSession.readObjectData(ois);
		assertNotNull(localSession);
		assertEquals(12, ((Integer)localSession.getAttribute("tim")).intValue());
	}

	public void testSetServers() {
		assertNotNull(ms);
	}

	public void testSet() throws Exception {
		assertNotNull(ms);
		ms.save(persistentManager.createSession());
	}
	
	public void testGet() throws Exception {
		assertNotNull(ms);
		Session localSession = persistentManager.createSession();
		ms.save(localSession);
		assertNotNull(ms.load(localSession.getId()));
	}
	
	public void testRemove() throws Exception {
		assertNotNull(ms);
		Session localSession = persistentManager.createSession("testRemoveSessionId");
		System.out.println(localSession.getId());
		ms.save(localSession);
		assertNotNull(ms.load(localSession.getId()));
		ms.remove(localSession.getId());
		assertNull(ms.load(localSession.getId()));
	}
	
	public void testClear() throws Exception {
		assertNotNull(ms);
		ms.clear();
	}
	
}
