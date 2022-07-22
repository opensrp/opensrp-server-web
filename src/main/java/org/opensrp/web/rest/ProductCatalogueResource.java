package org.opensrp.web.rest;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.domain.Multimedia;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.search.ProductCatalogueSearchBean;
import org.opensrp.service.MultimediaService;
import org.opensrp.service.ProductCatalogueService;
import org.smartregister.domain.ProductCatalogue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/product-catalogue")
public class ProductCatalogueResource {

    private static final String DOWNLOAD_PHOTO_END_POINT = "/multimedia/media/";
    private static final Logger logger = LogManager.getLogger(ProductCatalogueResource.class.toString());
    @Autowired
    private ProductCatalogueService productCatalogueService;
    @Autowired
    private MultimediaService multimediaService;

    @Deprecated
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<ProductCatalogue> getAll(
            @RequestParam(value = "productName", defaultValue = "", required = false) String productName
            , @RequestParam(value = "uniqueId", defaultValue = "0", required = false) Long uniqueId,
            @RequestParam(value = "serverVersion", required = false) String serverVersion) {

        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        Long lastSyncedServerVersion = null;
        if (serverVersion != null) {
            lastSyncedServerVersion = Long.parseLong(serverVersion);
        }

        ProductCatalogueSearchBean productCatalogueSearchBean = new ProductCatalogueSearchBean();
        productCatalogueSearchBean.setProductName(productName);
        productCatalogueSearchBean.setUniqueId(uniqueId);
        productCatalogueSearchBean.setServerVersion(lastSyncedServerVersion);

        return productCatalogueService.getProductCatalogues(productCatalogueSearchBean, baseUrl);

    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, params = "limit")
    public List<ProductCatalogue> getAll(
            @RequestParam(value = "productName", defaultValue = "", required = false) String productName
            , @RequestParam(value = "uniqueId", defaultValue = "0", required = false) Long uniqueId,
            @RequestParam(value = "serverVersion", required = false) String serverVersion,
            @RequestParam(value = "limit") String limit) {
        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        Long lastSyncedServerVersion = 0L;
        if (serverVersion != null) {
            lastSyncedServerVersion = Long.parseLong(serverVersion);
        }

        ProductCatalogueSearchBean productCatalogueSearchBean = new ProductCatalogueSearchBean();
        productCatalogueSearchBean.setProductName(productName);
        productCatalogueSearchBean.setUniqueId(uniqueId);
        productCatalogueSearchBean.setServerVersion(lastSyncedServerVersion);

        if (StringUtils.isBlank(limit)) {
            return productCatalogueService.getProductCatalogues(productCatalogueSearchBean, Integer.MAX_VALUE, baseUrl);
        } else {
            return productCatalogueService
                    .getProductCatalogues(productCatalogueSearchBean, Integer.parseInt(limit), baseUrl);
        }
    }

    @PostMapping(headers = {"Accept=multipart/form-data"})
    public ResponseEntity<String> create(@RequestPart(required = false) MultipartFile file,
                                         @RequestPart ProductCatalogue productCatalogue) {

        try {
            productCatalogueService.add(productCatalogue);
            ProductCatalogue createdProductCatalogue = productCatalogueService
                    .getProductCatalogueByName(productCatalogue.getProductName());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            ProductCatalogue catalogue = productCatalogueService
                    .getProductCatalogueByName(productCatalogue.getProductName());
            if (catalogue != null && file != null) {
                Multimedia multimedia = multimediaService.findByCaseId(String.valueOf(catalogue.getUniqueId()));
                if (multimedia == null) {
                    MultimediaDTO multimediaDTO = new MultimediaDTO(catalogue.getUniqueId().toString(), userName.trim(),
                            file.getContentType().trim(), null, "catalog_image");
                    multimediaDTO.withOriginalFileName(file.getOriginalFilename()).withDateUploaded(new Date());

                    logger.info("Saving multimedia file...");
                    multimediaService.saveFile(multimediaDTO, file.getBytes(), file.getOriginalFilename());
                }
                createdProductCatalogue.setPhotoURL(DOWNLOAD_PHOTO_END_POINT + createdProductCatalogue.getUniqueId());
                productCatalogueService.update(createdProductCatalogue);
            }

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (IOException e) {
            logger.error(
                    String.format("Exception occurred while persisting image of Product Catalogue" + e.getMessage() + e));
            return new ResponseEntity<String>("Exception occurred while persisting image of Product Catalogue",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/{id}", headers = {"Accept=multipart/form-data"})
    public ResponseEntity<String> update(@PathVariable("id") Long uniqueId,
                                         @RequestPart(required = false) MultipartFile file,
                                         @RequestPart ProductCatalogue productCatalogue) {

        try {
            if (file != null) {
                productCatalogue.setPhotoURL(DOWNLOAD_PHOTO_END_POINT + productCatalogue.getUniqueId());
            }

            productCatalogueService.update(productCatalogue);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            ProductCatalogue catalogue = productCatalogueService
                    .getProductCatalogueByName(productCatalogue.getProductName());
            if (catalogue != null && file != null) {
                Multimedia multimedia = multimediaService.findByCaseId(String.valueOf(catalogue.getUniqueId()));
                if (multimedia != null) {
                    multimediaService.deleteMultimedia(multimedia); //remove old image
                }
                MultimediaDTO multimediaDTO = new MultimediaDTO(catalogue.getUniqueId().toString(), userName.trim(),
                        file.getContentType().trim(), null, "catalog_image");
                multimediaDTO.withOriginalFileName(file.getOriginalFilename()).withDateUploaded(new Date());

                logger.info("Saving multimedia file...");
                multimediaService.saveFile(multimediaDTO, file.getBytes(), file.getOriginalFilename());
            }
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (IOException e) {
            logger.error(
                    String.format("Exception occurred while persisting image of Product Catalogue" + e.getMessage() + e));
            return new ResponseEntity<String>("Exception occurred while persisting image of Product Catalogue",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> delete(@PathVariable("id") Long uniqueId) {
        productCatalogueService.deleteProductCatalogueById(uniqueId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @GetMapping(value = "/{id}", produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ProductCatalogue getByUniqueId(@PathVariable("id") Long uniqueId) {
        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return productCatalogueService.getProductCatalogue(uniqueId, baseUrl);
    }
}
