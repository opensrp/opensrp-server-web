package org.opensrp.web.controller.it;

import org.junit.Ignore;
import org.opensrp.common.audit.Auditor;
import org.opensrp.web.rest.it.BaseResourceTest;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore
public class AuditMessageControllerIntegrationTest extends BaseResourceTest {

	@Autowired
	Auditor auditor;


/*
	@Test
	public void getAuditMessagesBasedOnPreviousIndex() throws Exception {
		String url = "/audit/messages";
		Auditor.AuditMessageBuilder auditMessageBuilder = new Auditor.AuditMessageBuilder(auditor, AuditMessageType.NORMAL);
		auditMessageBuilder.done();
		auditMessageBuilder = new Auditor.AuditMessageBuilder(auditor, AuditMessageType.SMS);
		auditMessageBuilder.done();

		MvcResult mvcResult = this.mockMvc.perform(get(url + "?previousAuditMessageIndex=1").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(print()).andReturn();

		String responseString = mvcResult.getResponse().getContentAsString();
		JsonNode actualObj = mapper.readTree(responseString);
		Client actualClient = mapper.treeToValue(actualObj, Client.class);

		assertEquals(Auditor.AuditMessageItem.from(auditor.messagesSince(1)), actualClient);
	}*/
}
