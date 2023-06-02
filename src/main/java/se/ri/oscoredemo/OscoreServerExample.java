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
