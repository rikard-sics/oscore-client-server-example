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
