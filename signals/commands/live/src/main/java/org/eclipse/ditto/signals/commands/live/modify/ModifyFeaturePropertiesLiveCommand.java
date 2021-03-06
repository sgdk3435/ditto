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
package org.eclipse.ditto.signals.commands.live.modify;

import javax.annotation.Nonnull;

import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.signals.base.WithFeatureId;
import org.eclipse.ditto.signals.commands.live.base.LiveCommand;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.ThingModifyCommand;

/**
 * {@link ModifyFeatureProperties} live command giving access to the command and all of its special accessors. Also the
 * entry point for creating a {@link ModifyFeaturePropertiesLiveCommandAnswerBuilder} as answer for an incoming
 * command.
 */
public interface ModifyFeaturePropertiesLiveCommand extends LiveCommand<ModifyFeaturePropertiesLiveCommand,
        ModifyFeaturePropertiesLiveCommandAnswerBuilder>, ThingModifyCommand<ModifyFeaturePropertiesLiveCommand>,
        WithFeatureId {

    /**
     * Returns the {@link FeatureProperties} to modify.
     *
     * @return the Properties to modify.
     * @see ModifyFeatureProperties#getProperties()
     */
    @Nonnull
    FeatureProperties getProperties();

}
