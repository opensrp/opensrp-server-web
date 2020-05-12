/**
 * 
 */
package org.opensrp.web.rest.shadow;

import org.opensrp.service.ViewConfigurationService;
import org.opensrp.web.rest.ViewConfigurationResource;
import org.springframework.stereotype.Component;


/**
 * @author Samuel Githengi created on 05/11/20
 */
@Component
public class ViewConfigurationResourceShadow extends ViewConfigurationResource {
	
	@Override
	public void setViewConfigurationService(ViewConfigurationService viewConfigurationService) {
		super.setViewConfigurationService(viewConfigurationService);
	}
}
