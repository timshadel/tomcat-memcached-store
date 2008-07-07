package com.timshadel.tomcat.session.store.memcached;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

public class AlwaysPassivateValve extends ValveBase {
	
	private static String[] endings = { ".gif", ".js", ".css", ".png", ".jpeg", ".jpg", ".htm", ".html", ".txt" };

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		getNext().invoke(request, response);
		String uri = request.getDecodedRequestURI();
		Manager manager = request.getContext().getManager();
		Session sessionInternal = request.getSessionInternal(false);
		if (sessionInternal == null) {
			System.out.println("Null session not saved.");
			return;
		}
		if (!canSkip(uri)) {
			((MemcachedManager) manager).passivateSession(sessionInternal);
			System.out.println("passivated session: " + sessionInternal);
		} else {
			manager.remove(sessionInternal);
			System.out.println("skipped session: " + sessionInternal);
		}
	}

	/** see if the uri is something like http://server/myimage.gif?something */
	private boolean canSkip(String uri) {
		for(String ending : endings) {
			if (uri.contains(ending)) {
				return true;
			}
		}
		return false;
	}
}
