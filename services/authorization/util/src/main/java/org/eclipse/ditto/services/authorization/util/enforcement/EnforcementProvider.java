/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial contribution
 */
package org.eclipse.ditto.services.authorization.util.enforcement;

import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.services.utils.akka.functional.AsyncPartial;
import org.eclipse.ditto.services.utils.akka.functional.ImmutableActor;

/**
 * Provider interface for {@link Enforcement}.
 *
 * @param <T> the type of commands which are enforced.
 */
public interface EnforcementProvider<T extends WithDittoHeaders> {

    /**
     * The base class of the commands to which this enforcement applies.
     *
     * @return the command class.
     */
    Class<T> getCommandClass();

    /**
     * Test whether this enforcement provider is applicable for the given command.
     *
     * @param command the command.
     * @return whether this enforcement provider is applicable.
     */
    default boolean isApplicable(final T command) {
        return true;
    }

    /**
     * Creates an {@link Enforcement} for the given {@code context}.
     *
     * @param context the context.
     * @return the {@link Enforcement}.
     */
    Enforcement<T> createEnforcement(final Enforcement.Context context);

    default ImmutableActor.Builder<Enforcement.Context, Object, Void> toActorBuilder() {
        return context -> actorUtils -> {
            final Enforcement<T> enforcement = createEnforcement(context.withActorUtils(actorUtils));
            return sender -> {

                final AsyncPartial<T, Void> messageHandler =
                        AsyncPartial.fromTotal(message -> {
                            enforcement.enforce(message, sender);
                            return null;
                        });

                return messageHandler.filter(this::isApplicable)
                        .filterBy(getCommandClass());
            };
        };
    }
}
