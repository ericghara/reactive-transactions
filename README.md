## Reactive Transactions With jOOQ

This project is a simple proof of concept and tests for integrating Spring's transaction management with jOOQ.
This project was developed in response to an [unfortunate bug](https://github.com/jOOQ/jOOQ/issues/13802) in jOOQ's 
newly released `TransactionPublisher` that makes it unusable.

The simplest strategy that was found involves using Spring's `ConnectionFactoryUtils` to generate a transaction
aware connection from the standard R2DBC connection pool.  This connection is wrapped with jOOQ's `DSLContext`
before being provided to each jOOQ query.

Room for improvement here could be to create a custom connection factory and provide that to jOOQ.