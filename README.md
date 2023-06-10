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

## OSCORE Client Source Code

```
/*******************************************************************************
 * Copyright (c) 2023 RISE SICS and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Tobias Andersson (RISE SICS)
 *    Rikard Höglund (RISE) rikard.hoglund@ri.se
 *    
 ******************************************************************************/
package se.ri.oscoredemo;

import java.io.IOException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.cose.AlgorithmID;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.californium.elements.util.Bytes;
import org.eclipse.californium.elements.util.StringUtil;
import org.eclipse.californium.oscore.HashMapCtxDB;
import org.eclipse.californium.oscore.OSCoreCoapStackFactory;
import org.eclipse.californium.oscore.OSCoreCtx;
import org.eclipse.californium.oscore.OSException;

/**
 * 
 * Example OSCORE client using the Californium library.
 *
 */
public class OscoreClientExample {

	private final static HashMapCtxDB db = new HashMapCtxDB();
	private final static String uriBase = "coap://localhost";
	private final static String uriHello = "/hello";
	private final static String uriTime = "/time";

	// OSCORE Security Context parameters
	private final static AlgorithmID alg = AlgorithmID.AES_CCM_16_64_128;
	private final static AlgorithmID kdf = AlgorithmID.HKDF_HMAC_SHA_256;
	private final static byte[] masterSecret = StringUtil.hex2ByteArray("0102030405060708090A0B0C0D0E0F10");
	private final static byte[] masterSalt = StringUtil.hex2ByteArray("9e7ca92223786340");
	private final static byte[] idContext = null;
	private final static Integer replayWindowSize = 32;
	private final static byte[] sid = new byte[] { 0x02 };
	private final static byte[] rid = new byte[] { 0x01 };
	private final static int MAX_UNFRAGMENTED_SIZE = 4096;

	public static void main(String[] args) throws OSException, ConnectorException, IOException {
		// Create and set OSCORE Security Context
		OSCoreCtx ctx = new OSCoreCtx(masterSecret, true, alg, sid, rid, kdf, replayWindowSize, masterSalt, idContext,
				MAX_UNFRAGMENTED_SIZE);
		db.addContext(uriBase, ctx);
		OSCoreCoapStackFactory.useAsDefault(db);

		CoapClient c = new CoapClient();

		// Send request to Hello World resource
		Request req = new Request(Code.GET);
		req.setURI(uriBase + uriHello);
		req.getOptions().setOscore(Bytes.EMPTY);
		CoapResponse resp = c.advanced(req);
		System.out.println(Utils.prettyPrint(resp));

		// Send request to Time resource
		req = new Request(Code.GET);
		req.setURI(uriBase + uriTime);
		req.getOptions().setOscore(Bytes.EMPTY);
		resp = c.advanced(req);
		System.out.println(Utils.prettyPrint(resp));

		c.shutdown();
	}

}
```

## OSCORE Server Source Code

```
/*******************************************************************************
 * Copyright (c) 2023 RISE SICS and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Tobias Andersson (RISE SICS)
 *    Rikard Höglund (RISE) rikard.hoglund@ri.se
 *    
 ******************************************************************************/
package se.ri.oscoredemo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.cose.AlgorithmID;
import org.eclipse.californium.elements.util.StringUtil;
import org.eclipse.californium.oscore.HashMapCtxDB;
import org.eclipse.californium.oscore.OSCoreCoapStackFactory;
import org.eclipse.californium.oscore.OSCoreCtx;
import org.eclipse.californium.oscore.OSCoreResource;
import org.eclipse.californium.oscore.OSException;

/**
 * 
 * Example OSCORE server using the Californium library.
 *
 */
public class OscoreServerExample {

	private final static HashMapCtxDB db = new HashMapCtxDB();

	// OSCORE Security Context parameters
	private final static AlgorithmID alg = AlgorithmID.AES_CCM_16_64_128;
	private final static AlgorithmID kdf = AlgorithmID.HKDF_HMAC_SHA_256;
	private final static byte[] masterSecret = StringUtil.hex2ByteArray("0102030405060708090A0B0C0D0E0F10");
	private final static byte[] masterSalt = StringUtil.hex2ByteArray("9e7ca92223786340");
	private final static byte[] idContext = null;
	private final static Integer replayWindowSize = 32;
	private final static byte[] sid = new byte[] { 0x01 };
	private final static byte[] rid = new byte[] { 0x02 };
	private final static int MAX_UNFRAGMENTED_SIZE = 4096;

	public static void main(String[] args) throws OSException {
		// Create and set OSCORE Security Context
		OSCoreCtx ctx = new OSCoreCtx(masterSecret, false, alg, sid, rid, kdf, replayWindowSize, masterSalt, idContext,
				MAX_UNFRAGMENTED_SIZE);
		db.addContext(ctx);
		OSCoreCoapStackFactory.useAsDefault(db);

		final CoapServer server = new CoapServer(CoAP.DEFAULT_COAP_PORT);

		OSCoreResource helloRes = new HelloResource("hello", true);
		OSCoreResource timeRes = new TimeResource("time", true);
		server.add(helloRes);
		server.add(timeRes);
		server.start();
	}

	/**
	 * Hello World resource
	 *
	 */
	static class HelloResource extends OSCoreResource {

		public HelloResource(String name, boolean isProtected) {
			super(name, isProtected);
		}

		@Override
		public void handleGET(CoapExchange exchange) {
			System.out.println("Accessing hello resource");
			Response r = new Response(ResponseCode.CONTENT);
			r.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
			r.setPayload("Hello World!");
			exchange.respond(r);
		}
	}

	/**
	 * Current Time resource
	 * 
	 */
	static class TimeResource extends OSCoreResource {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

		public TimeResource(String name, boolean isProtected) {
			super(name, isProtected);
		}

		@Override
		public void handleGET(CoapExchange exchange) {
			System.out.println("Accessing time resource");
			Response r = new Response(ResponseCode.CONTENT);
			r.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
			LocalDateTime now = LocalDateTime.now();
			r.setPayload(dtf.format(now));
			exchange.respond(r);
		}
	}
}
```
