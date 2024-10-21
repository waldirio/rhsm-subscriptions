/*
 * Copyright Red Hat, Inc.
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
package com.redhat.swatch.contract.resource.api.v1;

import com.redhat.swatch.contract.openapi.model.ReportCategory;
import com.redhat.swatch.contract.repository.HardwareMeasurementType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface ApiModelMapperV1 {
  com.redhat.swatch.contract.repository.ReportCategory map(
      com.redhat.swatch.contract.openapi.model.ReportCategory reportCategory);

  com.redhat.swatch.contract.repository.ServiceLevel map(
      com.redhat.swatch.contract.openapi.model.ServiceLevelType serviceLevelType);

  com.redhat.swatch.contract.openapi.model.ServiceLevelType map(
      com.redhat.swatch.contract.repository.ServiceLevel sanitizedServiceLevel);

  com.redhat.swatch.contract.repository.Usage map(
      com.redhat.swatch.contract.openapi.model.UsageType usage);

  com.redhat.swatch.contract.openapi.model.UsageType map(
      com.redhat.swatch.contract.repository.Usage sanitizedUsage);

  com.redhat.swatch.contract.repository.BillingProvider map(
      com.redhat.swatch.contract.openapi.model.BillingProviderType billingProviderType);

  com.redhat.swatch.contract.openapi.model.BillingProviderType map(
      com.redhat.swatch.contract.repository.BillingProvider sanitizeProviderLevel);

  @SuppressWarnings("Duplicates")
  default ReportCategory measurementTypeToReportCategory(HardwareMeasurementType measurementType) {
    if (HardwareMeasurementType.isSupportedCloudProvider(measurementType.name())) {
      return ReportCategory.CLOUD;
    }
    return switch (measurementType) {
      case VIRTUAL -> ReportCategory.VIRTUAL;
      case PHYSICAL -> ReportCategory.PHYSICAL;
      case HYPERVISOR -> ReportCategory.HYPERVISOR;
      default -> null;
    };
  }
}