package com.timshadel.tomcat.session.store.memcached;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.catalina.Container;
import org.apache.catalina.Loader;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.session.StoreBase;
import org.apache.catalina.util.CustomObjectInputStream;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

/**
 * Provides a simple implementation of a Tomcat Session
 * {@link Store} that's backed by memcached.
 * @author tim
 */
public class MemcachedStore extends StoreBase {
	
	MemCachedClient mc;
	
	List<String> keys = Collections.synchronizedList(new ArrayList<String>());
	
	List<String> servers = new ArrayList<String>();
	
	public void clear() throws IOException {
		getMemcacheClient().flushAll();
		keys.clear();
	}

	public int getSize() throws IOException {
		return getKeyList().size();
	}

	public String[] keys() throws IOException {
		return getKeyList().toArray(new String[] {});
	}

	public Session load(String sessionId) throws ClassNotFoundException, IOException {
		byte[] bytes = (byte[]) getMemcacheClient().get(sessionId);
		if (bytes == null) return null;
		ObjectInputStream ois = bytesToObjectStream(bytes);
        
		StandardSession session = (StandardSession) manager.createEmptySession();
		session.setManager(manager);
		session.readObjectData(ois);
		if (session.isValid() && !keys.contains(sessionId)) {
			keys.add(sessionId);
		}
		return session;
	}

	private ObjectInputStream bytesToObjectStream(byte[] bytes) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        Container container = manager.getContainer();
        if (container != null)
            loader = container.getLoader();
        if (loader != null)
            classLoader = loader.getClassLoader();
        if (classLoader != null)
            ois = new CustomObjectInputStream(bais, classLoader);
        else
            ois = new ObjectInputStream(bais);
		return ois;
	}

	public void remove(String sessionId) throws IOException {
		getMemcacheClient().delete(sessionId);
		List<String> keyList = getKeyList();
		keyList.remove(sessionId);
	}

	public void save(Session session) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		StandardSession standard = (StandardSession) session;
		standard.writeObjectData(oos);
		getMemcacheClient().set(session.getId(), baos.toByteArray());
		List<String> keyList = getKeyList();
		keyList.add(session.getId());
	}
	
	private List<String> getKeyList() {
		return keys;
	}

	private MemCachedClient getMemcacheClient() {
		if (mc == null) {
			String[] serverlist = servers.toArray(new String[] {});

            SockIOPool pool = SockIOPool.getInstance();
            if (!pool.isInitialized()) {
            	pool.setServers(serverlist);
            	pool.setInitConn(5);
            	pool.initialize();
            }
            
			mc = new MemCachedClient();
		}
		return mc;
	}
	
	public void setServers(String serverList) {
		StringTokenizer st = new StringTokenizer(serverList, ", ");
		servers.clear();
		while (st.hasMoreTokens()) {
			servers.add(st.nextToken());
		}
	}

}