package org.opensrp.web.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.opensrp.common.audit.AuditMessage;
import org.opensrp.common.audit.AuditMessageType;
import org.opensrp.common.audit.Auditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonProperty;

@Controller
public class AuditMessageController {
	
	private final Auditor auditor;
	
	@Autowired
	public AuditMessageController(Auditor auditor) {
		this.auditor = auditor;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/audit/messages", produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<AuditMessageItem> getAuditMessages(
	        @RequestParam(value = "previousAuditMessageIndex", defaultValue = "0") long previousIndex) throws IOException {
		List<AuditMessage> messages = auditor.messagesSince(previousIndex);
		return messages.stream().map(auditMessage -> AuditMessageItem.from(auditMessage)).collect(Collectors.toList());
		
	}
	
	protected static class AuditMessageItem {
		
		@JsonProperty
		private final DateTime time;
		
		@JsonProperty
		private final long index;
		
		@JsonProperty
		private final AuditMessageType type;
		
		@JsonProperty
		private final Map<String, String> data;
		
		public AuditMessageItem(DateTime time, long index, AuditMessageType type, Map<String, String> data) {
			this.time = time;
			this.index = index;
			this.type = type;
			this.data = data;
		}
		
		public static AuditMessageItem from(AuditMessage auditMessage) {
			return new AuditMessageItem(auditMessage.time(), auditMessage.index(), auditMessage.type(), auditMessage.data());
		}
	}
}
