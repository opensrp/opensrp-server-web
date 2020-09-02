package org.opensrp.web.rest;

import org.opensrp.domain.ProductCatalogue;
import org.opensrp.search.ProductCatalogueSearchBean;
import org.opensrp.service.ProductCatalogueService;
import org.opensrp.util.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/product-catalogue")
public class ProductCatalogueResource {

	@Autowired
	private ProductCatalogueService productCatalogueService;

	private static Logger logger = LoggerFactory.getLogger(ProductCatalogueResource.class.toString());

	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<ProductCatalogue>> getAll(
			@RequestParam(value = "productName", defaultValue = "", required = false) String productName
			, @RequestParam(value = "type", defaultValue = "", required = false) String type
			, @RequestParam(value = "uniqueId", defaultValue = "0", required = false) Long uniqueId,
			@RequestParam(value = "serverVersion", required = false) String serverVersion) {

		Long lastSyncedServerVersion = null;
		if (serverVersion != null) {
			lastSyncedServerVersion = Long.parseLong(serverVersion) + 1;
		}

		ProductCatalogueSearchBean productCatalogueSearchBean = new ProductCatalogueSearchBean();
		productCatalogueSearchBean.setProductName(productName);
		productCatalogueSearchBean.setProductType(ProductType.get(type));
		productCatalogueSearchBean.setUniqueId(uniqueId);
		productCatalogueSearchBean.setServerVersion(lastSyncedServerVersion);

		return new ResponseEntity<>(productCatalogueService.getProductCatalogues(productCatalogueSearchBean),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> create(@RequestBody ProductCatalogue productCatalogue) {
		try {
			productCatalogueService.add(productCatalogue);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (IllegalArgumentException e) {
			logger.error(String.format("Exception occurred while adding product catalogue: %s", e.getMessage()));
			return new ResponseEntity<String>("The request contain illegal argument ", HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(value = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> update(@PathVariable("id") Long uniqueId, @RequestBody ProductCatalogue productCatalogue) {
		try {
			productCatalogueService.update(productCatalogue);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (IllegalArgumentException e) {
			logger.error(String.format("Exception occurred while updating product catalogue: %s", e.getMessage()));
			return new ResponseEntity<String>("The request contain illegal argument ", HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping(value = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> delete(@PathVariable("id") Long uniqueId) {
		try {
			productCatalogueService.deleteProductCatalogueById(uniqueId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping(value = "/{id}", produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<ProductCatalogue> getByUniqueId(@PathVariable("id") Long uniqueId) {
		return new ResponseEntity<>(productCatalogueService.getProductCatalogue(uniqueId),
				RestUtils.getJSONUTF8Headers(),
				HttpStatus.OK);
	}
}
