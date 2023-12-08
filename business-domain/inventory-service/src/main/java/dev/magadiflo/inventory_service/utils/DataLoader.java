package dev.magadiflo.inventory_service.utils;

import dev.magadiflo.inventory_service.model.entities.Inventory;
import dev.magadiflo.inventory_service.repositories.IInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataLoader implements CommandLineRunner {

    private final IInventoryRepository inventoryRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading data...");
        if (this.inventoryRepository.findAll().isEmpty()) {
            List<Inventory> inventories = List.of(
                    Inventory.builder().sku("000001").quantity(10L).build(),
                    Inventory.builder().sku("000002").quantity(20L).build(),
                    Inventory.builder().sku("000003").quantity(30L).build(),
                    Inventory.builder().sku("000004").quantity(0L).build()
            );
            this.inventoryRepository.saveAll(inventories);
        }
        log.info("Data loaded...");
    }
}