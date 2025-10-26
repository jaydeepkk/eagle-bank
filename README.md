# Eagle Bank - Final (OpenAPI aligned)

Run:
```
mvn spring-boot:run
```

OpenAPI UI (swagger):
http://localhost:8080/swagger-ui.html

H2 console:
http://localhost:8080/h2-console (jdbc url: jdbc:h2:mem:eagle)

### Quick curl smoke tests (replace <TOKEN> after login):
# 1) Create user
curl -X POST http://localhost:8080/v1/users -H 'Content-Type: application/json' -d '{
  "name":"Test User",
  "address":{ "line1":"1 St", "town":"Town", "county":"County", "postcode":"AB1 2CD" },
  "phoneNumber":"+447700900000",
  "email":"test@example.com",
  "password":"Pass@123"
}'

# 2) Login to get token
curl -X POST http://localhost:8080/v1/auth/login -H 'Content-Type: application/json' -d '{"email":"test@example.com","password":"Pass@123"}'

# 3) Create account
curl -X POST http://localhost:8080/v1/accounts -H 'Content-Type: application/json' -H 'Authorization: Bearer <TOKEN>' -d '{"name":"Personal","accountType":"personal"}'

# 4) Deposit
curl -X POST http://localhost:8080/v1/accounts/01xxxxxx/transactions -H 'Content-Type: application/json' -H 'Authorization: Bearer <TOKEN>' -d '{"amount":100.5,"currency":"GBP","type":"deposit","reference":"pay"}'

# 5) List transactions
curl -X GET http://localhost:8080/v1/accounts/01xxxxxx/transactions -H 'Authorization: Bearer <TOKEN>'
