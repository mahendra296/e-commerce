package com.mestro.enums;

import com.mestro.common.enums.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    CATEGORY_NOT_FOUND("CAT_001", "Category not found"),
    CATEGORY_ALREADY_EXISTS("CAT_002", "Category already exists"),
    CATEGORY_HAS_PRODUCTS("CAT_003", "Category has products"),
    INVALID_CATEGORY_DATA("CAT_004", "Invalid category data"),
    PRODUCT_NOT_FOUND("PRD_001", "Product not found"),
    PRODUCT_ALREADY_EXISTS("PRD_002", "Product already exists"),
    INVALID_PRODUCT_DATA("PRD_003", "Invalid product data"),
    PRODUCT_SKU_EXISTS("PRD_004", "Product SKU already exists"),
    INVENTORY_NOT_FOUND("INV_001", "Inventory not found"),
    INSUFFICIENT_STOCK("INV_002", "Insufficient stock"),
    INVALID_INVENTORY_DATA("INV_003", "Invalid inventory data"),
    IMAGE_NOT_FOUND("IMG_001", "Image not found"),
    INVALID_IMAGE_DATA("IMG_002", "Invalid image data"),
    WAREHOUSE_NOT_FOUND("WHS_001", "Warehouse not found"),
    WAREHOUSE_ALREADY_EXISTS("WHS_002", "Warehouse already exists");

    private final String code;
    private final String message;
}
