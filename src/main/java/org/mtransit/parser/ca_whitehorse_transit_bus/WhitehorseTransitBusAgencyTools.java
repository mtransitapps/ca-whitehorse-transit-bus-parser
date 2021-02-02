package org.mtransit.parser.ca_whitehorse_transit_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.StringUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

// http://data.whitehorse.ca/
// http://ww3.whitehorse.ca/Features/GIS/GoogleTransit/Google_transit_feed_docs.zip
public class WhitehorseTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[4];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-whitehorse-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new WhitehorseTransitBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating Whitehorse Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating Whitehorse Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
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
			int rid = (int) getRouteId(gRoute);
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

	private static final String SEP = " - "; // same as GTFS
	private static final String EXPRESS = "Express";
	private static final String COPPER_RIDGE = "Copper Rdg";
	private static final String COPPER_RIDGE_GRANGER = COPPER_RIDGE + SEP + "Granger";
	private static final String PORTER_CREEK = "Porter Crk";
	private static final String PORTER_CREEK_EXPRESS = PORTER_CREEK + " " + EXPRESS;
	private static final String RIVERDALE = "Riverdale";
	private static final String RIVERDALE_NORTH = RIVERDALE + " North";
	private static final String RIVERDALE_SOUTH = RIVERDALE + " South";
	private static final String RR_MC_INTYRE_HILLCREST = "RR" + SEP + "McIntyre" + SEP + "Hillcrest";
	private static final String TAKHINI_YUKON_COLLEGE = "Takhini" + SEP + "Yukon College";
	private static final String LOBIRD_COPPER_RIDGE_EXPRESS = "Lobird" + SEP + COPPER_RIDGE + " " + EXPRESS;

	private static final HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		//noinspection deprecation
		map2.put(1L, new RouteTripSpec(1L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, PORTER_CREEK_EXPRESS, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, RIVERDALE_NORTH) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(
								"16",
								"46",
								"17"
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(
								"17",
								"3",
								"16"
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(2L, new RouteTripSpec(2L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, COPPER_RIDGE_GRANGER, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, RIVERDALE_SOUTH) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(
								"16",
								"51",
								"17"
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(
								"17",
								"60",
								"16"
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(3L, new RouteTripSpec(3L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, RR_MC_INTYRE_HILLCREST, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, RIVERDALE_NORTH) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(
								"16",
								"87",
								"17"
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(
								"17",
								"83",
								"16"
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(5L, new RouteTripSpec(5L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, TAKHINI_YUKON_COLLEGE, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, LOBIRD_COPPER_RIDGE_EXPRESS) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(
								"16",
								"146",
								"17"
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(
								"17",
								"128",
								"16"
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(6L, new RouteTripSpec(6L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Porter Crk - Whistle Bend", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ingram - Granger") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"139", // Range Rd & Takhini Arena
								"38", // ++
								"141" // Industial Road (West side)
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"141", // Industial Road (West side)
								"91", // Hamilton Blvd by the CGC
								"58", // ++
								"79", // Hamilton & Valley View Walkway
								"139" // Range Rd & Takhini Arena
						)) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, @NotNull List<MTripStop> list1, @NotNull List<MTripStop> list2, @NotNull MTripStop ts1, @NotNull MTripStop ts2, @NotNull GStop ts1GStop, @NotNull GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@NotNull
	@Override
	public ArrayList<MTrip> splitTrip(@NotNull MRoute mRoute, @Nullable GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@NotNull
	@Override
	public Pair<Long[], Integer[]> splitTripStop(@NotNull MRoute mRoute, @NotNull GTrip gTrip, @NotNull GTripStop gTripStop, @NotNull ArrayList<MTrip> splitTrips, @NotNull GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		String tripHeadSign = gTrip.getTripHeadsign();
		if (StringUtils.isEmpty(tripHeadSign)) {
			tripHeadSign = mRoute.getLongName();
		}
		mTrip.setHeadsignString(
				cleanTripHeadsign(tripHeadSign),
				gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId()
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return false; // DISABLED because direction_id & trip head-sign NOT provided
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.cleanSlashes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
