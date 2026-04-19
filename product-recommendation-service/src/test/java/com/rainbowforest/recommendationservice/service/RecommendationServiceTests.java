package com.rainbowforest.recommendationservice.service;

import com.rainbowforest.recommendationservice.model.Product;
import com.rainbowforest.recommendationservice.model.Recommendation;
import com.rainbowforest.recommendationservice.model.User;
import com.rainbowforest.recommendationservice.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach; // JUnit 5
import org.junit.jupiter.api.Test; // JUnit 5
import org.junit.jupiter.api.extension.ExtendWith; // JUnit 5
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension; // Mockito cho JUnit 5

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals; // Assertions của JUnit 5
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Thay thế @RunWith(SpringRunner.class)
class RecommendationServiceTests { // JUnit 5 không cần public class

    private final Long RECOMMENDATION_ID = 1L;
    private final Integer RATING = 5;
    private final String PRODUCT_NAME = "testProduct";
    private final String USER_NAME = "testUser";
    private User user;
    private Product product;
    private Recommendation recommendation;
    private List<Recommendation> recommendations;

    @Mock
    private RecommendationRepository repository;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @BeforeEach // Thay thế @Before
    void setUp() {
        user = new User();
        user.setUserName(USER_NAME);
        product = new Product();
        product.setProductName(PRODUCT_NAME);
        recommendation = new Recommendation();
        recommendation.setId(RECOMMENDATION_ID);
        recommendation.setUser(user);
        recommendation.setProduct(product);
        recommendation.setRating(RATING);
        recommendations = new ArrayList<>();
        recommendations.add(recommendation);
    }

    @Test
    void get_all_recommendation_by_product_name_test() {
        // given
        // Lưu ý: Đảm bảo tên method trong repository khớp với method bạn gọi ở Service
        when(repository.findAllRatingByProductName(anyString())).thenReturn(recommendations);

        // when
        List<Recommendation> foundRecommendations = recommendationService.getAllRecommendationByProductName(PRODUCT_NAME);

        // then
        assertEquals(RECOMMENDATION_ID, foundRecommendations.get(0).getId());
        assertEquals(PRODUCT_NAME, foundRecommendations.get(0).getProduct().getProductName());
        assertEquals(USER_NAME, foundRecommendations.get(0).getUser().getUserName());
        
        Mockito.verify(repository, Mockito.times(1)).findAllRatingByProductName(anyString());
        Mockito.verifyNoMoreInteractions(repository);
    }

    @Test
    void save_recommendation_test() {
        // given
        when(repository.save(any(Recommendation.class))).thenReturn(recommendation);

        // when
        Recommendation found = recommendationService.saveRecommendation(recommendation);

        // then
        assertEquals(RECOMMENDATION_ID, found.getId());
        assertEquals(PRODUCT_NAME, found.getProduct().getProductName());
        assertEquals(USER_NAME, found.getUser().getUserName());
        
        Mockito.verify(repository, Mockito.times(1)).save(any(Recommendation.class));
        Mockito.verifyNoMoreInteractions(repository);
    }
}