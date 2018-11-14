package org.opensrp.web.rest.shadow;

import org.opensrp.service.CampaignService;
import org.opensrp.web.rest.CampaignResource;

public class CampaignResourceShadow extends CampaignResource {
	
	@Override
	public void setCampaignService(CampaignService campaignService) {
		super.setCampaignService(campaignService);
	}

}
