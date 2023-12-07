package dev.magadiflo.products_service.services;

import dev.magadiflo.products_service.model.dtos.ProductRequest;
import dev.magadiflo.products_service.model.dtos.ProductResponse;

import java.util.List;

public interface IProductService {
    List<ProductResponse> getAllProducts();

    void addProduct(ProductRequest productRequest);
}
