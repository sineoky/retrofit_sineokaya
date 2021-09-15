package lesson5;

import com.github.javafaker.Faker;
import okhttp3.ResponseBody;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import retrofit2.Response;
import retrofit2.Retrofit;
import lesson5.dto.Product;
import lesson5.enums.CategoryType;
import lesson5.dto.Category;
import lesson5.service.CategoryService;
import lesson5.service.ProductService;
import lesson5.utils.RetrofitUtils;
import lesson5.EndPoints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static lesson5.EndPoints.wrongProductId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProductTests {

    Integer productId;

    static Retrofit client;
    static ProductService productService;
    static CategoryService categoryService;
    Faker faker = new Faker();
    Product product;

    @BeforeAll
    static void beforeAll() {
        client = RetrofitUtils.getRetrofit();
        productService = client.create(ProductService.class);
        categoryService = client.create(CategoryService.class);
    }

    @BeforeEach
    void setUp() throws IOException {
        product = new Product()
                .withTitle(faker.food().dish())
                .withPrice((int) ((Math.random() + 1) * 100))
                .withCategoryTitle(CategoryType.FOOD.getTitle());

        Response<Product> response = productService
                .createProduct(product)
                .execute();
        productId = Objects.requireNonNull(response.body()).getId();
    }


    @Test
    @DisplayName("Return product by ID")
    void getProductByIdPositiveTest() throws IOException {
        Response<Product> response = productService.getProduct(productId)
                .execute();
        assertThat(response.body().getTitle(), equalTo(product.getTitle()));
        assertThat(response.body().getPrice(), equalTo(product.getPrice()));
        assertThat(response.body().getCategoryTitle(), equalTo(product.getCategoryTitle()));
    }

    @Test
    @DisplayName("Return product by wrong ID")
    void getProductByIdNegativeTest() throws IOException {

        Response<Product> response = productService
                .getProduct(wrongProductId)
                .execute();
        assertThat(response.code(), equalTo(404));
    }

    @Test
    @DisplayName("Return list all products")
    void getListAllProductsTest() throws IOException {
        Response<List<Product>> response = productService
                .getProducts()
                .execute();
        assertThat(response.raw(), CoreMatchers.not(equalTo("0")));
    }

    @Test
    @DisplayName("Create a new product")
    void postProductTest() throws IOException {
        Response<Product> response = productService
                .createProduct(product)
                .execute();
        assertThat(response.body().getTitle(), equalTo(product.getTitle()));
        assertThat(response.body().getPrice(), equalTo(product.getPrice()));
        assertThat(response.body().getCategoryTitle(), equalTo(product.getCategoryTitle()));
    }

    @Test
    @DisplayName("Get category by ID")
    void getCategoryByIdTest() throws IOException {
        Integer id = CategoryType.FOOD.getId();
        Response<Category> response = categoryService
                .getCategory(id)
                .execute();
        assertThat(response.body().getTitle(), equalTo(CategoryType.FOOD.getTitle()));
        assertThat(response.body().getId(), equalTo(id));
    }

    @Test
    @DisplayName("Create empty product")
    void createNewProductEmptyFieldsTest() throws IOException {
        Response<Product> response = productService.createProduct(new Product())
                .execute();
        assertThat(response.code(), equalTo(500));
    }


    @Test
    @DisplayName("Delete product")
    void deleteProductTest() throws IOException {
        Response<ResponseBody> response = productService
                .deleteProduct(productId)
                .execute();
        productId = null;
        assertNull(response.errorBody());
    }

    @Test
    @DisplayName("Delete wrong product ID")
    void deleteWrongProductTest() throws IOException {
        Response<ResponseBody> response = productService
                .deleteProduct(wrongProductId)
                .execute();
        assertThat(response.code(), equalTo(500));
    }

    @Test
    @DisplayName("Modify product")
    void updateProductTest() throws IOException {
        Product newProduct = new Product()
                .withId(productId)
                .withCategoryTitle(CategoryType.FOOD.getTitle())
                .withPrice((int) (Math.random() * 1000 + 1))
                .withTitle(faker.food().ingredient());
        Response<Product> response = productService
                .updateProduct(newProduct)
                .execute();
        assertThat(response.body().getId(), equalTo(productId));
        assertThat(response.body().getPrice(), equalTo(newProduct.getPrice()));
        assertThat(response.body().getTitle(), equalTo(newProduct.getTitle()));
        assertThat(response.body().getCategoryTitle(), equalTo(newProduct.getCategoryTitle()));
    }

    @Test
    @DisplayName("Modify product with wrong ID")
    void updateProductTestNegative() throws IOException {
        Product newProduct = new Product()
                .withId(wrongProductId)
                .withCategoryTitle(CategoryType.FOOD.getTitle())
                .withPrice((int) (Math.random() * 1000 + 1))
                .withTitle(faker.food().ingredient());
        Response<Product> response = productService
                .updateProduct(newProduct)
                .execute();
        assertThat(response.code(), equalTo(400));
    }

    @AfterEach
    void tearDown() {
        if (productId != null)
            try {
                Response<ResponseBody> response = productService
                        .deleteProduct(productId)
                        .execute();
                assertThat(response.code(), equalTo(200));
            } catch (IOException e) {

            }
    }

}