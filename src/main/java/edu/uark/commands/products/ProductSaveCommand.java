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
			System.out.println("Not saving product. Blank lookup code.");
			return (new Product()).setApiRequestStatus(ProductApiRequestStatus.INVALID_INPUT);
		}
		
		ProductEntity productEntity = this.productRepository.get(this.apiProduct.getId());
		if (productEntity != null) {
			System.out.println("Saving existing product. Current count = " + Integer.toString(productEntity.getCount()));
			this.apiProduct = productEntity.synchronize(this.apiProduct);
			System.out.println("Saving existing product. Updated count = " + Integer.toString(productEntity.getCount()));
		} else {
			productEntity = this.productRepository.byLookupCode(this.apiProduct.getLookupCode());
			if (productEntity == null) {
				productEntity = new ProductEntity(this.apiProduct);
				System.out.println("Saving new product. Current count = " + Integer.toString(productEntity.getCount()));
			} else {
				System.out.println("Not saving product. Lookup code already exists.");
				return (new Product()).setApiRequestStatus(ProductApiRequestStatus.LOOKUP_CODE_ALREADY_EXISTS);
			}
		}
		
		productEntity.save();
		if ((new UUID(0, 0)).equals(this.apiProduct.getId())) {
			this.apiProduct.setId(productEntity.getId());
		}
		
		System.out.println("Finishing up.");
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
