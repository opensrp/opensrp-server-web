package org.opensrp.web.rest.shadow;

import org.opensrp.service.ManifestService;
import org.opensrp.web.rest.ManifestResource;

public class ManifestResourceShadow extends ManifestResource {
//    public ManifestResourceShadow(ManifestService manifestService) {
  //      super(manifestService);
 //   }

    public void setManifestService(ManifestService manifestService) {
        super.setManifestService(manifestService);
    }
}
