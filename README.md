# learning-reactor
This project contains code and examples that illustrates concepts of Project Reactor and Webflux.

DISCLAIMER: This is purely based on my own experiences and knowledge from learning and using the framework for application
development at my place of work. As such, I might write or code things that are in conflict
with how the original designers intended to use the framework. If this is the case I am happy
to take advice and corrections. I have also, without shame, stolen a lot of material from different sources
that might not always be properly referenced.

## Project Reactor
Official reference guide: https://projectreactor.io/docs/core/release/reference:

Project Reactor provides a set of classes and operators that enables you
to write non-blocking and asynchronous code in a functional style.
This greatly simplifies a lot of the challenges that arise when creating
applications that relies heavily on external systems with which they
communicate with through the network. This is a common situation in today's 
popular microservices architecture.

- What is Project Reactor?
- What is Webflux?
- Reactor basics
    - Assembly vs Execution
    - Concurrency agnostic
    - Pipelines
    - Generating data

- Threading model: Reactor run in unit tests vs on Webflux vs on Webflux + MVC (Tomcat)
- Background processing: How to trigger processing that completes in the background.
