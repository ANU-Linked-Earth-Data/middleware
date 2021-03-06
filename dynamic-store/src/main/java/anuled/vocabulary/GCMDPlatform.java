package anuled.vocabulary;

import org.apache.jena.rdf.model.Resource;

/**
 * NASA remote sensing platform vocabulary
 *
 */
public class GCMDPlatform extends AbstractVocabulary {
	static {
		uri = "http://geobrain.laits.gmu.edu/ontology/2004/11/gcmd-platform.owl#";
	}
	
	// Classes
    public static final Resource METEOR_2 = resource("METEOR-2");
    public static final Resource METEOR = resource("METEOR");
    public static final Resource NOAA = resource("NOAA");
    public static final Resource PLATFORM = resource("PLATFORM");
    public static final Resource NIMBUS_1 = resource("NIMBUS-1");
    public static final Resource NIMBUS = resource("NIMBUS");
    public static final Resource SOLARD_7A = resource("SOLARD-7A");
    public static final Resource SOLRAD = resource("SOLRAD");
    public static final Resource DMSP_5B_F3 = resource("DMSP_5B_F3");
    public static final Resource DMSP = resource("DMSP");
    public static final Resource NOAA_8 = resource("NOAA-8");
    public static final Resource SPACE_SHUTTLE = resource("SPACE_SHUTTLE");
    public static final Resource CBERS_1 = resource("CBERS-1");
    public static final Resource CBERS = resource("CBERS");
    public static final Resource SPOT_1 = resource("SPOT-1");
    public static final Resource SPOT = resource("SPOT");
    public static final Resource STS_62 = resource("STS-62");
    public static final Resource STS = resource("STS");
    public static final Resource EXPLORER_35 = resource("EXPLORER-35");
    public static final Resource GMS_2 = resource("GMS-2");
    public static final Resource GMS = resource("GMS");
    public static final Resource AE_A = resource("AE-A");
    public static final Resource AE = resource("AE");
    public static final Resource OSO_4 = resource("OSO-4");
    public static final Resource OSO = resource("OSO");
    public static final Resource AEM = resource("AEM");
    public static final Resource NIMBUS_3 = resource("NIMBUS-3");
    public static final Resource PROFS = resource("PROFS");
    public static final Resource ETALON_1 = resource("ETALON-1");
    public static final Resource ETALON = resource("ETALON");
    public static final Resource ISIS_2 = resource("ISIS-2");
    public static final Resource ISIS = resource("ISIS");
    public static final Resource ODIN = resource("ODIN");
    public static final Resource ORBVIEW_2 = resource("ORBVIEW-2");
    public static final Resource MOS_1 = resource("MOS-1");
    public static final Resource MOS = resource("MOS");
    public static final Resource GOES_12 = resource("GOES-12");
    public static final Resource GOES = resource("GOES");
    public static final Resource PIBAL = resource("PIBAL");
    public static final Resource BALLOON = resource("BALLOON");
    public static final Resource SAGE = resource("SAGE");
    public static final Resource ADEOS_I = resource("ADEOS-I");
    public static final Resource ADEOS = resource("ADEOS");
    public static final Resource GEOS_2 = resource("GEOS-2");
    public static final Resource GEOS = resource("GEOS");
    public static final Resource ALOUETTE_1 = resource("ALOUETTE-1");
    public static final Resource ALOUETTE = resource("ALOUETTE");
    public static final Resource GSN = resource("GSN");
    public static final Resource NEUTRON_MONITOR_STATIONS = resource("NEUTRON_MONITOR_STATIONS");
    public static final Resource GROUND_STATION = resource("GROUND_STATION");
    public static final Resource STS_64 = resource("STS-64");
    public static final Resource DMSP_5D_1_F1 = resource("DMSP_5D-1_F1");
    public static final Resource STS_39 = resource("STS-39");
    public static final Resource OGO_2 = resource("OGO-2");
    public static final Resource OGO = resource("OGO");
    public static final Resource STS_41 = resource("STS-41");
    public static final Resource QUIKSCAT = resource("QUIKSCAT");
    public static final Resource NOAA_POES = resource("NOAA_POES");
    public static final Resource AE_C = resource("AE-C");
    public static final Resource ACRIMSAT = resource("ACRIMSAT");
    public static final Resource EXPLORER_9 = resource("EXPLORER-9");
    public static final Resource AEM_2 = resource("AEM-2");
    public static final Resource STS_51F = resource("STS-51F");
    public static final Resource NIMBUS_5 = resource("NIMBUS-5");
    public static final Resource CRYOSAT = resource("CRYOSAT");
    public static final Resource IMP = resource("IMP");
    public static final Resource SOLARD_10 = resource("SOLARD-10");
    public static final Resource SORCE = resource("SORCE");
    public static final Resource OKEAN_O = resource("OKEAN-O");
    public static final Resource OCEAN_PLATFORM = resource("OCEAN_PLATFORM");
    public static final Resource STELLA = resource("STELLA");
    public static final Resource TRMM = resource("TRMM");
    public static final Resource MIO = resource("MIO");
    public static final Resource GOES_10 = resource("GOES-10");
    public static final Resource LAGEOS_1 = resource("LAGEOS-1");
    public static final Resource LAGEOS = resource("LAGEOS");
    public static final Resource DMSP_5D_2_F11 = resource("DMSP_5D-2_F11");
    public static final Resource IRAS = resource("IRAS");
    public static final Resource ALOS = resource("ALOS");
    public static final Resource GLONASS_40_82 = resource("GLONASS-40-82");
    public static final Resource METEOSAT_7 = resource("METEOSAT-7");
    public static final Resource METEOSAT = resource("METEOSAT");
    public static final Resource METEOSAT_1 = resource("METEOSAT-1");
    public static final Resource AWOS = resource("AWOS");
    public static final Resource GMS_4 = resource("GMS-4");
    public static final Resource LANDSAT_4 = resource("LANDSAT-4");
    public static final Resource LANDSAT = resource("LANDSAT");
    public static final Resource TIROS_N = resource("TIROS-N");
    public static final Resource TIROS = resource("TIROS");
    public static final Resource FIXED_OBSERVATION_STATIONS = resource("FIXED_OBSERVATION_STATIONS");
    public static final Resource OV_102 = resource("OV-102");
    public static final Resource R_V_NBP = resource("R_V_NBP");
    public static final Resource SOLARD_1 = resource("SOLARD-1");
    public static final Resource GOES_2 = resource("GOES-2");
    public static final Resource SPOT_2 = resource("SPOT-2");
    public static final Resource AE_E = resource("AE-E");
    public static final Resource ARGON = resource("ARGON");
    public static final Resource NOAA_11 = resource("NOAA-11");
    public static final Resource DMSP_5D_2_F13 = resource("DMSP_5D-2_F13");
    public static final Resource LANDSAT_7 = resource("LANDSAT-7");
    public static final Resource RAE_A = resource("RAE-A");
    public static final Resource RAE = resource("RAE");
    public static final Resource SPOT_4 = resource("SPOT-4");
    public static final Resource SOLRAD_7B = resource("SOLRAD-7B");
    public static final Resource STS_56 = resource("STS-56");
    public static final Resource METEOSAT_3 = resource("METEOSAT-3");
    public static final Resource ARWS = resource("ARWS");
    public static final Resource GOES_6 = resource("GOES-6");
    public static final Resource MIPS = resource("MIPS");
    public static final Resource BAPMON = resource("BAPMON");
    public static final Resource METEOR_3M = resource("METEOR-3M");
    public static final Resource METOP = resource("METOP");
    public static final Resource IRS_1A = resource("IRS-1A");
    public static final Resource IRS = resource("IRS");
    public static final Resource STS_68 = resource("STS-68");
    public static final Resource GEOMET = resource("GEOMET");
    public static final Resource SEISMOLOGICAL_STATIONS = resource("SEISMOLOGICAL_STATIONS");
    public static final Resource SPACELAB_1 = resource("SPACELAB-1");
    public static final Resource SPACELAB = resource("SPACELAB");
    public static final Resource UARS = resource("UARS");
    public static final Resource OV_104 = resource("OV-104");
    public static final Resource DMSP_5D_2_F15 = resource("DMSP_5D-2_F15");
    public static final Resource GEMINI_7 = resource("GEMINI-7");
    public static final Resource GEMINI = resource("GEMINI");
    public static final Resource INSAT = resource("INSAT");
    public static final Resource SPACELAB_3 = resource("SPACELAB-3");
    public static final Resource MIDAS_2 = resource("MIDAS_2");
    public static final Resource AIR_PHOTOGRAPH = resource("AIR_PHOTOGRAPH");
    public static final Resource IRS_1C = resource("IRS-1C");
    public static final Resource AEROS_1 = resource("AEROS-1");
    public static final Resource AEROS = resource("AEROS");
    public static final Resource PMS = resource("PMS");
    public static final Resource STS_66 = resource("STS-66");
    public static final Resource GOES_4 = resource("GOES-4");
    public static final Resource RADIO_TRANSMITTER = resource("RADIO_TRANSMITTER");
    public static final Resource IMAGE = resource("IMAGE");
    public static final Resource HATDL = resource("HATDL");
    public static final Resource JASON_1 = resource("JASON-1");
    public static final Resource SMM = resource("SMM");
    public static final Resource INSAT_1B = resource("INSAT-1B");
    public static final Resource NOAA_5 = resource("NOAA-5");
    public static final Resource GEOSAT = resource("GEOSAT");
    public static final Resource COASTAL_STATIONS = resource("COASTAL_STATIONS");
    public static final Resource SRL_1 = resource("SRL-1");
    public static final Resource SRL = resource("SRL");
    public static final Resource ERS_1 = resource("ERS-1");
    public static final Resource ERS = resource("ERS");
    public static final Resource IMP_8 = resource("IMP-8");
    public static final Resource YOHKOH = resource("YOHKOH");
    public static final Resource OSO_6 = resource("OSO-6");
    public static final Resource GFO_1 = resource("GFO-1");
    public static final Resource NOAA_3 = resource("NOAA-3");
    public static final Resource NOAA_13 = resource("NOAA-13");
    public static final Resource OSTA_1 = resource("OSTA-1");
    public static final Resource ECHO_2 = resource("ECHO-2");
    public static final Resource ECHO = resource("ECHO");
    public static final Resource GONG_NETWORK = resource("GONG_NETWORK");
    public static final Resource APOLLO_SOYUZ = resource("APOLLO-SOYUZ");
    public static final Resource APOLLO = resource("APOLLO");
    public static final Resource DASH_2 = resource("DASH-2");
    public static final Resource IRS_P3 = resource("IRS-P3");
    public static final Resource NOAA_17 = resource("NOAA-17");
    public static final Resource GEMINI_8 = resource("GEMINI-8");
    public static final Resource DIADEM_1D = resource("DIADEM-1D");
    public static final Resource DIADEM = resource("DIADEM");
    public static final Resource SMS_1 = resource("SMS-1");
    public static final Resource SMS = resource("SMS");
    public static final Resource BE_B = resource("BE-B");
    public static final Resource BE = resource("BE");
    public static final Resource SME = resource("SME");
    public static final Resource STS_72 = resource("STS-72");
    public static final Resource DE = resource("DE");
    public static final Resource EOS_CHEM = resource("EOS_CHEM");
    public static final Resource GMS_5 = resource("GMS-5");
    public static final Resource STS_2 = resource("STS-2");
    public static final Resource ATS_3 = resource("ATS-3");
    public static final Resource ATS = resource("ATS");
    public static final Resource GOES_9 = resource("GOES-9");
    public static final Resource METEOSAT_4 = resource("METEOSAT-4");
    public static final Resource GPS_35 = resource("GPS-35");
    public static final Resource GPS = resource("GPS");
    public static final Resource LANDSAT_2 = resource("LANDSAT-2");
    public static final Resource NIMBUS_6 = resource("NIMBUS-6");
    public static final Resource TIPS = resource("TIPS");
    public static final Resource SKYLAB = resource("SKYLAB");
    public static final Resource LANDSAT_5 = resource("LANDSAT-5");
    public static final Resource NOAA_15 = resource("NOAA-15");
    public static final Resource AD_B = resource("AD-B");
    public static final Resource AD = resource("AD");
    public static final Resource ATS_1 = resource("ATS-1");
    public static final Resource SOLARD_9 = resource("SOLARD-9");
    public static final Resource DMSP_5D_1_F3 = resource("DMSP_5D-1_F3");
    public static final Resource OGO_5 = resource("OGO-5");
    public static final Resource SUBMARINE = resource("SUBMARINE");
    public static final Resource DE_2 = resource("DE-2");
    public static final Resource AES = resource("AES");
    public static final Resource GEMINI_10 = resource("GEMINI-10");
    public static final Resource IMP_I = resource("IMP-I");
    public static final Resource METEOSAT_6 = resource("METEOSAT-6");
    public static final Resource STS_43 = resource("STS-43");
    public static final Resource ISIS_1 = resource("ISIS-1");
    public static final Resource VENERA_13 = resource("VENERA-13");
    public static final Resource VENERA = resource("VENERA");
    public static final Resource NAVSTAR = resource("NAVSTAR");
    public static final Resource POLAR = resource("POLAR");
    public static final Resource ISS = resource("ISS");
    public static final Resource OSO_8 = resource("OSO-8");
    public static final Resource HAGGLUND = resource("HAGGLUND");
    public static final Resource NOAA_7 = resource("NOAA-7");
    public static final Resource OSO_1 = resource("OSO-1");
    public static final Resource GEMINI_12 = resource("GEMINI-12");
    public static final Resource IKONOS = resource("IKONOS");
    public static final Resource DMSP_5D_2_F7 = resource("DMSP_5D-2_F7");
    public static final Resource NOAA_2 = resource("NOAA-2");
    public static final Resource RESURS_01 = resource("RESURS-01");
    public static final Resource CRRES = resource("CRRES");
    public static final Resource SV = resource("SV");
    public static final Resource MESONET = resource("MESONET");
    public static final Resource GEMINI_4 = resource("GEMINI-4");
    public static final Resource ETALON_2 = resource("ETALON-2");
    public static final Resource SPAS_II = resource("SPAS-II");
    public static final Resource ATLAS_MOORINGS = resource("ATLAS_MOORINGS");
    public static final Resource LANYARD = resource("LANYARD");
    public static final Resource STS_45 = resource("STS-45");
    public static final Resource GEMINI_6 = resource("GEMINI-6");
    public static final Resource SAGE_III = resource("SAGE-III");
    public static final Resource DMSP_5D_2_F9 = resource("DMSP_5D-2_F9");
    public static final Resource STS_59 = resource("STS-59");
    public static final Resource MICROLAB_1 = resource("MICROLAB-1");
    public static final Resource OGO_3 = resource("OGO-3");
    public static final Resource CLOUDSAT = resource("CLOUDSAT");
    public static final Resource GMS_1 = resource("GMS-1");
    public static final Resource MAXIS = resource("MAXIS");
    public static final Resource NSRN = resource("NSRN");
    public static final Resource OSO_3 = resource("OSO-3");
    public static final Resource WESTPAC = resource("WESTPAC");
    public static final Resource STS_11 = resource("STS-11");
    public static final Resource METEOR_3 = resource("METEOR-3");
    public static final Resource ACE = resource("ACE");
    public static final Resource GMCC = resource("GMCC");
    public static final Resource COSMOS_1500 = resource("COSMOS_1500");
    public static final Resource COSMOS = resource("COSMOS");
    public static final Resource CMDL = resource("CMDL");
    public static final Resource AE_B = resource("AE-B");
    public static final Resource OGO_1 = resource("OGO-1");
    public static final Resource TRITON = resource("TRITON");
    public static final Resource SEASOAR = resource("SEASOAR");
    public static final Resource WTSS = resource("WTSS");
    public static final Resource DE_1 = resource("DE-1");
    public static final Resource C_MAN = resource("C-MAN");
    public static final Resource GOES_11 = resource("GOES-11");
    public static final Resource SMART_R = resource("SMART-R");
    public static final Resource SOUNDING_ROCKET = resource("SOUNDING_ROCKET");
    public static final Resource ANTHMS = resource("ANTHMS");
    public static final Resource ERS_2 = resource("ERS-2");
    public static final Resource VENERA_14 = resource("VENERA-14");
    public static final Resource EXOS_A = resource("EXOS-A");
    public static final Resource NIMBUS_4 = resource("NIMBUS-4");
    public static final Resource TRACE = resource("TRACE");
    public static final Resource TIROS_M = resource("TIROS-M");
    public static final Resource ALOUETTE_2 = resource("ALOUETTE-2");
    public static final Resource NIMBUS_2 = resource("NIMBUS-2");
    public static final Resource OV_099 = resource("OV-099");
    public static final Resource GRACE = resource("GRACE");
    public static final Resource CBERS_2 = resource("CBERS-2");
    public static final Resource PASSCAL = resource("PASSCAL");
    public static final Resource COSMOS_49 = resource("COSMOS_49");
    public static final Resource SOLAR_RADIATION_STATIONS = resource("SOLAR_RADIATION_STATIONS");
    public static final Resource SAGE_I = resource("SAGE-I");
    public static final Resource DMSP_5D_1_F4 = resource("DMSP_5D-1_F4");
    public static final Resource METROROLOGICAL_STATIONS = resource("METROROLOGICAL_STATIONS");
    public static final Resource NOAA_9 = resource("NOAA-9");
    public static final Resource TOPEX_POSEIDON = resource("TOPEX_POSEIDON");
    public static final Resource GOES_5 = resource("GOES-5");
    public static final Resource NOAA_12 = resource("NOAA-12");
    public static final Resource DMSP_5D_2_F12 = resource("DMSP_5D-2_F12");
    public static final Resource OV_103 = resource("OV-103");
    public static final Resource LAGEOS_2 = resource("LAGEOS-2");
    public static final Resource SOLARD_8 = resource("SOLARD-8");
    public static final Resource STS_55 = resource("STS-55");
    public static final Resource NWS = resource("NWS");
    public static final Resource AEROS_2 = resource("AEROS-2");
    public static final Resource GEOSTATIONARY_SATELLITE = resource("GEOSTATIONARY_SATELLITE");
    public static final Resource LANDSAT_3 = resource("LANDSAT-3");
    public static final Resource GOES_3 = resource("GOES-3");
    public static final Resource DMSP_5D_2_F14 = resource("DMSP_5D-2_F14");
    public static final Resource OCEAN_WEATHER_STATION = resource("OCEAN_WEATHER_STATION");
    public static final Resource AJISAI = resource("AJISAI");
    public static final Resource MOS_1B = resource("MOS-1B");
    public static final Resource RAE_B = resource("RAE-B");
    public static final Resource STS_9 = resource("STS-9");
    public static final Resource ENVISAT = resource("ENVISAT");
    public static final Resource GOES_1 = resource("GOES-1");
    public static final Resource GRAVITY_STATION = resource("GRAVITY_STATION");
    public static final Resource NOAA_10 = resource("NOAA-10");
    public static final Resource EO_1 = resource("EO-1");
    public static final Resource GMS_3 = resource("GMS-3");
    public static final Resource SUNSAT = resource("SUNSAT");
    public static final Resource STS_34 = resource("STS-34");
    public static final Resource GOES_7 = resource("GOES-7");
    public static final Resource HESSI = resource("HESSI");
    public static final Resource SAC_A = resource("SAC-A");
    public static final Resource SAC = resource("SAC");
    public static final Resource DMSP_5D_F1_F2 = resource("DMSP_5D-F1_F2");
    public static final Resource GEOMAGNETIC_STATIONS = resource("GEOMAGNETIC_STATIONS");
    public static final Resource GRO = resource("GRO");
    public static final Resource IRS_1B = resource("IRS-1B");
    public static final Resource AE_D = resource("AE-D");
    public static final Resource EOLE = resource("EOLE");
    public static final Resource EOS_AURA = resource("EOS_AURA");
    public static final Resource GEOS_3 = resource("GEOS-3");
    public static final Resource METEOSAT_2 = resource("METEOSAT-2");
    public static final Resource INSAT_1A = resource("INSAT-1A");
    public static final Resource ERBS = resource("ERBS");
    public static final Resource DMSP_5D_2_F16 = resource("DMSP_5D-2_F16");
    public static final Resource ALTUS = resource("ALTUS");
    public static final Resource CHAMP = resource("CHAMP");
    public static final Resource OV_105 = resource("OV-105");
    public static final Resource HCMM = resource("HCMM");
    public static final Resource DMSP_5D_2_F10 = resource("DMSP_5D-2_F10");
    public static final Resource EOS_CHEM_1 = resource("EOS_CHEM-1");
    public static final Resource PAM_II = resource("PAM-II");
    public static final Resource PAM = resource("PAM");
    public static final Resource TERRA = resource("TERRA");
    public static final Resource FY_1 = resource("FY-1");
    public static final Resource FY = resource("FY");
    public static final Resource GEODYNAMIC_STATIONS = resource("GEODYNAMIC_STATIONS");
    public static final Resource DIADEM_1C = resource("DIADEM-1C");
    public static final Resource ROCKET = resource("ROCKET");
    public static final Resource ZODIACS = resource("ZODIACS");
    public static final Resource AVAPS = resource("AVAPS");
    public static final Resource AQUA = resource("AQUA");
    public static final Resource STS_51B = resource("STS-51B");
    public static final Resource RADIOSONDE = resource("RADIOSONDE");
    public static final Resource ADEOS_II = resource("ADEOS-II");
    public static final Resource SCISAT = resource("SCISAT");
    public static final Resource SEASAT = resource("SEASAT");
    public static final Resource NOAA_16 = resource("NOAA-16");
    public static final Resource SOON = resource("SOON");
    public static final Resource WEATHER_STATION = resource("WEATHER_STATION");
    public static final Resource GEOS_1 = resource("GEOS-1");
    public static final Resource DRILLING_PLATFORMS = resource("DRILLING_PLATFORMS");
    public static final Resource ZEIA = resource("ZEIA");
    public static final Resource OSO_5 = resource("OSO-5");
    public static final Resource NOAA_6 = resource("NOAA-6");
    public static final Resource BE_C = resource("BE-C");
    public static final Resource MSTI_2 = resource("MSTI-2");
    public static final Resource X_POW = resource("X-POW");
    public static final Resource VANGUARD = resource("VANGUARD");
    public static final Resource AEROSONDE = resource("AEROSONDE");
    public static final Resource METEOSAT_5 = resource("METEOSAT-5");
    public static final Resource SMS_2 = resource("SMS-2");
    public static final Resource STS_7 = resource("STS-7");
    public static final Resource SPOT_3 = resource("SPOT-3");
    public static final Resource GEMINI_9 = resource("GEMINI-9");
    public static final Resource GOES_8 = resource("GOES-8");
    public static final Resource FY_2 = resource("FY-2");
    public static final Resource SRL_2 = resource("SRL-2");
    public static final Resource MIR_PRIRODA = resource("MIR-PRIRODA");
    public static final Resource EP_TOMS = resource("EP-TOMS");
    public static final Resource RADARSAT_1 = resource("RADARSAT-1");
    public static final Resource VCL = resource("VCL");
    public static final Resource FDSN = resource("FDSN");
    public static final Resource JERS_1 = resource("JERS-1");
    public static final Resource AD_C = resource("AD-C");
    public static final Resource ECHO_1 = resource("ECHO-1");
    public static final Resource NOAA_14 = resource("NOAA-14");
    public static final Resource ATS_2 = resource("ATS-2");
    public static final Resource SOYUZ = resource("SOYUZ");
    public static final Resource TMRS2 = resource("TMRS2");
    public static final Resource GPS_36 = resource("GPS-36");
    public static final Resource LANDSAT_1 = resource("LANDSAT-1");
    public static final Resource DMSP_5D_2_F8 = resource("DMSP_5D-2_F8");
    public static final Resource EOS_PM_1 = resource("EOS_PM-1");
    public static final Resource PALACE_FLOAT = resource("PALACE_FLOAT");
    public static final Resource GEMINI_11 = resource("GEMINI-11");
    public static final Resource ASOS = resource("ASOS");
    public static final Resource PAGEOS_1 = resource("PAGEOS_1");
    public static final Resource METEOR_2_21 = resource("METEOR_2-21");
    public static final Resource TIMED = resource("TIMED");
    public static final Resource DROPWINDSONDE = resource("DROPWINDSONDE");
    public static final Resource AEM_3 = resource("AEM-3");
    public static final Resource SAC_C = resource("SAC-C");
    public static final Resource OGO_6 = resource("OGO-6");
    public static final Resource GFZ_1 = resource("GFZ-1");
    public static final Resource AEM_1 = resource("AEM-1");
    public static final Resource AD_A = resource("AD-A");
    public static final Resource OSO_7 = resource("OSO-7");
    public static final Resource ACARS = resource("ACARS");
    public static final Resource BUOY = resource("BUOY");
    public static final Resource OSO_2 = resource("OSO-2");
    public static final Resource EXPLORER_33 = resource("EXPLORER-33");
    public static final Resource SOHO = resource("SOHO");
    public static final Resource ATS_4 = resource("ATS-4");
    public static final Resource GEMINI_3 = resource("GEMINI-3");
    public static final Resource FEDSAT = resource("FEDSAT");
    public static final Resource ATLAS = resource("ATLAS");
    public static final Resource AIRCRAFT = resource("AIRCRAFT");
    public static final Resource NOAA_1 = resource("NOAA-1");
    public static final Resource SHIP = resource("SHIP");
    public static final Resource GROUND_BASED_OBSERVATION = resource("GROUND-BASED_OBSERVATION");
    public static final Resource PROTEUS = resource("PROTEUS");
    public static final Resource CORONA = resource("CORONA");
    public static final Resource TIROS_7 = resource("TIROS-7");
    public static final Resource STS_41G = resource("STS-41G");
    public static final Resource ICESAT = resource("ICESAT");
    public static final Resource NPOESS = resource("NPOESS");
    public static final Resource OGO_4 = resource("OGO-4");
    public static final Resource VOLCANO_OBSERVATORY = resource("VOLCANO_OBSERVATORY");
    public static final Resource NOAA_4 = resource("NOAA-4");
    public static final Resource SCD = resource("SCD");
    public static final Resource DCP = resource("DCP");
    public static final Resource GOMS = resource("GOMS");
    public static final Resource NIMBUS_7 = resource("NIMBUS-7");
    public static final Resource HELICOPTER = resource("HELICOPTER");
    public static final Resource GEMINI_5 = resource("GEMINI-5");
    public static final Resource LPATS = resource("LPATS");
}
