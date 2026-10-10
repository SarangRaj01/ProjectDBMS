1. Product and warehouse management
Store product details, warehouse locations, supplier information and stock quantities.




2. Order processing
Create orders, reserve inventory, handle cancellations and track fulfilment.




3. Consistency and concurrency
Prevent overselling, duplicate reservations, invalid stock updates and partially committed database operations.




4. Audit and reporting
Track stock movements, order history, low-stock products, sales and inventory valuation.




5. Database performance
Use indexes, efficient SQL queries and benchmarks to demonstrate that the application can handle realistic workloads.

Test	                                            Expected outcome
Two simultaneous orders exceed available stock	  *Not all orders can succeed
A transaction fails halfway through	              *No partial database changes remain
An order request is repeated	                  *No duplicate business effect
A warehouse transfer succeeds	                  *Total stock remains conserved
An order is cancelled	                          *Its reservation is released exactly once
An inventory query uses an appropriate index	  *Query plan and execution time can be measured