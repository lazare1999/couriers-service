package com.lazo.couriers;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CouriersApplicationTests {

//    @Test
//    void contextLoads() {
//
//
//
//            try {
//                String url = "http://localhost:8083/authenticate";
//
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//                params.add("username", "foo");
//                params.add("password", "foo");
//                params.add("client_secret", "r+*!v*!+o0t*(+d7exg4dbz(%2oy@(7af*or6nqm*3b)!7*e#@");
//                params.add("grant_type", "password");
//
//                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
//
//                var ans =  new RestTemplate().postForEntity(url, request, String.class).getBody();
//                System.out.println("sss");
//
//            } catch (Exception e){
//                e.printStackTrace();
//            }
//
//
//    }

}
