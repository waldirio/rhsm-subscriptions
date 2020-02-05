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

import static org.candlepin.subscriptions.tally.collector.Assertions.*;
import static org.candlepin.subscriptions.tally.collector.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.candlepin.subscriptions.db.model.HardwareMeasurementType;
import org.candlepin.subscriptions.tally.ProductUsageCalculation;
import org.candlepin.subscriptions.tally.facts.NormalizedFacts;

import org.junit.jupiter.api.Test;

public class RHELProductUsageCollectorTest {

    private RHELProductUsageCollector collector;

    public RHELProductUsageCollectorTest() {
        collector = new RHELProductUsageCollector();
    }

    @Test
    public void testCountsForHypervisor() {
        NormalizedFacts facts = hypervisorFacts(4, 12);

        ProductUsageCalculation calc = new ProductUsageCalculation("RHEL");
        collector.collect(calc, facts);
        assertTotalsCalculation(calc, 4, 12, 1);
        assertHypervisorTotalsCalculation(calc, 4, 12, 1);

        // Expects no physical totals in this case.
        assertNull(calc.getTotals(HardwareMeasurementType.PHYSICAL));
    }

    @Test
    public void testCountsForGuestWithKnownHypervisor() {
        NormalizedFacts facts = guestFacts(3, 12, false);

        ProductUsageCalculation calc = new ProductUsageCalculation("RHEL");
        collector.collect(calc, facts);

        // A guest with a known hypervisor does not contribute to any counts
        // as they are accounted for by the guest's hypervisor.
        assertNull(calc.getTotals(HardwareMeasurementType.TOTAL));
        assertNull(calc.getTotals(HardwareMeasurementType.PHYSICAL));
        assertNull(calc.getTotals(HardwareMeasurementType.HYPERVISOR));
    }

    @Test
    public void testCountsForGuestWithUnkownHypervisor() {
        NormalizedFacts facts = guestFacts(3, 12, true);

        ProductUsageCalculation calc = new ProductUsageCalculation("RHEL");
        collector.collect(calc, facts);

        // A guest with an unknown hypervisor contributes to the overall totals
        // It is considered as having its own unique hypervisor and therefore
        // contributes its own values to the hypervisor counts.
        assertTotalsCalculation(calc, 1, 12, 1);
        assertHypervisorTotalsCalculation(calc, 1, 12, 1);
        assertNull(calc.getTotals(HardwareMeasurementType.PHYSICAL));
    }

    @Test
    public void testCountsForPhysicalSystem() {
        NormalizedFacts facts = physicalNonHypervisor(4, 12);

        ProductUsageCalculation calc = new ProductUsageCalculation("RHEL");
        collector.collect(calc, facts);

        assertTotalsCalculation(calc, 4, 12, 1);
        assertPhysicalTotalsCalculation(calc, 4, 12, 1);
        assertNull(calc.getTotals(HardwareMeasurementType.HYPERVISOR));
    }

    @Test
    public void hypervisorReportedWithNoSocketsWillRaiseException() {
        NormalizedFacts facts = hypervisorFacts(0, 0);
        ProductUsageCalculation calc = new ProductUsageCalculation("RHEL");
        assertThrows(IllegalStateException.class, () -> collector.collect(calc, facts));
    }

    @Test
    public void testCountsForCloudProvider() {
        // Cloud provider host should contribute to the matched supported cloud provider,
        // as well as the overall total. A cloud host should only ever contribute 1 socket
        // along with its cores.
        NormalizedFacts facts = cloudMachineFacts("aws", 4, 12);

        ProductUsageCalculation calc = new ProductUsageCalculation("RHEL");
        collector.collect(calc, facts);

        assertTotalsCalculation(calc, 1, 12, 1);
        assertHardwareMeasurementTotals(calc, HardwareMeasurementType.AWS, 1, 12, 1);
        assertNullExcept(calc, HardwareMeasurementType.TOTAL, HardwareMeasurementType.AWS);
    }

    @Test
    public void testNormalCollectionRulesWhenCloudProviderIsUnknown() {
        NormalizedFacts facts = guestFacts(3, 12, true);
        facts.setCloudProvider("UNKNOWN");

        ProductUsageCalculation calc = new ProductUsageCalculation("RHEL");
        collector.collect(calc, facts);

        assertTotalsCalculation(calc, 1, 12, 1);
        assertHypervisorTotalsCalculation(calc, 1, 12, 1);
        assertNull(calc.getTotals(HardwareMeasurementType.PHYSICAL));
    }
}
