package com.gotop.sanqinapp.msg;

import java.util.Map;

public class SanQinClientTest {

    public static void testLogin() {
        SanQinClient client = SanQinClient.getInstance();
        Long userId = 1L;
        String username = "Username_TEST";
        String role = "User";

        boolean result = client.login(userId, username, role);
        System.out.println(result);
    }
    
    public static void testLogout() {
        SanQinClient client = SanQinClient.getInstance();
        Long userId = 1L;
        String username = "Username_TEST";
        String role = "User";

        boolean result = client.logout(userId, username, role);
        System.out.println(result);
    }
    
    public static void testRequest() {
        SanQinClient client = SanQinClient.getInstance();
        Map<String, String> result = client.request(123434L);
        if (result != null) {
            System.out.println();result.get("client_url");
            System.out.println();result.get("url_time");
        }
    }
    
    public void closeSession() {
        SanQinClient client = SanQinClient.getInstance();
        client.setCloseCharListener(new SanQinClient.CloseCharListener() {
            public void onCloseChar() {
                System.out.println("todo close char");
            }
        });
    }
    
    public static void main(String[] args) {
        System.out.println("testLogin");
        testLogin();
        System.out.println("testLogout");
        testLogout();
        SanQinClient client = SanQinClient.getInstance();
        client.closeConnect();
    }
}
