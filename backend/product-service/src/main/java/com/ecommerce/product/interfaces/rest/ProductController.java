package com.ecommerce.product.interfaces.rest;

import com.ecommerce.product.application.service.ProductApplicationService;
import com.ecommerce.product.domain.model.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品 REST Controller
 */
@Tag(name = "商品管理", description = "商品的查詢、新增、刪除 API（資料儲存於 MongoDB）")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductApplicationService productService;

    /**
     * 查詢所有上架商品
     */
    @Operation(
        summary = "取得所有上架商品",
        description = "回傳所有狀態為 ACTIVE 的商品列表，結果會從 Redis 快取取得（TTL=10分鐘）"
    )
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }

    /**
     * 根據 ID 查詢商品
     */
    @Operation(summary = "查詢商品詳情", description = "根據 MongoDB ObjectId 取得商品完整資訊")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查詢成功"),
        @ApiResponse(responseCode = "404", description = "商品不存在")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(
            @Parameter(description = "MongoDB ObjectId", example = "65a1b2c3d4e5f6g7h8i9j0k1")
            @PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * 根據分類查詢商品
     */
    @Operation(summary = "依分類查詢", description = "取得指定分類下的所有商品")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(
            @Parameter(description = "商品分類", example = "electronics")
            @PathVariable String category) {
        return ResponseEntity.ok(productService.getByCategory(category));
    }

    /**
     * 關鍵字搜尋商品
     */
    @Operation(
        summary = "關鍵字搜尋",
        description = "整合 Elasticsearch 全文搜尋，支援商品名稱、描述的模糊搜尋"
    )
    @ApiResponse(responseCode = "200", description = "搜尋成功")
    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(
            @Parameter(description = "搜尋關鍵字", example = "iPhone")
            @RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    /**
     * 新增商品
     */
    @Operation(
        summary = "新增商品",
        description = "新增商品到 MongoDB，需要管理員權限（由 API Gateway 驗證）",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                        {
                          "name": "iPhone 16 Pro",
                          "description": "Apple 最新旗艦手機",
                          "price": 35900,
                          "stock": 100,
                          "category": "electronics",
                          "imageUrl": "https://example.com/iphone16.jpg"
                        }
                        """)
            )
        )
    )
    @ApiResponse(responseCode = "200", description = "新增成功")
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    /**
     * 刪除商品
     */
    @Operation(summary = "刪除商品", description = "從 MongoDB 中刪除指定商品（軟刪除，狀態改為 INACTIVE）")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "刪除成功"),
        @ApiResponse(responseCode = "404", description = "商品不存在")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "商品 MongoDB ObjectId", example = "65a1b2c3d4e5f6g7h8i9j0k1")
            @PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 健康檢查
     */
    @Operation(summary = "健康檢查", description = "確認 product-service 是否正常運行")
    @SecurityRequirement(name = "")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("product-service is running");
    }
}
