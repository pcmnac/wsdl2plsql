package br.gov.serpro.wsdl2pl.emitter;

public interface IKeywordEmitter
{
    String create();

    String createOrReplace();

    String as();

    String is();

    String nullKey();

    String function();

    String type();

    String record();

    String packageKey();

    String begin();

    String end();

    String ifKey();

    String elseKey();

    String forKey();

    String not();

    String and();

    String then();

    String count();

    String first();

    String last();

    String loop();

    String in();

    String of();

    String varray();

    String number();

    String returnKey();

    String out();

    String inOut();

    String varchar2();

    String defaultKey();

    String body();

    String clob();

    String xmlType();

    String declare();

    String whileKey();

    String booleanKey();

    String integer();

    String date();

    String exception();

    Object raise();

    Object elseif();

}
