# API getAccountList – account-server & account-client

Java 17, Spring Boot 3.3.5, Maven, PostgreSQL.

## Cách chạy
1. Tạo 2 database (ví dụ `account_server`, `account_client`). Chạy `account-server/sql/postgres-schema.sql` rồi `account-server/sql/postgres-data.sql` (5 tài khoản mẫu) trên DB server; chạy `account-client/sql/postgres-schema.sql` trên DB client.
2. Sửa `spring.datasource.*` trong `application.yml` của mỗi project (hoặc đặt biến môi trường `DB_USER`, `DB_PASSWORD`).
3. `cd account-server && mvn spring-boot:run` (cổng 8081)
4. `cd account-client && mvn spring-boot:run` (cổng 8080)
5. Gọi thử: `curl "http://localhost:8080/client/accounts?cifNo=CIF001"`

## API server
`POST http://localhost:8081/api/v1/accounts/getAccountList`

```json
{"requestId":"REQ1","processTime":"20261104101500","appCode":"APP01","cifNo":"CIF001"}
```

Mọi kết quả (kể cả lỗi) đều trả HTTP 200 với `tranStatus` 0/1, lỗi nằm trong `errordetails`.
Thiếu cả `cifNo` và `idNo` → `VAL001`. Các lỗi validate khác → `VAL002`.

## Luồng client
1. Tạo request (`requestId` = UUID, `processTime` = giờ hiện tại, `appCode` lấy từ cấu hình).
2. **Ghi `API_CALL_LOG` (PENDING) và commit ngay** – trước khi gọi API.
3. Gọi API (connect/read timeout cấu hình được).
4. Cập nhật `API_CALL_LOG` (SUCCESS / FAILED / TIMEOUT) và, nếu `tranStatus=0`, insert từng phần tử `data` vào `ACCOUNT_INFO` – cùng một transaction.
   Timeout → `tranStatus=2`; lỗi mạng/HTTP/parse → `tranStatus=1`.

## Log phía server (account-server)
- Bảng `API_REQUEST_LOG`: requestId, appCode, request body, response body, tranStatus, errorCode, thời gian xử lý (ms).
- Ghi bằng transaction `REQUIRES_NEW`: request lỗi validate, JSON sai, trùng requestId hoặc lỗi hệ thống vẫn có log.
- Chống trùng: khóa `appCode|requestId` UNIQUE (cột `DEDUP_KEY`). Gửi lại → `tranStatus=1`, `errorCode=DUP001`.
  Request bị từ chối vì trùng có `DEDUP_KEY = NULL` nên vẫn ghi log được. Unique index cũng chặn đúng khi chạy nhiều pod.

## Triển khai OpenShift
Mỗi project có `Dockerfile` và thư mục `openshift/` (ConfigMap, Secret mẫu, Deployment, Service, Route).
- Secret: `DB_USER`, `DB_PASSWORD`. ConfigMap: `SPRING_DATASOURCE_URL`, `ACCOUNT_API_URL`, `ACCOUNT_API_APPCODE`, timeout.
- `mvn -DskipTests package` -> build image -> `oc apply -f openshift/` (nhớ thay `<NAMESPACE>`, tạo Secret thật bằng `oc create secret`).
