package edu.uark.commands.products;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import edu.uark.commands.ResultCommandInterface;
import edu.uark.models.api.Product;
import edu.uark.models.api.enums.ProductApiRequestStatus;
import edu.uark.models.entities.ProductEntity;
import edu.uark.models.repositories.ProductRepository;
import edu.uark.models.repositories.interfaces.ProductRepositoryInterface;

public class ProductSaveCommand implements ResultCommandInterface<Product> {
	@Override
	public Product execute() {
		if (StringUtils.isBlank(this.apiProduct.getLookupCode())) {
			return (new Product()).setApiRequestStatus(ProductApiRequestStatus.INVALID_INPUT);
		}
		
		ProductEntity productEntity = this.productRepository.get(this.apiProduct.getId());
		if (productEntity != null) {
			this.apiProduct = productEntity.synchronize(this.apiProduct);
		} else {
			productEntity = this.productRepository.byLookupCode(this.apiProduct.getLookupCode());
			if (productEntity == null) {
				productEntity = new ProductEntity(this.apiProduct);
			} else {
				return (new Product()).setApiRequestStatus(ProductApiRequestStatus.LOOKUP_CODE_ALREADY_EXISTS);
			}
		}
		
		productEntity.save();
		if ((new UUID(0, 0)).equals(this.apiProduct.getId())) {
			this.apiProduct.setId(productEntity.getId());
		}
		
		return this.apiProduct;
	}

	//Properties
	private Product apiProduct;
	public Product getApiProduct() {
		return this.apiProduct;
	}
	public ProductSaveCommand setApiProduct(Product apiProduct) {
		this.apiProduct = apiProduct;
		return this;
	}
	
	private ProductRepositoryInterface productRepository;
	public ProductRepositoryInterface getProductRepository() {
		return this.productRepository;
	}
	public ProductSaveCommand setProductRepository(ProductRepositoryInterface productRepository) {
		this.productRepository = productRepository;
		return this;
	}
	
	public ProductSaveCommand() {
		this.productRepository = new ProductRepository();
	}
}
