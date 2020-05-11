/**
 * 
 */
package org.opensrp.web.rest.shadow;

import org.opensrp.service.ViewConfigurationService;
import org.opensrp.web.rest.ViewConfigurationResource;


/**
 * @author Samuel Githengi created on 05/11/20
 */
public class ViewConfigurationResourceShadow extends ViewConfigurationResource {
	
	@Override
	public void setViewConfigurationService(ViewConfigurationService viewConfigurationService) {
		super.setViewConfigurationService(viewConfigurationService);
	}
}
