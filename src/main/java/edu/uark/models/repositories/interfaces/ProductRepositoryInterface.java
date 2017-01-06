package edu.uark.models.repositories.interfaces;

import edu.uark.dataaccess.repository.BaseRepositoryInterface;
import edu.uark.models.entities.ProductEntity;

public interface ProductRepositoryInterface extends BaseRepositoryInterface<ProductEntity> {
	ProductEntity byLookupCode(String lookupCode);
}
