package com.bank.accountmanager;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableTransactionManagement
public class AccountManagerApplication {

    public static void main(String[] args) {
        run(AccountManagerApplication.class, args);
    }

}
