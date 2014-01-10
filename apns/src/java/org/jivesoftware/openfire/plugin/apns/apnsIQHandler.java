package org.jivesoftware.openfire.plugin;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.PacketExtension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class apnsIQHandler extends IQHandler {

	private IQHandlerInfo info;
	
	private apnsDBHandler dbManager;
	
	public apnsIQHandler() {
		super("APNS IQ Handler");
		info = new IQHandlerInfo("query","urn:xmpp:apns");
		dbManager = new apnsDBHandler();
	}
 
	@Override
	public IQHandlerInfo getInfo() {	
		return info;
	}
 	
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		IQ result = IQ.createResultIQ(packet);
		
		JID from = packet.getFrom();
		IQ.Type type = packet.getType();
		
		if (type.equals(IQ.Type.get)) {
			
			Element responseElement = DocumentHelper.createElement(QName.get("query", "urn:xmpp:apns"));
			responseElement.addElement("token").setText(dbManager.getDeviceToken(from));			
			
			result.setChildElement(responseElement);
         
		}
		else if(type.equals(IQ.Type.set))
		{
			Element receivedPacket = packet.getElement();
			
			String token = receivedPacket.element("query").elementText("token");
			if( token.length() == 64 ) {
				if( dbManager.insertDeviceToken(from ,token) ) {
					
					Element responseElement = DocumentHelper.createElement(QName.get("query", "urn:xmpp:apns"));
					responseElement.addElement("token").setText(token);
				
					result.setChildElement(responseElement);
					
				} else {					
					result.setChildElement(packet.getChildElement().createCopy());
					result.setError(PacketError.Condition.internal_server_error);					
				}
			} else {
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.not_acceptable);
			}
		}
		else{
			result.setChildElement(packet.getChildElement().createCopy());
			result.setError(PacketError.Condition.not_acceptable);
		}

		return result;
	}
 
}
