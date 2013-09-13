package br.gov.serpro.wsdl2pl.util;

import br.gov.serpro.wsdl2pl.type.ElementInfo;

public interface K
{
    String TYPE_PREFFIX = "tp_rc_";
    String ARRAY_PREFFIX = "tp_va_";
    String FUNCTION_PREFFIX = "fc_";

    String TYPE_SUFFIX = "_t";
    String ARRAY_SUFFIX = "_a";

    String PROPERTY_STYLE = "style";

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
            String FAULT = "Fault";
        }
    }

    public static interface Elem
    {
        ElementInfo ENVELOPE = new ElementInfo(Tag.Soap.ENVELOPE, Uri.SOAP_ENVELOPE);
        ElementInfo HEADER = new ElementInfo(Tag.Soap.HEADER, Uri.SOAP_ENVELOPE);
        ElementInfo BODY = new ElementInfo(Tag.Soap.BODY, Uri.SOAP_ENVELOPE);
        ElementInfo FAULT = new ElementInfo(Tag.Soap.FAULT, Uri.SOAP_ENVELOPE);
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
