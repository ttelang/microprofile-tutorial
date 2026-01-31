package io.microprofile.tutorial.graphql.product;

import org.eclipse.microprofile.graphql.GraphQLException;

/**
 * Custom exception for when a product is not found
 */
public class ProductNotFoundException extends GraphQLException {
    
    public ProductNotFoundException(Long productId) {
        super("Product not found", GraphQLException.ExceptionType.DataFetchingException);
        getExtensions().put("productId", productId);
        getExtensions().put("errorCode", "PRODUCT_NOT_FOUND");
    }
}
