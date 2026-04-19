package kr.ac.hansung.cse.controller;

import jakarta.validation.Valid;
import kr.ac.hansung.cse.exception.ProductNotFoundException;
import kr.ac.hansung.cse.model.Product;
import kr.ac.hansung.cse.model.ProductForm;
import kr.ac.hansung.cse.service.CategoryService;
import kr.ac.hansung.cse.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * =====================================================================
 * ProductController - 웹 요청 처리 계층 (Controller Layer)
 * =====================================================================
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService; // 4.3 검색창 카테고리 목록 조회를 위해 추가

    // 생성자 주입 방식 (의존성 추가)
    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /products - 상품 목록 조회 및 검색 (4.3.3 수정)
    // ─────────────────────────────────────────────────────────────────

    /**
     * @RequestParam(required = false): 검색 조건이 없을 때(첫 진입 시) null을 허용합니다.
     */
    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String keyword,   // 상품명 검색 키워드
            @RequestParam(required = false) Long categoryId,  // 카테고리 필터 ID
            Model model) {

        List<Product> products;

        // 1. 검색/필터링 로직 분기 (4.3 필수 기능)
        if (keyword != null && !keyword.isBlank()) {
            products = productService.searchByName(keyword);
        } else if (categoryId != null) {
            products = productService.searchByCategory(categoryId);
        } else {
            products = productService.getAllProducts();
        }

        model.addAttribute("products", products);

        // 2. 검색 폼 구성 요소 전달
        // 카테고리 드롭다운 목록을 위해 전체 카테고리를 전달합니다.
        model.addAttribute("categories", categoryService.getAllCategories());

        // 검색 후에도 입력창과 드롭다운 상태를 유지하기 위해 현재 검색 조건을 다시 전달합니다.
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);

        return "productList";
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /products/{id} - 상품 상세 조회
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public String showProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        model.addAttribute("product", product);
        return "productView";
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /products/create - 상품 등록 폼 표시
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        return "productForm";
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /products/create - 상품 등록 처리
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "productForm";
        }

        Product product = productForm.toEntity();
        product.setCategory(productService.resolveCategory(productForm.getCategory()));
        Product savedProduct = productService.createProduct(product);

        redirectAttributes.addFlashAttribute("successMessage",
                "'" + savedProduct.getName() + "' 상품이 성공적으로 등록되었습니다.");

        return "redirect:/products";
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /products/{id}/edit - 상품 수정 폼 표시
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        model.addAttribute("productForm", ProductForm.from(product));
        return "productEditForm";
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /products/{id}/edit - 상품 수정 처리
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "productEditForm";
        }

        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(productForm.getName());
        product.setCategory(productService.resolveCategory(productForm.getCategory()));
        product.setPrice(productForm.getPrice());
        product.setDescription(productForm.getDescription());

        productService.updateProduct(product);

        redirectAttributes.addFlashAttribute("successMessage",
                "'" + product.getName() + "' 상품 정보가 수정되었습니다.");
        return "redirect:/products/" + id;
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /products/{id}/delete - 상품 삭제 처리
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {

        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        String productName = product.getName();
        productService.deleteProduct(id);

        redirectAttributes.addFlashAttribute("successMessage",
                "'" + productName + "' 상품이 삭제되었습니다.");
        return "redirect:/products";
    }
}