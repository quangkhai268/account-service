package com.example.account.client;

import com.example.account.client.dto.GetAccountListResponse;
import com.example.account.client.entity.ApiCallLog;
import com.example.account.client.repository.AccountInfoRepository;
import com.example.account.client.repository.ApiCallLogRepository;
import com.example.account.client.service.AccountClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:cli;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "account.api.url=http://server/api/v1/accounts/getAccountList"
})
class AccountClientServiceTest {

    @Autowired AccountClientService service;
    @Autowired RestTemplate accountRestTemplate;
    @Autowired ApiCallLogRepository logRepo;
    @Autowired AccountInfoRepository accountRepo;

    MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        accountRepo.deleteAll();
        logRepo.deleteAll();
        server = MockRestServiceServer.createServer(accountRestTemplate);
    }

    private static final String OK_BODY = """
            {"processTime":"20261104101501","requestId":"x","tranStatus":"0","tranDesc":"Success",
             "data":[
               {"acctNo":"100000000001","acctType":"CA01","acctTitle":"NGUYEN VAN A","currCode":"VND",
                "branchCode":"HN0001","ledgerBalance":"15000000.50","minBalance":"50000","accStatus":"0"},
               {"acctNo":"100000000002","acctType":"SA02","acctTitle":"NGUYEN VAN A","currCode":"USD",
                "branchCode":"HN0001","ledgerBalance":"2500.00","minBalance":"10","accStatus":"0"}]}
            """;

    @Test
    void successInsertsLogAndAccounts() {
        server.expect(once(), requestTo("http://server/api/v1/accounts/getAccountList"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.cifNo").value("CIF001"))
                .andExpect(jsonPath("$.idNo").doesNotExist())
                .andRespond(withSuccess(OK_BODY, MediaType.APPLICATION_JSON));

        GetAccountListResponse res = service.getAccountList("CIF001", null);

        assertEquals("0", res.getTranStatus());
        assertEquals(2, accountRepo.count());
        List<ApiCallLog> logs = logRepo.findAll();
        assertEquals(1, logs.size());
        assertEquals(ApiCallLog.SUCCESS, logs.get(0).getStatus());
        assertEquals("0", logs.get(0).getTranStatus());
        assertEquals(200, logs.get(0).getHttpStatus());
        server.verify();
    }

    @Test
    void businessFailureOnlyUpdatesLog() {
        server.expect(once(), requestTo("http://server/api/v1/accounts/getAccountList"))
                .andRespond(withSuccess("""
                        {"processTime":"20261104101501","requestId":"x","tranStatus":"1","tranDesc":"Bat buoc cifNo hoac idNo",
                         "errordetails":{"errorSource":"PMH","errorCode":"VAL001","errorDesc":"x"},"data":[]}
                        """, MediaType.APPLICATION_JSON));

        GetAccountListResponse res = service.getAccountList("CIF001", null);

        assertEquals("1", res.getTranStatus());
        assertEquals(0, accountRepo.count());
        ApiCallLog l = logRepo.findAll().get(0);
        assertEquals(ApiCallLog.FAILED, l.getStatus());
        assertEquals("VAL001", l.getErrorCode());
    }

    @Test
    void timeoutReturnsStatus2AndKeepsLog() {
        server.expect(once(), requestTo("http://server/api/v1/accounts/getAccountList"))
                .andRespond(request -> { throw new SocketTimeoutException("Read timed out"); });

        GetAccountListResponse res = service.getAccountList(null, "ID001");

        assertEquals("2", res.getTranStatus());
        assertEquals(0, accountRepo.count());
        ApiCallLog l = logRepo.findAll().get(0);
        assertEquals(ApiCallLog.TIMEOUT, l.getStatus());
        assertEquals("CLI_TIMEOUT", l.getErrorCode());
        // log request đã được ghi trước khi gọi API
        assertEquals(true, l.getRequestBody().contains("ID001"));
    }

    @Test
    void http500MarksLogFailed() {
        server.expect(once(), requestTo("http://server/api/v1/accounts/getAccountList"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("boom"));

        GetAccountListResponse res = service.getAccountList("CIF001", null);

        assertEquals("1", res.getTranStatus());
        ApiCallLog l = logRepo.findAll().get(0);
        assertEquals(ApiCallLog.FAILED, l.getStatus());
        assertEquals(500, l.getHttpStatus());
    }

    @Test
    void missingBothIdentifiersIsRejectedBeforeAnyLog() {
        assertThrows(IllegalArgumentException.class, () -> service.getAccountList(" ", null));
        assertEquals(0, logRepo.count());
    }
}
