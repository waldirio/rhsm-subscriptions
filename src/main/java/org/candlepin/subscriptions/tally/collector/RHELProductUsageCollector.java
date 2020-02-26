/*
 * Copyright (c) 2009 - 2019 Red Hat, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Red Hat trademarks are not licensed under GPLv3. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.candlepin.subscriptions.tally.collector;

import org.candlepin.subscriptions.tally.UsageCalculation;
import org.candlepin.subscriptions.tally.facts.NormalizedFacts;

// NOTE: If we need to eventually reuse these rules/calculations for other products
//       we should consider renaming this class.

/**
 * Collects usage data for the RHEL product.
 */
public class RHELProductUsageCollector implements ProductUsageCollector {

    @Override
    public void collect(UsageCalculation prodCalc, NormalizedFacts normalizedFacts) {
        int cores = normalizedFacts.getCores() != null ? normalizedFacts.getCores() : 0;
        int sockets = normalizedFacts.getSockets() != null ? normalizedFacts.getSockets() : 0;

        boolean guestWithUnknownHypervisor =
            normalizedFacts.isVirtual() && normalizedFacts.isHypervisorUnknown();

        // Cloud provider hosts only account for a single socket.
        if (normalizedFacts.getCloudProviderType() != null) {
            prodCalc.addCloudProvider(normalizedFacts.getCloudProviderType(), cores, 1, 1);
        }
        else if (normalizedFacts.isHypervisor()) {
            if (sockets == 0) {
                throw new IllegalStateException("Hypervisor has no sockets and will not contribute to the " +
                    "totals. The tally for the RHEL product will not be accurate since all associated " +
                    "guests will not contribute to the tally.");
            }
            prodCalc.addHypervisor(cores, sockets, 1);
        }
        else if (guestWithUnknownHypervisor) {
            // If the hypervisor is unknown for a guest, we consider it as having a
            // unique hypervisor instance contributing to the hypervisor counts.
            // Since the guest is unmapped, we only contribute a single socket.
            prodCalc.addHypervisor(cores, 1, 1);
        }
        // Accumulate for physical systems.
        else if (!normalizedFacts.isVirtual()) {
            // Physical system so increment the physical system counts.
            prodCalc.addPhysical(cores, sockets, 1);
        }
    }

}
