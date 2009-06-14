package com.timshadel.tomcat.session.store.memcached;

import java.io.IOException;

import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.session.StandardSession;

public class MemcachedManager extends ManagerBase {
	Store store;

	public int getRejectedSessions() {
		return 0;
	}

	public void load() throws ClassNotFoundException, IOException {

	}

	public void setRejectedSessions(int arg0) {

	}

	public void unload() throws IOException {

	}

    public Session findSession(String sessionId) {
		StandardSession session = null;
		try {
	    	removeSessionById(sessionId);
			session = (StandardSession) store.load(sessionId);
			if (session == null) {
				session = (StandardSession) createEmptySession();
				session.setManager(this);
				session.tellNew();
			}
			session.setId(sessionId);
			session.setManager(this);
			add(session);
			session.activate();
			session.endAccess();
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
		return session;
	}
    
    public void passivateSession(Session session) {
    	if (session == null) return;
    	
    	try {
			((StandardSession) session).passivate();
			store.save(session);
			remove(session);
			session.recycle();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

	public void setStore(Store store) {
		this.store = store;
		store.setManager(this);
	}

    public void removeSessionById(String sessionId) {

        synchronized (sessions) {
            sessions.remove(sessionId);
        }

    }
}
