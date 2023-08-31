package com.publicis.sapient.p2p.external;


import com.publicis.sapient.p2p.model.ServiceResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductService {

    @GetMapping("/product-service/products/pdp/{productId}")
    ServiceResponseDto getPDP(@PathVariable String productId);
}
