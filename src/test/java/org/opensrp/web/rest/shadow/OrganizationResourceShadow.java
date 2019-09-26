/**
 * 
 */
package org.opensrp.web.rest.shadow;

import org.opensrp.service.OrganizationService;
import org.opensrp.web.rest.OrganizationResource;

/**
 * @author Samuel Githengi created on 09/17/19
 */
public class OrganizationResourceShadow extends OrganizationResource {

	@Override
	public void setOrganizationService(OrganizationService organizationService) {
		super.setOrganizationService(organizationService);
	}

}
