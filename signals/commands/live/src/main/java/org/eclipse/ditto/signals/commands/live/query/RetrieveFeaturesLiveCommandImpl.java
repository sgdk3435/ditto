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
package org.eclipse.ditto.signals.commands.live.query;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.signals.commands.base.Command;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatures;

/**
 * An immutable implementation of {@link RetrieveFeaturesLiveCommand}.
 */
@ParametersAreNonnullByDefault
@Immutable
final class RetrieveFeaturesLiveCommandImpl extends AbstractQueryLiveCommand<RetrieveFeaturesLiveCommand,
        RetrieveFeaturesLiveCommandAnswerBuilder> implements RetrieveFeaturesLiveCommand {

    private RetrieveFeaturesLiveCommandImpl(final RetrieveFeatures command) {
        super(command);
    }

    /**
     * Returns an instance of {@code RetrieveFeaturesLiveCommandImpl}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of {@link RetrieveFeatures}.
     */
    @Nonnull
    public static RetrieveFeaturesLiveCommandImpl of(final Command<?> command) {
        return new RetrieveFeaturesLiveCommandImpl((RetrieveFeatures) command);
    }

    @Override
    public RetrieveFeaturesLiveCommand setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(RetrieveFeatures.of(getThingId(), getSelectedFields().orElse(null), dittoHeaders));
    }

    @Nonnull
    @Override
    public RetrieveFeaturesLiveCommandAnswerBuilder answer() {
        return RetrieveFeaturesLiveCommandAnswerBuilderImpl.newInstance(this);
    }

    @Nonnull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + super.toString() + "]";
    }

}
