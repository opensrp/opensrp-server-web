/**
 * 
 */
package org.opensrp.web.rest.shadow;

import org.opensrp.service.OrganizationService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.rest.OrganizationResource;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 09/17/19
 */
@Component
public class OrganizationResourceShadow extends OrganizationResource {

	@Override
	public void setOrganizationService(OrganizationService organizationService) {
		super.setOrganizationService(organizationService);
	}

	@Override
	public void setPractitionerService(PractitionerService practitionerService) {
		super.setPractitionerService(practitionerService);
	}
}
