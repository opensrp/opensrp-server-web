/**
 * 
 */
package org.opensrp.web.listener;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.opensrp.domain.Camp;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 03/19/20
 */
@Component
public class AllCamp {

	public List<Camp> findAllActiveByProvider(String provider) {
		throw new NotImplementedException();
	}

	public List<Camp> findAllActive() {
		throw new NotImplementedException();
	}

	public void updateCamp(Camp camp) {
		throw new NotImplementedException();
		
	}

	public void add(Camp camp) {		
	}
	
}