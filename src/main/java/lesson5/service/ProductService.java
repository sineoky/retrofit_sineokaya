package lesson5.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import lesson5.dto.Product;

import java.util.ArrayList;
import java.util.List;

public interface ProductService {
    @GET("products")
    Call<List<Product>> getProducts();

    @GET("products/{id}")
    Call<Product> getProduct(@Path("id") Integer id);

    @POST("products")
    Call<Product> createProduct(@Body Product createProductRequest);

    @PUT("products")
    Call<Product> updateProduct(@Body Product updateProductRequest);

    @DELETE("products/{id}")
    Call<ResponseBody> deleteProduct(@Path("id") int id);
}