package kr.ac.hansung.cse.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.ac.hansung.cse.model.Category;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepository {

    @PersistenceContext
    private EntityManager em;

    public Category save(Category category) {
        em.persist(category);
        return category;
    }

    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(em.find(Category.class, id));
    }

    public List<Category> findAll() {
        return em.createQuery("SELECT c FROM Category c ORDER BY c.id", Category.class)
                .getResultList();
    }

    /**
     * [힌트 ② 반영] 이름으로 카테고리 조회 (JPQL 활용)
     * 이름 중복 검사용으로 사용됩니다.
     */
    public Optional<Category> findByName(String name) {
        List<Category> r = em.createQuery(
                        "SELECT c FROM Category c WHERE c.name = :name", Category.class)
                .setParameter("name", name)
                .getResultList();
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    /**
     * [힌트 ② 반영] 삭제 전 연결 상품 수 확인 (COUNT 쿼리)
     * 상품이 하나라도 있으면 삭제를 막는 비즈니스 로직의 핵심입니다.
     */
    public long countProductsByCategoryId(Long categoryId) {
        return em.createQuery(
                        "SELECT COUNT(p) FROM Product p WHERE p.category.id = :id",
                        Long.class)
                .setParameter("id", categoryId)
                .getSingleResult();
    }

    /**
     * [힌트 ② 반영] 카테고리 삭제
     * em.find로 엔티티를 영속성 컨텍스트에 올린 후 삭제합니다.
     */
    public void delete(Long id) {
        Category c = em.find(Category.class, id);
        if (c != null) {
            em.remove(c);
        }
    }

    /**
     * [추가 기능] JOIN FETCH: N+1 문제 방지
     * 나중에 상품과 함께 카테고리를 조회할 때 성능 최적화를 위해 남겨둡니다.
     */
    public Optional<Category> findByIdWithProducts(Long id) {
        List<Category> result = em.createQuery(
                        "SELECT DISTINCT c FROM Category c JOIN FETCH c.products WHERE c.id = :id",
                        Category.class)
                .setParameter("id", id)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}