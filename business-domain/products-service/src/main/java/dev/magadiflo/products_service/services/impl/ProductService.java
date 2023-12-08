package dev.magadiflo.products_service.services.impl;

import dev.magadiflo.products_service.mapper.ProductMapper;
import dev.magadiflo.products_service.model.dtos.ProductRequest;
import dev.magadiflo.products_service.model.dtos.ProductResponse;
import dev.magadiflo.products_service.model.entities.Product;
import dev.magadiflo.products_service.repositories.IProductRepository;
import dev.magadiflo.products_service.services.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService implements IProductService {

    private final IProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return this.productRepository.findAll().stream()
                .map(ProductMapper::mapToProductResponse)
                .toList();
    }

    @Override
    @Transactional
    public void addProduct(ProductRequest productRequest) {
        Product product = ProductMapper.mapToProduct(productRequest);
        this.productRepository.save(product);
        log.info("Product added: {}", product);
    }
}
