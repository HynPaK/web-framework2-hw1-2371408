package kr.ac.hansung.cse.service;

import kr.ac.hansung.cse.model.Category;
import kr.ac.hansung.cse.model.Product;
import kr.ac.hansung.cse.repository.CategoryRepository;
import kr.ac.hansung.cse.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * ProductService - 비즈니스 로직 계층 (Service Layer)
 * =====================================================================
 */
@Service
@Transactional(readOnly = true) // 클래스 기본값: 읽기 전용 트랜잭션
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * 카테고리 이름(String) → Category 엔티티 변환
     */
    public Category resolveCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) return null;
        return categoryRepository.findByName(categoryName).orElse(null);
    }

    /**
     * 모든 상품 조회
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * ID로 상품 조회
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /* =====================================================================
     * 실습 4.3.2 추가: 검색 서비스 로직
     * readOnly = true 상속: 검색은 읽기 전용 트랜잭션으로 충분합니다.
     * ===================================================================== */

    /**
     * 이름으로 상품 검색 (JPQL LIKE 활용)
     */
    public List<Product> searchByName(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    /**
     * 카테고리 ID로 상품 필터링
     */
    public List<Product> searchByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    /**
     * 새 상품 등록
     */
    @Transactional
    public Product createProduct(Product product) {
        if (product.getPrice() != null && product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("상품 가격은 0 이상이어야 합니다.");
        }
        return productRepository.save(product);
    }

    /**
     * 상품 수정
     */
    @Transactional
    public Product updateProduct(Product product) {
        if (product.getPrice() != null && product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("상품 가격은 0 이상이어야 합니다.");
        }
        return productRepository.update(product);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.delete(id);
    }
}