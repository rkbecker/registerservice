package edu.uark.commands.products;

import org.apache.commons.lang3.StringUtils;

import edu.uark.commands.ResultCommandInterface;
import edu.uark.models.api.Product;
import edu.uark.models.api.enums.ProductApiRequestStatus;
import edu.uark.models.entities.ProductEntity;
import edu.uark.models.repositories.ProductRepository;
import edu.uark.models.repositories.interfaces.ProductRepositoryInterface;

public class ProductByLookupCodeQuery implements ResultCommandInterface<Product> {
	@Override
	public Product execute() {
		if (StringUtils.isBlank(this.lookupCode)) {
			return new Product().setApiRequestStatus(ProductApiRequestStatus.INVALID_INPUT);
		}
		
		ProductEntity productEntity = this.productRepository.byLookupCode(this.lookupCode);
		if (productEntity != null) {
			return new Product(productEntity);
		} else {
			return new Product().setApiRequestStatus(ProductApiRequestStatus.NOT_FOUND);
		}
	}

	//Properties
	private String lookupCode;
	public String getLookupCode() {
		return this.lookupCode;
	}
	public ProductByLookupCodeQuery setLookupCode(String lookupCode) {
		this.lookupCode = lookupCode;
		return this;
	}
	
	private ProductRepositoryInterface productRepository;
	public ProductRepositoryInterface getProductRepository() {
		return this.productRepository;
	}
	public ProductByLookupCodeQuery setProductRepository(ProductRepositoryInterface productRepository) {
		this.productRepository = productRepository;
		return this;
	}
	
	public ProductByLookupCodeQuery() {
		this.productRepository = new ProductRepository();
	}
}
