package dev.magadiflo.products_service.mapper;

import dev.magadiflo.products_service.model.dtos.ProductRequest;
import dev.magadiflo.products_service.model.dtos.ProductResponse;
import dev.magadiflo.products_service.model.entities.Product;

public class ProductMapper {

    public static ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(product.getId(), product.getSku(), product.getName(),
                product.getDescription(), product.getPrice(), product.getStatus());
    }

    public static Product mapToProduct(ProductRequest productRequest) {
        return Product.builder()
                .sku(productRequest.sku())
                .name(productRequest.name())
                .description(productRequest.description())
                .price(productRequest.price())
                .status(productRequest.status())
                .build();
    }
}
