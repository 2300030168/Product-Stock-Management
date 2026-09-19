# Product & Stock Management Module (Milestone 2)

Spring Boot (Java 17) + React (Vite) + SQL (H2 by default, MySQL optional)

## Run the backend
Requirements: JDK 17+, Maven
```
cd backend
mvn spring-boot:run
```
Runs at http://localhost:8080  (sample products are added on first start)
H2 database console (optional): http://localhost:8080/h2-console
JDBC URL: jdbc:h2:file:./data/inventorydb   User: sa   Password: (empty)

## Run the frontend
Requirements: Node.js 18+
```
cd frontend
npm install
npm run dev
```
Open http://localhost:5173

## Quick API test (Postman / curl)
```
GET    /api/products
POST   /api/products            {"name":"Toor Dal","category":"Grocery","baseUnit":"kg","reorderLevel":10,"openingStock":50,"conversions":[{"unitName":"bag","toBaseQty":25}]}
POST   /api/stock               {"productId":1,"type":"OUT","quantity":2,"unit":"kg"}
POST   /api/stock               {"productId":1,"type":"IN","quantity":5,"unit":"bag","price":1200}
GET    /api/stock/transactions
POST   /api/stock/transactions/{id}/undo
GET    /api/products/low-stock
POST   /api/products/{id}/conversions   {"unitName":"box","toBaseQty":5}
DELETE /api/products/{id}
```
