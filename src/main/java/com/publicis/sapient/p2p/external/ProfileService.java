package com.publicis.sapient.p2p.external;


import com.publicis.sapient.p2p.model.ServiceResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "profile-service")
public interface ProfileService {

    @GetMapping("/profile-service/profile/public/{userId}")
    ServiceResponseDto getUserDetails(@PathVariable String userId);
}
