package com.example.account.server;

import com.example.account.server.entity.Account;
import com.example.account.server.entity.ApiRequestLog;
import com.example.account.server.repository.AccountRepository;
import com.example.account.server.repository.ApiRequestLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:srv;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class AccountControllerTest {

    private static final String URL = "/api/v1/accounts/getAccountList";

    @Autowired MockMvc mvc;
    @Autowired AccountRepository repo;
    @Autowired ApiRequestLogRepository logRepo;

    @BeforeEach
    void init() {
        logRepo.deleteAll();
        repo.deleteAll();
        Account a = new Account();
        a.setAcctNo("100000000001");
        a.setCifNo("CIF001");
        a.setIdNo("ID001");
        a.setAcctType("CA01");
        a.setAcctTitle("NGUYEN VAN A");
        a.setCurrCode("VND");
        a.setBranchCode("HN0001");
        a.setLedgerBalance(new BigDecimal("15000000.50"));
        a.setMinBalance(new BigDecimal("50000.00"));
        a.setAccStatus("0");
        repo.save(a);
    }

    private String body(String reqId, String cif, String id) {
        return "{\"requestId\":\"" + reqId + "\",\"processTime\":\"20261104101500\",\"appCode\":\"APP01\""
                + (cif != null ? ",\"cifNo\":\"" + cif + "\"" : "")
                + (id != null ? ",\"idNo\":\"" + id + "\"" : "") + "}";
    }

    @Test
    void successByCifAndLogged() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body("REQ1", "CIF001", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranStatus").value("0"))
                .andExpect(jsonPath("$.requestId").value("REQ1"))
                .andExpect(jsonPath("$.data[0].acctNo").value("100000000001"))
                .andExpect(jsonPath("$.data[0].ledgerBalance").value("15000000.50"))
                .andExpect(jsonPath("$.errordetails").doesNotExist());

        List<ApiRequestLog> logs = logRepo.findAll();
        assertEquals(1, logs.size());
        ApiRequestLog l = logs.get(0);
        assertEquals("REQ1", l.getRequestId());
        assertEquals("APP01", l.getAppCode());
        assertEquals("APP01|REQ1", l.getDedupKey());
        assertEquals("0", l.getTranStatus());
        assertNull(l.getErrorCode());
        assertNotNull(l.getRequestBody());
        assertNotNull(l.getResponseBody());
        assertNotNull(l.getProcessTimeMs());
    }

    @Test
    void successByIdNo() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body("REQ2", null, "ID001")))
                .andExpect(jsonPath("$.tranStatus").value("0"))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void missingCifAndIdReturnsVal001AndIsLogged() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body("REQ3", null, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranStatus").value("1"))
                .andExpect(jsonPath("$.errordetails.errorCode").value("VAL001"))
                .andExpect(jsonPath("$.errordetails.subErrorCode").doesNotExist());

        ApiRequestLog l = logRepo.findAll().get(0);
        assertEquals("1", l.getTranStatus());
        assertEquals("VAL001", l.getErrorCode());
    }

    @Test
    void badProcessTimeReturnsVal002() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"R\",\"processTime\":\"2026-11-04\",\"appCode\":\"A\",\"cifNo\":\"C\"}"))
                .andExpect(jsonPath("$.tranStatus").value("1"))
                .andExpect(jsonPath("$.errordetails.errorCode").value("VAL002"));
    }

    @Test
    void malformedJsonIsRejectedButRawBodyIsLogged() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranStatus").value("1"))
                .andExpect(jsonPath("$.errordetails.errorCode").value("VAL002"));

        ApiRequestLog l = logRepo.findAll().get(0);
        assertEquals("{not json", l.getRequestBody());
        assertEquals("VAL002", l.getErrorCode());
    }

    @Test
    void duplicateRequestIdIsRejectedAndBothAreLogged() throws Exception {
        String b = body("REQ-DUP", "CIF001", null);
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(b))
                .andExpect(jsonPath("$.tranStatus").value("0"));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(b))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranStatus").value("1"))
                .andExpect(jsonPath("$.errordetails.errorCode").value("DUP001"));

        List<ApiRequestLog> logs = logRepo.findAll();
        assertEquals(2, logs.size());
        long accepted = logs.stream().filter(x -> x.getDedupKey() != null).count();
        assertEquals(1, accepted);
        assertEquals(1, logs.stream().filter(x -> "DUP001".equals(x.getErrorCode())).count());
    }

    @Test
    void sameRequestIdFromDifferentAppIsNotDuplicate() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body("REQ-X", "CIF001", null)))
                .andExpect(jsonPath("$.tranStatus").value("0"));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("REQ-X", "CIF001", null).replace("APP01", "APP02")))
                .andExpect(jsonPath("$.tranStatus").value("0"));
    }
}
