package com.pixel.chatapp.api.model;

import java.util.Arrays;

public class VerificationResponse {
    private String reference;
    private String event;
    private String country;
    private Proofs proofs;
    private VerificationData verification_data;
    private VerificationResult verification_result;
    private Info info;

    public VerificationResponse(String reference, String event, String country,
                                Proofs proofs, VerificationData verification_data,
                                VerificationResult verification_result, Info info) {
        this.reference = reference;
        this.event = event;
        this.country = country;
        this.proofs = proofs;
        this.verification_data = verification_data;
        this.verification_result = verification_result;
        this.info = info;
    }

    public String getReference() {
        return reference;
    }

    public String getEvent() {
        return event;
    }

    public String getCountry() {
        return country;
    }

    public Proofs getProofs() {
        return proofs;
    }

    public VerificationData getVerification_data() {
        return verification_data;
    }

    public VerificationResult getVerification_result() {
        return verification_result;
    }

    public Info getInfo() {
        return info;
    }
}

class Proofs {
    private ProofDocument document;
    private String access_token;
    private Face face;
    private String verification_report;

    public Proofs(ProofDocument document, String access_token, Face face, String verification_report) {
        this.document = document;
        this.access_token = access_token;
        this.face = face;
        this.verification_report = verification_report;
    }

    public ProofDocument getDocument() {
        return document;
    }

    public String getAccess_token() {
        return access_token;
    }

    public Face getFace() {
        return face;
    }

    public String getVerification_report() {
        return verification_report;
    }
}

class ProofDocument {
    private String proof;
    private String additional_proof;

    public ProofDocument(String proof, String additional_proof) {
        this.proof = proof;
        this.additional_proof = additional_proof;
    }

    public String getProof() {
        return proof;
    }

    public String getAdditional_proof() {
        return additional_proof;
    }
}

class Face {
    private String proof;

    public Face(String proof) {
        this.proof = proof;
    }

    public String getProof() {
        return proof;
    }
}

class VerificationData {
    private Document document;

    public VerificationData(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }
}

class Document {
    private Name name;
    private String dob;
    private String expiry_date;
    private String issue_date;
    private String document_number;
    private String country;
    private int face_match_confidence;
    private String[] selected_type;
    private String[] supported_types;

    public Document(Name name, String dob, String expiry_date, String issue_date,
                    String document_number, String country, int face_match_confidence,
                    String[] selected_type, String[] supported_types) {
        this.name = name;
        this.dob = dob;
        this.expiry_date = expiry_date;
        this.issue_date = issue_date;
        this.document_number = document_number;
        this.country = country;
        this.face_match_confidence = face_match_confidence;
        this.selected_type = selected_type;
        this.supported_types = supported_types;
    }

    public Name getName() {
        return name;
    }

    public String getDob() {
        return dob;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public String getIssue_date() {
        return issue_date;
    }

    public String getDocument_number() {
        return document_number;
    }

    public String getCountry() {
        return country;
    }

    public int getFace_match_confidence() {
        return face_match_confidence;
    }

    public String[] getSelected_type() {
        return selected_type;
    }

    public String[] getSupported_types() {
        return supported_types;
    }
}

class Name {
    private String first_name;
    private String middle_name;
    private String last_name;

    public Name(String first_name, String middle_name, String last_name) {
        this.first_name = first_name;
        this.middle_name = middle_name;
        this.last_name = last_name;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public String getLast_name() {
        return last_name;
    }
}

class VerificationResult {
    private int face;
    private DocumentVerification document;

    public VerificationResult(int face, DocumentVerification document) {
        this.face = face;
        this.document = document;
    }

    public int getFace() {
        return face;
    }

    public DocumentVerification getDocument() {
        return document;
    }
}

class DocumentVerification {
    private int dob;
    private int document;
    private int document_must_not_be_expired;
    private int document_number;
    private int document_proof;
    private int document_visibility;
    private String expiry_date;
    private int face_on_document_matched;
    private int issue_date;
    private int name;
    private int selected_type;

    public DocumentVerification(int dob, int document, int document_must_not_be_expired,
                                int document_number, int document_proof, int document_visibility,
                                String expiry_date, int face_on_document_matched, int issue_date,
                                int name, int selected_type) {
        this.dob = dob;
        this.document = document;
        this.document_must_not_be_expired = document_must_not_be_expired;
        this.document_number = document_number;
        this.document_proof = document_proof;
        this.document_visibility = document_visibility;
        this.expiry_date = expiry_date;
        this.face_on_document_matched = face_on_document_matched;
        this.issue_date = issue_date;
        this.name = name;
        this.selected_type = selected_type;
    }

    // Getters...
}

class Info {
    private Agent agent;
    private Geolocation geolocation;

    public Info(Agent agent, Geolocation geolocation) {
        this.agent = agent;
        this.geolocation = geolocation;
    }

    // Getters...
}

class Agent {
    private boolean is_desktop;
    private boolean is_phone;
    private String useragent;
    private String device_name;
    private String browser_name;
    private String platform_name;

    public Agent(boolean is_desktop, boolean is_phone, String useragent,
                 String device_name, String browser_name, String platform_name) {
        this.is_desktop = is_desktop;
        this.is_phone = is_phone;
        this.useragent = useragent;
        this.device_name = device_name;
        this.browser_name = browser_name;
        this.platform_name = platform_name;
    }

    // Getters...
}

class Geolocation {
    private String host;
    private String ip;
    private String rdns;
    private String asn;
    private String isp;
    private String country_name;
    private String country_code;
    private String region_name;
    private String region_code;
    private String city;
    private String postal_code;
    private String continent_name;
    private String continent_code;
    private String latitude;
    private String longitude;
    private String metro_code;
    private String timezone;
    private String ip_type;
    private String capital;
    private String currency;

    public Geolocation(String host, String ip, String rdns, String asn, String isp,
                       String country_name, String country_code, String region_name,
                       String region_code, String city, String postal_code,
                       String continent_name, String continent_code, String latitude,
                       String longitude, String metro_code, String timezone,
                       String ip_type, String capital, String currency) {
        this.host = host;
        this.ip = ip;
        this.rdns = rdns;
        this.asn = asn;
        this.isp = isp;
        this.country_name = country_name;
        this.country_code = country_code;
        this.region_name = region_name;
        this.region_code = region_code;
        this.city = city;
        this.postal_code = postal_code;
        this.continent_name = continent_name;
        this.continent_code = continent_code;
        this.latitude = latitude;
        this.longitude = longitude;
        this.metro_code = metro_code;
        this.timezone = timezone;
        this.ip_type = ip_type;
        this.capital = capital;
        this.currency = currency;
    }

    // Getters...
}
