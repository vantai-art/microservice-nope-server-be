package com.rainbowforest.recommendationservice.service;

import com.rainbowforest.recommendationservice.model.Recommendation;
import com.rainbowforest.recommendationservice.repository.RecommendationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Recommendation saveRecommendation(Recommendation recommendation) {
        /*
         * Giải thích: Khi lấy User/Product từ Feign Client, chúng có ID nhưng 
         * chưa nằm trong Persistence Context của Service này. 
         * merge() sẽ kiểm tra:
         * 1. Nếu ID đã có trong DB cục bộ -> Nó sẽ Update thông tin.
         * 2. Nếu ID chưa có -> Nó sẽ Insert mới (nhờ CascadeType ở Model).
         */
        return entityManager.merge(recommendation);
    }

    @Override
    public List<Recommendation> getAllRecommendationByProductName(String productName) {
        return recommendationRepository.findAllRatingByProductName(productName);
    }

    @Override
    public void deleteRecommendation(Long id) {
        recommendationRepository.deleteById(id);
    }

    @Override
    public Recommendation getRecommendationById(Long recommendationId) {
        // Sử dụng findById thay vì getOne để tránh lỗi Lazy Initialization
        return recommendationRepository.findById(recommendationId).orElse(null);
    }
}