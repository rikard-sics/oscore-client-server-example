# oscore-client-server-example

OSCORE server and client example code in Java.  

This repository contains a basic implementation of an OSCORE capable client/server based on Californium.

## Overview
Object Security for Constrained RESTful Environments (OSCORE) is an application layer protocol for protecting Constrained Application Protocol (CoAP) messages. OSCORE offers end-to-end protection for endpoints that communicate using CoAP, and HTTP (which can be mapped to CoAP). It is specifically designed to work with constrained nodes and networks. Its specification can be found in [RFC8613](https://datatracker.ietf.org/doc/html/rfc8613).

## Repository contents
The repository contains example code of how to implement a server and client application that can communicate using OSCORE, based on the [Californium](https://github.com/eclipse-californium/californium) Java library (which includes support for OSCORE).

## Detailed overview

The repository contains two applications; OscoreClientExample and OscoreServerExample.

**OscoreClientExample**  
This application provides a demonstration of an example OSCORE client implemented using the Californium library. It first defines the necessary OSCORE security context parameters, and creates an OSCORE security context for secure communication, associating the context with the hostname of the server. Next, it creates a CoAP client which is used to send secure `GET` requests to two resources ("hello" and "time") on a server located at a specified URI (by default localhost).  

The client uses OSCORE for these requests to ensure end-to-end security. Finally, it prints the responses received from the server and then shuts down the client. This client example illustrates how to integrate OSCORE for application-layer protection in CoAP communications, which can be a useful feature for constrained environments such as IoT networks.

**OscoreServerExample**  
This application provides a demonstration of an example OSCORE server using the Californium library. It first defines the necessary OSCORE security context parameters, and creates an OSCORE security context for secure communication. A CoAP server is then created, running on the default CoAP port. The server hosts two resources: "hello" and "time".  

Each resource is protected by OSCORE and respond to a GET request: "hello" responds with the text "Hello World!", while "time" responds with the current time. This demonstrates a simple OSCORE-protected server responding to client requests in a secure manner.
