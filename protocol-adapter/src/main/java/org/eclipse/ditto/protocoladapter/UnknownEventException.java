/*
 * Copyright (c) 2017-2018 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.protocoladapter;

import java.net.URI;
import java.text.MessageFormat;

import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeExceptionBuilder;
import org.eclipse.ditto.model.base.headers.DittoHeaders;

/**
 * Thrown if a {@link org.eclipse.ditto.signals.events.base.Event} is not supported.
 */
public final class UnknownEventException extends DittoRuntimeException {

    /**
     * Error code of this exception.
     */
    public static final String ERROR_CODE = "things.protocol.adapter:unknown.event";

    private static final String MESSAGE_TEMPLATE = "The event ''{0}'' is not supported by the adapter.";

    private static final String DEFAULT_DESCRIPTION = "Check if the event is correct.";

    private static final long serialVersionUID = 1359090043587487779L;

    private UnknownEventException(final DittoHeaders dittoHeaders, final String message,
            final String description, final Throwable cause, final URI href) {
        super(ERROR_CODE, HttpStatusCode.BAD_REQUEST, dittoHeaders, message, description, cause, href);
    }

    /**
     * A mutable builder for a {@code UnknownEventException}.
     *
     * @param eventName the event not supported.
     * @return the builder.
     */
    public static Builder newBuilder(final String eventName) {
        return new Builder(eventName);
    }

    /**
     * Constructs a new {@code UnknownEventException} object with given message.
     *
     * @param message detail message. This message can be later retrieved by the {@link #getMessage()} method.
     * @param dittoHeaders the headers of the command which resulted in this exception.
     * @return the new UnknownEventException.
     */
    public static UnknownEventException fromMessage(final String message, final DittoHeaders dittoHeaders) {
        return new Builder() //
                .dittoHeaders(dittoHeaders) //
                .message(message) //
                .build();
    }

    /**
     * Constructs a new {@code UnknownEventException} object with the exception message extracted from the given JSON
     * object.
     *
     * @param jsonObject the JSON to read the {@link DittoRuntimeException.JsonFields#MESSAGE} field from.
     * @param dittoHeaders the headers of the command which resulted in this exception.
     * @return the new UnknownEventException.
     * @throws org.eclipse.ditto.json.JsonMissingFieldException if the {@code jsonObject} does not have the {@link
     * DittoRuntimeException.JsonFields#MESSAGE} field.
     */
    public static UnknownEventException fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return fromMessage(readMessage(jsonObject), dittoHeaders);
    }

    /**
     * A mutable builder with a fluent API for a {@link UnknownEventException}.
     *
     */
    @NotThreadSafe
    public static final class Builder extends DittoRuntimeExceptionBuilder<UnknownEventException> {

        private Builder() {
            description(DEFAULT_DESCRIPTION);
        }

        private Builder(final String eventName) {
            this();
            message(MessageFormat.format(MESSAGE_TEMPLATE, eventName));
        }

        @Override
        protected UnknownEventException doBuild(final DittoHeaders dittoHeaders, final String message,
                final String description, final Throwable cause, final URI href) {
            return new UnknownEventException(dittoHeaders, message, description, cause, href);
        }
    }

}
