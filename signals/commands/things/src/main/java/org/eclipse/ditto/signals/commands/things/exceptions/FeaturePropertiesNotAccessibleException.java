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
package org.eclipse.ditto.signals.commands.things.exceptions;

import java.net.URI;
import java.text.MessageFormat;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeExceptionBuilder;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.ThingException;

/**
 * This exception indicates, that the requested Properties do not exist or the request has insufficient rights.
 */
@Immutable
public final class FeaturePropertiesNotAccessibleException extends DittoRuntimeException implements ThingException {

    /**
     * Error code of this exception.
     */
    public static final String ERROR_CODE = ERROR_CODE_PREFIX + "feature.properties.notfound";

    private static final String MESSAGE_TEMPLATE = "The Properties of the Feature with ID ''{0}'' on the Thing with ID "
            + "''{1}'' do not exist or the requester had insufficient permissions to access it.";

    private static final String DEFAULT_DESCRIPTION =
            "Check if the ID of the Thing and the Feature ID was correct and you have sufficient permissions.";

    private static final long serialVersionUID = 3148170836485607502L;

    private FeaturePropertiesNotAccessibleException(final DittoHeaders dittoHeaders, final String message,
            final String description, final Throwable cause, final URI href) {
        super(ERROR_CODE, HttpStatusCode.NOT_FOUND, dittoHeaders, message, description, cause, href);
    }

    /**
     * A mutable builder for a {@code FeaturePropertiesNotAccessibleException}.
     *
     * @param thingId the ID of the thing.
     * @param featureId the ID of the feature.
     * @return the builder.
     */
    public static Builder newBuilder(final String thingId, final String featureId) {
        return new Builder(thingId, featureId);
    }

    /**
     * Constructs a new {@code FeaturePropertiesNotAccessibleException} object with given message.
     *
     * @param message detail message. This message can be later retrieved by the {@link #getMessage()} method.
     * @param dittoHeaders the headers of the command which resulted in this exception.
     * @return the new FeaturePropertiesNotAccessibleException.
     */
    public static FeaturePropertiesNotAccessibleException fromMessage(final String message,
            final DittoHeaders dittoHeaders) {
        return new Builder()
                .dittoHeaders(dittoHeaders)
                .message(message)
                .build();
    }

    /**
     * Constructs a new {@code FeaturePropertiesNotAccessibleException} object with the exception message extracted from
     * the given JSON object.
     *
     * @param jsonObject the JSON to read the {@link JsonFields#MESSAGE} field from.
     * @param dittoHeaders the headers of the command which resulted in this exception.
     * @return the new FeaturePropertiesNotAccessibleException.
     * @throws org.eclipse.ditto.json.JsonMissingFieldException if the {@code jsonObject} does not have the {@link
     * JsonFields#MESSAGE} field.
     */
    public static FeaturePropertiesNotAccessibleException fromJson(final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {
        return fromMessage(readMessage(jsonObject), dittoHeaders);
    }

    /**
     * A mutable builder with a fluent API for a {@link FeaturePropertiesNotAccessibleException}.
     *
     */
    @NotThreadSafe
    public static final class Builder extends DittoRuntimeExceptionBuilder<FeaturePropertiesNotAccessibleException> {

        private Builder() {
            description(DEFAULT_DESCRIPTION);
        }

        private Builder(final String thingId, final String featureId) {
            this();
            message(MessageFormat.format(MESSAGE_TEMPLATE, featureId, thingId));
        }

        @Override
        protected FeaturePropertiesNotAccessibleException doBuild(final DittoHeaders dittoHeaders,
                final String message, final String description, final Throwable cause, final URI href) {
            return new FeaturePropertiesNotAccessibleException(dittoHeaders, message, description, cause, href);
        }
    }

}
