package org.opensrp.web.rest;

import org.opensrp.domain.ProductCatalogue;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.search.ProductCatalogueSearchBean;
import org.opensrp.service.MultimediaService;
import org.opensrp.service.ProductCatalogueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/product-catalogue")
public class ProductCatalogueResource {

	@Autowired
	private ProductCatalogueService productCatalogueService;

	@Autowired
	private MultimediaService multimediaService;

	private static Logger logger = LoggerFactory.getLogger(ProductCatalogueResource.class.toString());

	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<ProductCatalogue>> getAll(
			@RequestParam(value = "productName", defaultValue = "", required = false) String productName
			, @RequestParam(value = "uniqueId", defaultValue = "0", required = false) Long uniqueId,
			@RequestParam(value = "serverVersion", required = false) String serverVersion) {

		Long lastSyncedServerVersion = null;
		if (serverVersion != null) {
			lastSyncedServerVersion = Long.parseLong(serverVersion) + 1;
		}

		ProductCatalogueSearchBean productCatalogueSearchBean = new ProductCatalogueSearchBean();
		productCatalogueSearchBean.setProductName(productName);
		productCatalogueSearchBean.setUniqueId(uniqueId);
		productCatalogueSearchBean.setServerVersion(lastSyncedServerVersion);

		return new ResponseEntity<>(productCatalogueService.getProductCatalogues(productCatalogueSearchBean),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	@PostMapping(headers = { "Accept=multipart/form-data" })
	public ResponseEntity<String> create(@RequestPart(required = false) MultipartFile file,
			@RequestPart ProductCatalogue productCatalogue) {
		try {
			productCatalogueService.add(productCatalogue);
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String userName = RestUtils.currentUser(authentication).getUsername();
			ProductCatalogue catalogue = productCatalogueService
					.getProductCatalogueByName(productCatalogue.getProductName());
			if (catalogue != null && file != null) {
				MultimediaDTO multimediaDTO = new MultimediaDTO(catalogue.getUniqueId().toString(), userName.trim(),
						file.getContentType().trim(), null, "catalog_image");
				multimediaDTO.withOriginalFileName(file.getOriginalFilename()).withDateUploaded(new Date());

				logger.info("Saving multimedia file...");
				String status = multimediaService.saveFile(multimediaDTO, file.getBytes(), file.getOriginalFilename());
			}

			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (IOException e) {
			logger.error(
					String.format("Exception occurred while persisting image of Product Catalogue: %s", e.getMessage(), e));
			return new ResponseEntity<String>("Exception occurred while persisting image of Product Catalogue",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		catch (IllegalArgumentException e) {
			logger.error(String.format("Exception occurred while adding product catalogue: %s", e.getMessage(), e));
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
