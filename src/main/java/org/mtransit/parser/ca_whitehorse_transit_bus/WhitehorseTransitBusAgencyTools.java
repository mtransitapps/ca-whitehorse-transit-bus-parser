package org.mtransit.parser.ca_whitehorse_transit_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.mt.data.MAgency;

import java.util.regex.Pattern;

import static org.mtransit.commons.StringUtils.EMPTY;

// https://data.whitehorse.ca/
// https://data.whitehorse.ca/Google_transit_Feed_Docs.zip
public class WhitehorseTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new WhitehorseTransitBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Whitehorse Transit";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Nullable
	@Override
	public String getRouteShortName(@NotNull GRoute gRoute) {
		//noinspection deprecation
		return gRoute.getRouteId(); // use route ID as route short name
	}

	private static final String AGENCY_COLOR_BLUE = "006666"; // BLUE (from Twitter color)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			final int rid = (int) getRouteId(gRoute);
			switch (rid) {
			// @formatter:off
			case 1: return "73A3CE";
			case 2: return "79B242";
			case 3: return "D42027";
			case 4: return "80407E";
			case 5: return "EA9025";
			case 6: return "14A79D";
			// @formatter:on
			}
			throw new MTLog.Fatal("Unexpected route color for %s!", gRoute);
		}
		return super.getRouteColor(gRoute);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern EXPRESS_ = CleanUtils.cleanWord("express");

	private static final Pattern COPPER_RIDGE_ = CleanUtils.cleanWord("CopperRidge");
	private static final String COPPER_RIDGE_REPLACEMENT = CleanUtils.cleanWordsReplacement("Copper Ridge");

	private static final Pattern _DASH_ = Pattern.compile(" - ");
	private static final String _DASH_REPLACEMENT = "<>";

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = EXPRESS_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = COPPER_RIDGE_.matcher(tripHeadsign).replaceAll(COPPER_RIDGE_REPLACEMENT);
		tripHeadsign = _DASH_.matcher(tripHeadsign).replaceAll(_DASH_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanBounds(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = COPPER_RIDGE_.matcher(gStopName).replaceAll(COPPER_RIDGE_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.cleanSlashes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
