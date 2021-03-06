/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.internal;

import java.io.IOException;
import java.lang.reflect.Type;

import com.arangodb.ArangoDBException;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public class ArangoExecutorSync extends ArangoExecutor {

	private final CommunicationProtocol protocol;

	public ArangoExecutorSync(final CommunicationProtocol protocol, final ArangoSerialization util,
		final DocumentCache documentCache) {
		super(util, documentCache);
		this.protocol = protocol;
	}

	public <T> T execute(final Request request, final Type type) throws ArangoDBException {
		return execute(request, new ResponseDeserializer<T>() {
			@Override
			public T deserialize(final Response response) throws VPackException {
				return createResult(type, response);
			}
		});
	}

	public <T> T execute(final Request request, final ResponseDeserializer<T> responseDeserializer)
			throws ArangoDBException {
		try {
			final Response response = protocol.execute(request);
			return responseDeserializer.deserialize(response);
		} catch (final VPackException e) {
			throw new ArangoDBException(e);
		}
	}

	public void disconnect() {
		try {
			protocol.close();
		} catch (final IOException e) {
			throw new ArangoDBException(e);
		}
	}
}
