package com.timshadel.tomcat.session.store.memcached;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Manager;
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
		if (!canSkip(uri)) {
			((MemcachedManager) manager).passivateSession(request.getSessionInternal(false));
			System.out.println("passivated session: " + request.getSessionInternal(false));
		} else {
			manager.remove(request.getSessionInternal(false));
			System.out.println("skipped session: " + request.getSessionInternal(false));
		}
	}

	/** see if the uri is something like http://server/myimage.gif?someting */
	private boolean canSkip(String uri) {
		for(String ending : endings) {
			if (uri.contains(ending)) {
				return true;
			}
		}
		return false;
	}
}
