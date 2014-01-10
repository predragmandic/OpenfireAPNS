package org.jivesoftware.openfire.plugin;

import org.apache.commons.httpclient.*; 
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

import org.jivesoftware.openfire.MessageRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.util.NotFoundException;

import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class apns implements Plugin, PacketInterceptor {
	
	private static final Logger Log = LoggerFactory.getLogger(apns.class);
	
    private InterceptorManager interceptorManager;
    
    private apnsDBHandler dbManager;
    
    public apns() {
        interceptorManager = InterceptorManager.getInstance();
        dbManager = new apnsDBHandler();       
    }
    
	public void initializePlugin(PluginManager pManager, File pluginDirectory) {		
        //TODO
        interceptorManager.addInterceptor(this);
        
        IQHandler myHandler = new apnsIQHandler();
        IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();       
        iqRouter.addHandler(myHandler);
    }
	
	public void destroyPlugin() {
		//TODO
        interceptorManager.removeInterceptor(this);
    }
	
	public void interceptPacket(Packet packet, Session session, boolean read, boolean processed) throws PacketRejectedException {
		
		if(isValidTargetPacket(packet,read,processed)) {
			Packet original = packet;			
						
			if(original instanceof Message) {
				Message receivedMessage = (Message)original;
				JID targetJID = receivedMessage.getTo();

				String body = receivedMessage.getBody();
				String deviceToken = dbManager.getDeviceToken(targetJID);

				if(deviceToken == null) return;
				
				pushMessage message = new pushMessage(body, 1, "beep.caf", "/usr/share/openfire/certificate.p12", "odeon", false, deviceToken);
				message.start();
				
			}
			
		}
		
	
	}
	
	private boolean isValidTargetPacket(Packet packet, boolean read, boolean processed) {
        return  !processed && read && packet instanceof Message;
    }
}
