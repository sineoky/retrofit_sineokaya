package lesson6;

import com.github.javafaker.Faker;
import lesson6.db.dao.CategoriesMapper;
import lesson6.db.model.Categories;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;
import retrofit2.Retrofit;
import lesson6.db.dao.ProductsMapper;
import lesson6.dto.Product;
import lesson6.enums.CategoryType;
import lesson6.dto.Category;
import lesson6.service.CategoryService;
import lesson6.service.ProductService;
import lesson6.utils.DbUtils;
import lesson6.utils.RetrofitUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static lesson6.EndPoints.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ProductTests {
    int productId;
    static ProductsMapper productsMapper;
    static Retrofit client;
    static ProductService productService;
    static CategoryService categoryService;
    static CategoriesMapper categoriesMapper;
    Faker faker = new Faker();
    Product product;

    @BeforeAll
    static void beforeAll() {
        client = RetrofitUtils.getRetrofit();
        productService = client.create(ProductService.class);
        categoryService = client.create(CategoryService.class);
        categoriesMapper = DbUtils.getCategoriesMapper();
        productsMapper = DbUtils.getProductsMapper();

    }

    @BeforeEach
    void setUp() {
        product = new Product()
                .withTitle(faker.food().dish())
                .withPrice((int) ((Math.random() + 1) * 100))
                .withCategoryTitle(CategoryType.FOOD.getTitle());

        Response<Product> response = null;
        try {
            response = productService.createProduct(product).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        productId = Objects.requireNonNull(response.body()).getId();

    }

    @Test
    void postProductTest() throws IOException {
        Integer countProductsBefore = DbUtils.countProducts(productsMapper);
        Response<Product> response = productService.createProduct(product).execute();
        Integer countProductsAfter = DbUtils.countProducts(productsMapper);
        assertThat(response.body().getTitle(), equalTo(product.getTitle()));
        assertThat(response.body().getPrice(), equalTo(product.getPrice()));
        assertThat(response.body().getCategoryTitle(), equalTo(product.getCategoryTitle()));
        productId = response.body().getId();
    }


    @Test
    //DisplayName("Return product by ID")
    void getProductByIdPositiveTest() throws IOException {
        Response<Product> response = productService.getProduct(productId)
                .execute();
        assertThat(response.body().getTitle(), equalTo(product.getTitle()));
        assertThat(response.body().getPrice(), equalTo(product.getPrice()));
        assertThat(response.body().getCategoryTitle(), equalTo(product.getCategoryTitle()));
    }

    @Test
    //DisplayName("Return product by wrong ID")
    void getProductByIdNegativeTest() throws IOException {

        Response<Product> response = productService
                .getProduct(wrongProductId)
                .execute();
        assertThat(response.code(), equalTo(404));
    }

    @Test
        //DisplayName("Return product by wrong ID")
    void getProductByZeroIdNegativeTest() throws IOException {

        Response<Product> response = productService
                .getProduct(zeroProductId)
                .execute();
        assertThat(response.code(), equalTo(404));
    }


    @Test
    //DisplayName("Return list all products")
    void getListAllProductsTest() throws IOException {
        Response<List<Product>> response = productService
                .getProducts()
                .execute();
        assertThat(response.raw(), CoreMatchers.not(equalTo("0")));
    }

    @Test
    //DisplayName("Get category by ID")
    void getCategoryByIdTest() throws IOException {
        Integer id = CategoryType.FOOD.getId();
        Response<Category> response = categoryService
                .getCategory(id)
                .execute();
        assertThat(response.body().getTitle(), equalTo(CategoryType.FOOD.getTitle()));
        assertThat(response.body().getId(), equalTo(id));
    }


    @Test
        //DisplayName("Get category by wrongID")
    void getCategoryByWrongIdTest() throws IOException {
        Integer id = wrongCategoryId;
        Response<Category> response = categoryService
                .getCategory(id)
                .execute();
        assertThat(response.code(), equalTo(404));
    }


    @Test
        //DisplayName("Get category by zeroID")
    void getCategoryByZeroIdTest() throws IOException {
        Integer id = zeroCategoryId;
        Response<Category> response = categoryService
                .getCategory(id)
                .execute();
        assertThat(response.code(), equalTo(404));
    }


    @Test
    //DisplayName("Create empty product")
    void createNewProductEmptyFieldsTest() throws IOException {
        Response<Product> response = productService.createProduct(new Product())
                .execute();
        assertThat(response.code(), equalTo(500));
    }


    @Test
    //DisplayName("Delete wrong product ID")
    void deleteWrongProductTest() throws IOException {
        Response<ResponseBody> response = productService
                .deleteProduct(wrongProductId)
                .execute();
        assertThat(response.code(), equalTo(500));
    }

    @Test
    //DisplayName("Modify product")
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
    //DisplayName("Modify product")
    void updateProductWrongCategoryTest() throws IOException {
        Product newProduct = new Product()
                .withId(wrongCategoryId)
                .withCategoryTitle(CategoryType.FOOD.getTitle())
                .withPrice((int) (Math.random() * 1000 + 1))
                .withTitle(faker.food().ingredient());
        Response<Product> response = productService
                .updateProduct(newProduct)
                .execute();
        assertThat(response.code(), equalTo(400));
    }


    @Test
    //DisplayName("Modify product with wrong ID")
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

    @Test
    //DisplayName("Create Product Long Name")
    void createNewProductLongTitleTest() throws IOException {
        Response<Product> response = productService
                .createProduct(new Product().withTitle(faker.lorem().fixedString(3333)))
                .execute();
        Objects.requireNonNull(response.errorBody()).string();
        assertThat(response.code(), equalTo(500));
    }

    @Test
        //DisplayName("Create Product Zero Price")
    void createNewProductZeroPriceTest() throws IOException {
        Response<Product> response = productService
                .createProduct(new Product().withPrice((zeroPrice)))
                .execute();
        Objects.requireNonNull(response.errorBody()).string();
        assertThat(response.code(), equalTo(500));
    }

    @Test
        //DisplayName("Create Product minusPrice")
    void createNewProductMinusPriceTest() throws IOException {
        Response<Product> response = productService
                .createProduct(new Product().withPrice((minusPrice)))
                .execute();
        Objects.requireNonNull(response.errorBody()).string();
        assertThat(response.code(), equalTo(500));
    }

    @AfterEach
    void tearDown() throws IOException {
        Response<ResponseBody> response = productService.deleteProduct(productId).execute();
        assertThat(response.isSuccessful(), is(true));
    }
}