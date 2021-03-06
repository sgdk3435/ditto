/*
 * Copyright (c) 2017-2018 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.ditto.services.things.persistence.actors.strategies.events;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.things.ThingBuilder;
import org.eclipse.ditto.signals.events.things.FeaturePropertyDeleted;

/**
 * This strategy handles the {@link org.eclipse.ditto.signals.events.things.FeaturePropertyDeleted} event.
 */
@Immutable
final class FeaturePropertyDeletedStrategy extends AbstractEventStrategy<FeaturePropertyDeleted> {

    @Override
    protected ThingBuilder.FromCopy applyEvent(final FeaturePropertyDeleted event,
            final ThingBuilder.FromCopy thingBuilder) {

        return thingBuilder.removeFeatureProperty(event.getFeatureId(), event.getPropertyPointer());
    }

}
