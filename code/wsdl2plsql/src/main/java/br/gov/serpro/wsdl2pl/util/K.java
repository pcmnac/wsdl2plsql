package br.gov.serpro.wsdl2pl.util;

import br.gov.serpro.wsdl2pl.type.ElementInfo;

public interface K
{

    public static interface Protocol
    {
        String SOAP_1_1 = "SOAP11";
        String SOAP_1_2 = "SOAP12";
    }

    public static interface Tag
    {
        public static interface Soap
        {
            String ENVELOPE = "Envelope";
            String HEADER = "Header";
            String BODY = "Body";
            // soap 1.2 fault
            String FAULT = "Fault";
            String CODE = "Code";
            String VALUE = "Value";
            String REASON = "Reason";
            String NODE = "Node";
            String DETAIL = "Detail";
            // soap 1.1 fault
            String FAULT_CODE = "faultcode";
            String FAULT_STRING = "faultstring";
            String FAULT_ACTOR = "faultactor";
        }
    }

    public static interface Elem
    {
        ElementInfo ENVELOPE = new ElementInfo(Tag.Soap.ENVELOPE, Uri.SOAP_ENVELOPE);
        ElementInfo HEADER = new ElementInfo(Tag.Soap.HEADER, Uri.SOAP_ENVELOPE);
        ElementInfo BODY = new ElementInfo(Tag.Soap.BODY, Uri.SOAP_ENVELOPE);
        ElementInfo FAULT = new ElementInfo(Tag.Soap.FAULT, Uri.SOAP_ENVELOPE);
        ElementInfo CODE = new ElementInfo(Tag.Soap.CODE, Uri.SOAP_ENVELOPE);
        ElementInfo VALUE = new ElementInfo(Tag.Soap.VALUE, Uri.SOAP_ENVELOPE);
        ElementInfo REASON = new ElementInfo(Tag.Soap.REASON, Uri.SOAP_ENVELOPE);
        ElementInfo NODE = new ElementInfo(Tag.Soap.NODE, Uri.SOAP_ENVELOPE);
        ElementInfo DETAIL = new ElementInfo(Tag.Soap.DETAIL, Uri.SOAP_ENVELOPE);

        ElementInfo FAULT_CODE = new ElementInfo(Tag.Soap.FAULT_CODE);
        ElementInfo FAULT_STRING = new ElementInfo(Tag.Soap.FAULT_STRING);
        ElementInfo FAULT_ACTOR = new ElementInfo(Tag.Soap.FAULT_ACTOR);
        ElementInfo DETAIL_1_1 = new ElementInfo(Tag.Soap.DETAIL.toLowerCase());
    }

    public static interface Style
    {
        String RPC = "rpc";
        String DOCUMENT = "document";
    }

    public static interface Uri
    {
        String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
        String SOAP_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/";
    }
}
