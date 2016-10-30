package br.gov.serpro.wsdl2pl.emitter.impl;

import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;

public class DefaultKeywordEmitter implements IKeywordEmitter
{

    @Override
    public String create()
    {
        return "CREATE";
    }

    @Override
    public String createOrReplace()
    {
        return "CREATE OR REPLACE";
    }

    @Override
    public String as()
    {
        return "AS";
    }

    @Override
    public String is()
    {
        return "IS";
    }

    @Override
    public String nullKey()
    {
        return "NULL";
    }

    @Override
    public String function()
    {
        return "FUNCTION";
    }

    @Override
    public String type()
    {
        return "TYPE";
    }

    @Override
    public String record()
    {
        return "RECORD";
    }

    @Override
    public String packageKey()
    {
        return "PACKAGE";
    }

    @Override
    public String begin()
    {
        return "BEGIN";
    }

    @Override
    public String end()
    {
        return "END";
    }

    @Override
    public String ifKey()
    {
        return "IF";
    }

    @Override
    public String elseKey()
    {
        return "ELSE";
    }

    @Override
    public String forKey()
    {
        return "FOR";
    }

    @Override
    public String not()
    {
        return "NOT";
    }

    @Override
    public String and()
    {
        return "AND";
    }

    @Override
    public String then()
    {
        return "THEN";
    }

    @Override
    public String count()
    {
        return "COUNT";
    }

    @Override
    public String first()
    {
        return "FIRST";
    }

    @Override
    public String last()
    {
        return "LAST";
    }

    @Override
    public String loop()
    {
        return "LOOP";
    }

    @Override
    public String in()
    {
        return "IN";
    }

    @Override
    public String of()
    {
        return "OF";
    }

    @Override
    public String varray()
    {
        return "VARRAY";
    }

    @Override
    public String number()
    {
        return "NUMBER";
    }

    @Override
    public String returnKey()
    {
        return "RETURN";
    }

    @Override
    public String out()
    {
        // TODO Auto-generated method stub
        return "OUT";
    }

    @Override
    public String inOut()
    {
        // TODO Auto-generated method stub
        return "IN  OUT";
    }

    @Override
    public String varchar2()
    {
        return "VARCHAR2";
    }

    @Override
    public String defaultKey()
    {
        return "DEFAULT";
    }

    @Override
    public String body()
    {
        return "BODY";
    }

    @Override
    public String clob()
    {
        return "CLOB";
    }

    @Override
    public String xmlType()
    {
        return "XMLTYPE";
    }

    @Override
    public String declare()
    {
        return "DECLARE";
    }

    @Override
    public String whileKey()
    {
        return "WHILE";
    }

    @Override
    public String booleanKey()
    {
        return "BOOLEAN";
    }

    @Override
    public String integer()
    {
        return "INTEGER";
    }

    @Override
    public String date()
    {
        return "DATE";
    }

    @Override
    public String exception()
    {
        return "EXCEPTION";
    }

    @Override
    public String raise()
    {
        return "RAISE";
    }

    @Override
    public String elseif()
    {
        return "ELSIF";
    }

    @Override
    public String when()
    {
        return "WHEN";
    }

    @Override
    public String others()
    {
        return "OTHERS";
    }

    @Override
    public String extend()
    {
        return "EXTEND";
    }

    @Override
    public String procedure()
    {
        return "PROCEDURE";
    }

    @Override
    public String exceptionInit()
    {
        return "EXCEPTION_INIT";
    }

    @Override
    public String pragma()
    {
        return "PRAGMA";
    }

    @Override
    public String raiseApplicationError()
    {
        return "RAISE_APPLICATION_ERROR";
    }

    @Override
    public String timestampWithTimeZone()
    {
        return "TIMESTAMP WITH TIME ZONE";
    }

    @Override
    public String blob()
    {
        return "BLOB";
    }

}
