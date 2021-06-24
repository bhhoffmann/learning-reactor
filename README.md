# Learning Project Reactor

This repository contains code and examples that illustrates concepts of Project Reactor. It is set up as a Spring Boot
application using the Spring WebFlux web framework, but most of the code examples are plain junit unit tests that can be
run independently (no relation to Spring Boot or WebFlux). Spring WebFlux is an asynchronous non-blocking web framework
created to work seamlessly with Project Reactor, so having a WebFlux application in this repository provides a simple
way to demonstrate a real use case of Project Reactor (not only isolated code snippets in unit tests).

DISCLAIMER: This is purely based on my own experiences and knowledge gained from working with the Project Reactor and
Spring WebFlux. As such, everything in this repository should probably be taken with a grain of salt. I have also
shamelessly stolen some material from various sources online that might not always be properly referenced.

## Lessons introducing the basics of Project Reactor

I have created lessons consisting of code examples and readme-files that aims to introduce and demonstrate the basics of
Project Reactor. Any person completely new to Project Reactor and reactive programming is encouraged to go through these
first. They can be found in the test folder under the package lessons.

## Project Reactor

Official reference documentation: https://projectreactor.io/docs/core/release/reference/

A simplified view: Project Reactor provides a set of classes and methods that lets you write Java code in a style that
makes it easier to solve some types of problems, such as writing asynchronous, concurrent and parallel code. This code
style is very similar (or identical) to functional programming.

## Some online resources

Documentation: https://github.com/reactor/reactor-core/tree/master/docs/asciidoc
Reference Guide: https://projectreactor.io/docs/core/release/reference/index.html
Interactive tutorial: https://tech.io/playgrounds/929/reactive-programming-with-reactor-3/Flux

Blog posts:
https://spring.io/search?q=notes+on+reactive+programming
https://spring.io/blog/2016/07/20/notes-on-reactive-programming-part-iii-a-simple-http-server-application

Reactive Gems
https://github.com/reactor/reactive-streams-commons/issues/21

Flux sharing: https://www.reactiveprogramming.be/project-reactor-flux-sharing/

https://www.youtube.com/watch?v=zls8ZLry68M&t=292s
Don't be Homer...: https://www.youtube.com/watch?v=eE5-dhP44dw
